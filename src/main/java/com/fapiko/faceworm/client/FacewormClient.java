package com.fapiko.faceworm.client;

import org.zeromq.ZMQ;

public class FacewormClient {

	public void applicationLoop() {

		ZMQ.Context context = ZMQ.context(1);
		ZMQ.Socket socket = context.socket(ZMQ.PAIR);



	}

}
