package com.briup.log;

import org.apache.log4j.Logger;

/**
 * @author horry
 * @Description 日志模块的实现类，等同于日志的门面
 * @date 2024/8/27-15:03
 */
public class LogImpl implements Log {

	// 获取Log4j 中的日志记录器对象
	private final Logger logger = Logger.getRootLogger();

	@Override
	public void info(String message) {
		logger.info(message);
	}

	@Override
	public void warn(String message) {
		logger.warn(message);
	}

	@Override
	public void error(String message) {
		logger.error(message);
	}
}
