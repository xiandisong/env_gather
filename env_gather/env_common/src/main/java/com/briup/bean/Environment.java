package com.briup.bean;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

// 在实体类上添加了@Data注解后，lombok就会给该类在其字节码文件中
// 添加get/set、toString、equals、hashCode方法
@Data
public class Environment implements Serializable {
	private static final long serialVersionUID = 1L;

	//环境种类名称
	private String name;
	//发送端id
	private String srcId;
	//树莓派系统id
	private String desId;
	//实验箱区域模块id(1-8)
	private String devId;
	//模块上传感器地址
	private String sersorAddress;
	//传感器个数
	private int count;
	//发送指令标号 3表示接收数据 16表示发送命令
	private String cmd;
	//状态 默认1表示成功
	private int status;
	//环境值
	private float data;
	//采集时间
	private Timestamp gather_date;
}
