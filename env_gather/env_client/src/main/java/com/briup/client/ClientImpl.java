package com.briup.client;

import com.briup.bean.Environment;
import com.briup.log.Log;
import lombok.Setter;

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;

/**
 * @author horry
 * @Description 客户端网络模块的实现
 * @date 2024/8/23-16:02
 */
@Setter
public class ClientImpl implements Client {
	private String host;
	private int port;
	private Log log;

	@Override
	public void send(Collection<Environment> environments) throws Exception {
		// 将采集到的数据发送到服务端
		Socket client = new Socket(host, port);
		// 获取输出流
		OutputStream out = client.getOutputStream();
		// 使用BufferedOutputStream进行包装，提高传输效率
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));
		log.info("客户端开始发送数据，准备发送的数据条数为:" + environments.size());
		// 写出数据
		oos.writeObject(environments);
		// 刷新管道
		oos.flush();
		// 叫停输出
		client.shutdownOutput();
		log.info("传输成功，本次传输的数据条数为:" + environments.size());

		// 关闭资源
		client.close();
	}
}
