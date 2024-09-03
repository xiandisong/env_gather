package com.briup.dbstore;

import com.briup.bean.Environment;

import java.util.Collection;

/**
 * @author horry
 * @Description 入库模块的接口
 * @date 2024/8/22-14:07
 */
public interface DbStore {

	// 入库的方法
	void dbStore(Collection<Environment> environments) throws Exception;
}
