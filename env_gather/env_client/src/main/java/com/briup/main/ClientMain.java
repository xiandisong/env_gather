package com.briup.main;

import com.briup.bean.Environment;
import com.briup.client.Client;
import com.briup.client.ClientImpl;
import com.briup.gather.Gather;
import com.briup.gather.GatherImpl;
import com.briup.log.Log;
import com.briup.log.LogImpl;

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
		Log log = new LogImpl();
		try {
			Collection<Environment> list = gather.gather();
			log.info("本次采集到的数据条数为:" + list.size());
			if (list.isEmpty()) {
				// 如果数据集合为空，那么本次无需传输数据
				log.warn("本次采集到的数据为空，无需传输");
				return;
			}
			// 如果本次采集到数据，那么直接传输即可
			client.send(list);
		} catch (Exception e) {
			log.error("出现异常了:" + e.getCause());
		}
	}
}
