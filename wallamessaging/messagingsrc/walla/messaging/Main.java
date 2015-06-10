/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package walla.messaging;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Main {

	private static final Logger meLogger = Logger.getLogger(Main.class);

	private Main() { }

	/**
	 * Load the Spring Integration Application Context
	 *
	 * @param args - command line arguments
	 * @throws Exception 
	 */
	
	public static void main(final String... args) throws Exception {

		//final AbstractApplicationContext context =
		//		new ClassPathXmlApplicationContext("classpath:META-INF/spring/integration/*-context.xml");

		final AbstractApplicationContext context =
				new ClassPathXmlApplicationContext("classpath:spring-integration-context.xml");
		
		context.registerShutdownHook();
		
		if (meLogger.isInfoEnabled()) {
			meLogger.info("\n ===== Welcome to fotowalla message processor. Please press 'q + Enter' ====");
		}

		meLogger.info(walla.utils.UserTools.GetComplexString());
		
		final Scanner scanner = new Scanner(System.in);

		while (!scanner.hasNext("q")) {
			//Do nothing unless user presses 'q' to quit.
		}
		scanner.close();

		if (meLogger.isInfoEnabled()) {
			meLogger.info("Exiting application...bye.");
		}

		System.exit(0);

	}
}
