package com.briup.main;

import com.briup.server.Server;
import com.briup.server.ServerImpl;

/**
 * @author horry
 * @Description 服务器端的启动类
 * @date 2024/8/22-14:09
 */
public class ServerMain {
	public static void main(String[] args) {
		Server server = new ServerImpl();
		try {
			// 服务器网络模块接收数据
			server.receive();
		} catch (Exception e) {
			if ("socket closed".equals(e.getMessage())) {
				System.err.println("服务器被关闭");
			} else {
				throw new RuntimeException(e);
			}
		}
	}
}
