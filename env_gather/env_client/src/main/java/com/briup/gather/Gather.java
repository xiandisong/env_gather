package com.briup.gather;

import com.briup.bean.Environment;

import java.util.Collection;

// 面向接口编程，在写具体的代码前，可以先定义其接口，规范类与方法
public interface Gather {
	// 采集模块中需要包含采集的方法，返回值为 环境数据对象的集合
	Collection<Environment> gather() throws Exception;
}
