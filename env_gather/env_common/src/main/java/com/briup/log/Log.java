package com.briup.log;

/**
 * @author horry
 * @Description 定义日志模块的接口，提供一个统一的面门标注
 * slf4j: simple logging facade for java
 * @date 2024/8/22-14:10
 */
public interface Log {

	// 负责记录输出 日志中info级别的信息
	void info(String message);

	// 负责记录输出 日志中warn级别的信息
	void warn(String message);

	// 负责记录输出 日志中error级别的信息
	void error(String message);
}
