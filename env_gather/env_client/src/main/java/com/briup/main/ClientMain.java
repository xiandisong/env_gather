package com.briup.main;

import com.briup.bean.Environment;
import com.briup.client.Client;
import com.briup.client.ClientImpl;
import com.briup.gather.Gather;
import com.briup.gather.GatherImpl;

import java.util.Collection;

/**
 * @author horry
 * @Description 客户端的启动类，用其启动客户端
 * @date 2024/8/22-11:30
 */
public class ClientMain {
	public static void main(String[] args) {
		Gather gather = new GatherImpl();
		Client client = new ClientImpl();
		try {
			Collection<Environment> list = gather.gather();
			System.out.println("本次采集到的数据条数为:" + list.size());
			if (list.isEmpty()) {
				// 如果数据集合为空，那么本次无需传输数据
				return;
			}
			// 如果本次采集到数据，那么直接传输即可
			client.send(list);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
