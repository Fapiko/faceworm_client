package com.fapiko.faceworm.client;

import com.tulskiy.keymaster.common.HotKey;
import com.tulskiy.keymaster.common.HotKeyListener;
import com.tulskiy.keymaster.common.MediaKey;
import com.tulskiy.keymaster.common.Provider;
import org.zeromq.ZMQ;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class FacewormClient {

	public void applicationLoop() {

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PAIR);

		socket.connect("tcp://localhost:5555");

		Provider provider = Provider.getCurrentProvider(false);


//		MyHotKeyListener hotKeyListener = new MyHotKeyListener(this);
		provider.register(MediaKey.MEDIA_NEXT_TRACK, new HotKeyListener() {
			@Override
			public void onHotKey(HotKey hotKey) {
				System.out.println(hotKey);
			}
		});
//		provider.register(MediaKey.MEDIA_PLAY_PAUSE, hotKeyListener);
//		provider.register(KeyStroke.getKeyStroke(0xAE, 0), hotKeyListener);

		while(true) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}

//		provider.reset();
//		provider.stop();
	}

	private class MyHotKeyListener implements HotKeyListener {

		private FacewormClient parent;

		public MyHotKeyListener(FacewormClient parent) {
			this.parent = parent;
		}

		@Override
		public void onHotKey(HotKey hotKey) {

			System.out.println(hotKey);
		}

	}

}
