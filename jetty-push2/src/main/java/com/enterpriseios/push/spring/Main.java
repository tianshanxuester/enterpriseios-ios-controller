/*
 * LEGAL NOTICE
 *
 * Copyright (C) ${year} InventIt Inc. All rights reserved.
 *
 * This source code, product and/or document is protected under licenses
 * restricting its use, copying, distribution, and decompilation.
 * No part of this source code, product or document may be reproduced in
 * any form by any means without prior written authorization of InventIt Inc.
 * and its licensors, if any.
 *
 * InventIt Inc.
 * 9F Kojimachi DUPLEX B's
 * 4-4-7 Kojimachi, Chiyoda-ku, Tokyo 102-0083
 * JAPAN
 * http://www.yourinventit.com/
 */

package com.enterpriseios.push.spring;

import com.enterpriseios.push.web.ActiveSyncServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 *
 * @author hnishi@enterpriseios.com
 * @author dbaba@enterpriseios.com
 */
public class Main {
	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final String config = args.length == 1 ? args[0] : "etc/spring/bean.xml";
//        final String config = args.length == 1 ? args[0] : "jetty-push2/src/main/resources/etc/spring/bean.xml";
        final AbstractApplicationContext applicationContext = new FileSystemXmlApplicationContext(config);
        final ActiveSyncServer server = (ActiveSyncServer) applicationContext
                .getBean(args.length == 2 ? args[1] : "ActiveSyncServer");
        server.start();
	}
}
