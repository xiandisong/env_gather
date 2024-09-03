package com.briup.client;

import com.briup.bean.Environment;

import java.util.Collection;

/**
 * @author horry
 * @Description 定义客户端的网络模块接口
 * @date 2024/8/22-11:28
 */
public interface Client {
	// 用于发送采集到的数据的方法
	void send(Collection<Environment> environments) throws Exception;
}
