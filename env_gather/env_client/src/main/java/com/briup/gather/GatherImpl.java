package com.briup.gather;

import com.briup.bean.Environment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author horry
 * @Description 采集模块的实现类，负责采集文件中的数据，并且对其进行解析与清洗
 * @date 2024/8/22-11:26
 */
public class GatherImpl implements Gather {
	@Override
	public Collection<Environment> gather() throws Exception {
		// 采集数据本质上就是为了读取data-file文件中的内容
		InputStream in = GatherImpl.class.getClassLoader()
				.getResourceAsStream("data-file-simple");
		// 该文件中每一行数据 就是一个整体，表示一条环境数据，应该考虑使用 BufferedReader中的readLine()
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		// 因为文件存在多行数据，所以应该使用集合将所有数据进行保存
		List<Environment> list = new ArrayList<>();
		String line;
		while ((line = br.readLine()) != null) {
			// 100|101|2|16|1|3|5d606f7802|1|1516323596029 数据是以|分割成9部分
			String[] infos = line.split("[|]");
			if (infos.length != 9) {
				// 数据分割后不够9部分，说明本条数据有问题
				System.err.println("出现问题数据:" + line);
				continue;
			}

			// 将字符串数组中的数据转换为 Environment 对象
			Environment env = new Environment();
			// 调用初始化环境对象的方法
			initEnv(env, infos);

			// 根据传感器地址，区分本条采集的数据是什么
			switch (infos[3]) {
				case "16":
					// 根据第四部分的 标识 不同 决定第七部分数据的所属类型 （16：温湿度，256：光照强度，1280：二氧化碳浓度）
					// 如果是16 那么第七部分数据中 前两个字节数据为 温度数据，中间两个字节的数据为 湿度数据
					// f: 15 16进制的表现形式 --> 1111: 15 二进制的表现形式 所以 1个16进制数 在 2进制中占 4位
					// 以5d606f7802为例， 前4位数 5d60 为温度数据，中间5-8位 6f78为 湿度数据
					env.setName("温度");
					// 给温度数据赋值
					String dataStr = infos[6].substring(0, 4);
					float data = Integer.parseInt(dataStr, 16) * (0.00268127F) - 46.85F;
					env.setData(data);

					Environment env2 = new Environment();
					initEnv(env2, infos);
					env2.setName("湿度");
					// 设置湿度的数据
					String dataStr2 = infos[6].substring(4, 8);
					float data2 = Integer.parseInt(dataStr2, 16) * 0.00190735F - 6;
					env2.setData(data2);
					list.add(env2);
					break;
				case "256":
					// 光照强度的数据
					env.setName("光照强度");
					env.setData(Integer.parseInt(infos[6].substring(0, 4), 16));
					break;
				case "1280":
					env.setName("二氧化碳浓度");
					env.setData(Integer.parseInt(infos[6].substring(0, 4), 16));
					break;
				default:
					System.err.println("出现异常传感器地址数据:" + infos[3]);
					continue;
			}

			list.add(env);
		}
		return list;
	}

	private void initEnv(Environment env, String[] infos) {
		env.setSrcId(infos[0]);
		env.setDesId(infos[1]);
		env.setDevId(infos[2]);
		env.setSersorAddress(infos[3]);
		env.setCount(Integer.parseInt(infos[4]));
		env.setCmd(infos[5]);
		env.setStatus(Integer.parseInt(infos[7]));
		// 将毫秒数 转换为 long类型，在创建TimeStamp 对象时传入
		long time = Long.parseLong(infos[8]);
		env.setGather_date(new Timestamp(time));
	}
}
