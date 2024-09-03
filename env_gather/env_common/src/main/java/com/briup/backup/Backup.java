package com.briup.backup;

/**
 * @author horry
 * @Description 备份模块的接口，定义对备份文件读取和存储的标注
 * @date 2024/8/22-14:15
 */
public interface Backup {

	/**
	 * 用于将输出备份到指定文件的方法
	 *
	 * @param fileName 指定文件的路径
	 * @param data     需要备份的数据
	 * @param append   在写入数据时确定是拼接写，还是覆盖写
	 * @param <T>      待备份数据的类型
	 */
	<T> void store(String fileName, T data, boolean append) throws Exception;

	/**
	 * 用于加载指定文件中指定类型的数据
	 *
	 * @param fileName 指定文件的路径
	 * @param tClass   指定类型的字节码文件
	 * @param deleted  指定在读取完文件的内容后，是否需要删除该文件
	 * @param <T>      数据类型
	 * @return 返回读取到的数据
	 */
	<T> T load(String fileName, Class<T> tClass, boolean deleted) throws Exception;
}
