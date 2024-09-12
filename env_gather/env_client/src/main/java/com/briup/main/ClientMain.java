package com.briup.main;

import com.briup.bean.Environment;
import com.briup.client.Client;
import com.briup.config.BeanFactory;
import com.briup.gather.Gather;
import com.briup.log.Log;

import java.util.Collection;

/**
 * @author horry
 * @Description 客户端的启动类，用其启动客户端
 * @date 2024/8/22-11:30
 */
public class ClientMain {
	public static void main(String[] args) {
		Gather gather = BeanFactory.getBean("gather", Gather.class);
		Client client = BeanFactory.getBean("client", Client.class);
		Log log = BeanFactory.getBean("log", Log.class);
		try {
			Collection<Environment> list = gather.gather();
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
