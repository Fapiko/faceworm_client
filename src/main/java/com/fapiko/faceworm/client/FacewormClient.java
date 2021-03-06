package com.fapiko.faceworm.client;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.MediaKey;
import com.tulskiy.keymaster.common.Provider;
import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

public class FacewormClient {

	private LinkedList<StringBuffer> messageBuffer = new LinkedList<StringBuffer>();

	private boolean shouldTerminate = false;
	private static Logger logger = Logger.getLogger(FacewormClient.class);

	private static final int APPLICATION_LOOP_DELAY = 50;
	private static final int HEALTHCHECK_DELAY = 60000;
	private static final String FACEWORM_SERVER_HOSTNAME = "192.168.0.9";
	private ZMQ.Context context;

	/**
	 * Main application loop
	 */
	public void applicationLoop() {

		int healthcheckTimer = 0;

		context = ZMQ.context(1);
		ZMQ.Socket socket = instantiatePublisher(5555);
		ZMQ.Socket socketHealthcheck = instantiateSubscriber(5556, "ACTION");

		logger.debug("Using ZMQ Version: " + ZMQ.getVersionString());

		socket.setHWM(1);

		Provider provider = registerHotkeys();

		while(!shouldTerminate) {

			while (messageBuffer.size() > 0) {
				socket.send(String.valueOf(messageBuffer.poll()).getBytes(), 0);
			}

			healthcheckTimer += APPLICATION_LOOP_DELAY;
			if (healthcheckTimer >= HEALTHCHECK_DELAY) {

				socket.close();
				socket = instantiatePublisher(5555);

				socketHealthcheck.close();
				socketHealthcheck = instantiateSubscriber(5556, "ACTION");


				healthcheckTimer = 0;

			}

			try {
				Thread.sleep(APPLICATION_LOOP_DELAY);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		provider.reset();
		provider.stop();

	}

	/**
	 * Registers the hotkeys and returns the associated provider
	 * @return provider
	 */
	private Provider registerHotkeys() {

		Provider provider = Provider.getCurrentProvider(false);

		MyHotKeyListener hotKeyListener = new MyHotKeyListener(this);
		provider.register(MediaKey.MEDIA_NEXT_TRACK, hotKeyListener);
		provider.register(MediaKey.MEDIA_PLAY_PAUSE, hotKeyListener);
		provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				hotKeyListener);
		provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				hotKeyListener);
		provider.register(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				hotKeyListener);

		return provider;

	}

	/**
	 * Instantiates the <strong>sub</strong>scriber side of a Pub/Sub connection
	 * @param port Port to listen on
	 * @return Returns the newly created socket
	 */
	private ZMQ.Socket instantiateSubscriber(int port, String filter) {

		ZMQ.Socket socket = context.socket(ZMQ.SUB);
		socket.subscribe(filter.getBytes());
		socket.connect(String.format("tcp://%s:%d", FACEWORM_SERVER_HOSTNAME, port));

		return socket;

	}

	/**
	 * Instantiates the <strong>pub</strong>lisher side of a Pub/Sub connection
	 * @param port Port to listen on
	 * @return Returns the newly created socket
	 */
	private ZMQ.Socket instantiatePublisher(int port) {

		ZMQ.Socket socket = context.socket(ZMQ.PUB);
		socket.connect(String.format("tcp://%s:%s", FACEWORM_SERVER_HOSTNAME, port));

		return socket;

	}

	/**
	 * Indicates to the main application loop that it should terminate when done processing the current run
	 */
	protected void shutdown() {

		logger.info("Setting terminate flag");
		shouldTerminate = true;

	}

	/**
	 * Getter method to return the message queue
	 *
	 * @return messageBuffer
	 */
	protected LinkedList<StringBuffer> getMessageBuffer() {
		return messageBuffer;
	}

	private class MyHotKeyListener implements HotKeyListener {

		private FacewormClient parent;
		private Logger logger;

		public MyHotKeyListener(FacewormClient parent) {

			this.parent = parent;
			logger = Logger.getLogger(MyHotKeyListener.class);

		}

		@Override
		public void onHotKey(HotKey hotKey) {

			StringBuffer payload = new StringBuffer("ACTION|");

			if (hotKey.isMedia()) {

				if (hotKey.mediaKey.equals(MediaKey.MEDIA_NEXT_TRACK)) {

					payload.append("NEXT_TRACK");

				} else if (hotKey.mediaKey.equals(MediaKey.MEDIA_PLAY_PAUSE)) {

					payload.append("PLAY_PAUSE");

				} else {

					logger.warn("Unknown media key received");
					logger.warn(hotKey.mediaKey);

				}

			} else {

				logger.info(hotKey.keyStroke);

				if (hotKey.keyStroke.getModifiers() == KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK |
						KeyEvent.SHIFT_DOWN_MASK).getModifiers())
				{

					if (hotKey.keyStroke.getKeyCode() == KeyEvent.VK_UP) {

						payload.append("THUMBS_UP");

					} else if (hotKey.keyStroke.getKeyCode() == KeyEvent.VK_DOWN) {

						payload.append("THUMBS_DOWN");

					} else if (hotKey.keyStroke.getKeyCode() == KeyEvent.VK_ESCAPE) {

						parent.shutdown();

					} else {

						logger.warn("Unknown key stroke received");
						logger.warn(hotKey.keyStroke);

					}

				} else {

					logger.warn("Unknown key stroke modifiers received");
					logger.warn(KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
					logger.warn(hotKey.keyStroke.getModifiers());

				}

			}

			logger.info(payload);
			parent.getMessageBuffer().add(payload);

		}

	}

}
