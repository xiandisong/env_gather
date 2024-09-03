package com.briup.server;

/**
 * @author horry
 * @Description 服务端网络模块的接口
 * @date 2024/8/22-14:05
 */
public interface Server {

	// 接收数据的服务器
	void receive() throws Exception;

	// 关闭服务器的方法
	void shutdown() throws Exception;
}
