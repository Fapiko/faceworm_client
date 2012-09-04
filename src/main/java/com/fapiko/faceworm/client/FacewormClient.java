package com.fapiko.faceworm.client;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.MediaKey;
import com.tulskiy.keymaster.common.Provider;
import org.apache.log4j.Logger;
import org.zeromq.ZMQ;

public class FacewormClient {

	private boolean shouldTerminate = false;
	private static Logger logger = Logger.getLogger(FacewormClient.class);

	public void applicationLoop() {

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PAIR);

		socket.connect("tcp://localhost:5555");

		Provider provider = Provider.getCurrentProvider(false);

		MyHotKeyListener hotKeyListener = new MyHotKeyListener(this);
		provider.register(MediaKey.MEDIA_NEXT_TRACK, hotKeyListener);
		provider.register(MediaKey.MEDIA_PLAY_PAUSE, hotKeyListener);

		while(!shouldTerminate) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		provider.reset();
		provider.stop();

	}

	protected void shutdown() {
		logger.info("Setting terminate flag");
		shouldTerminate = true;
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

			logger.info(hotKey);
			parent.shutdown();

		}

	}

}
