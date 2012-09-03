package com.fapiko.faceworm.client;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {

	public static void main(String[] args) {

		AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"com/fapiko/faceworm/client/annotations.xml");

		FacewormClient facewormClient = applicationContext.getBean("facewormClient", FacewormClient.class);
		facewormClient.applicationLoop();

	}

}
