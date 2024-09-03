package com.briup.main;

import java.io.IOException;
import java.net.Socket;

/**
 * @author horry
 * @Description 关闭资源服务器的客户端
 * @date 2024/8/26-14:34
 */
public class CloseMain {

	public static void main(String[] args) throws IOException {
		Socket socket = new Socket("127.0.0.1", 8989);
		socket.close();
	}
}
