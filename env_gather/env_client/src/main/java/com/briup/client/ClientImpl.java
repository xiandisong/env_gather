package com.briup.client;

import com.briup.bean.Environment;

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
public class ClientImpl implements Client {
	@Override
	public void send(Collection<Environment> environments) throws Exception {
		// 将采集到的数据发送到服务端
		Socket client = new Socket("127.0.0.1", 9999);
		// 获取输出流
		OutputStream out = client.getOutputStream();
		// 使用BufferedOutputStream进行包装，提高传输效率
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));
		// 写出数据
		oos.writeObject(environments);
		// 刷新管道
		oos.flush();
		// 叫停输出
		client.shutdownOutput();
		System.out.println("传输成功，本次传输的数据条数为:" + environments.size());

		// 关闭资源
		client.close();
	}
}
