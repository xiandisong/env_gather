package com.briup.backup;

import com.briup.log.Log;
import com.briup.log.LogImpl;

import java.io.*;

/**
 * @author horry
 * @Description 备份模块的基础实现类
 * @date 2024/8/28-9:08
 */
public class BackupImpl implements Backup {

	private final Log log = new LogImpl();

	@Override
	public <T> void store(String fileName, T data, boolean append) throws Exception {
		log.info("=====开始备份文件=====");
		// 创建文件输出流
		FileOutputStream out = new FileOutputStream(fileName, append);
		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(out));
		// 将数据写出
		oos.writeObject(data);
		oos.flush();
		// 关闭资源
		oos.close();
		out.close();
		log.info("=====文件备份成功=====");
	}

	@Override
	public <T> T load(String fileName, Class<T> tClass, boolean deleted) throws Exception {
		log.info("=====开始读取备份文件=====");
		File file = new File(fileName);
		if (!file.exists()) {
			log.error(String.format("读取失败，备份文件不存在:[%s]", fileName));
		}

		// 创建文件输入流
		FileInputStream in = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
		// 读取数据
		Object o = ois.readObject();
		log.info("=====备份数据读取成功=====");

		// 根据传入的标识，确定是否需要删除数据
		if (deleted) {
			boolean delete = file.delete();
			log.info(String.format("=====备份文件被删除:%s=====", delete));
		}
		// 关闭资源
		ois.close();
		in.close();

		return (T) o;
	}
}
