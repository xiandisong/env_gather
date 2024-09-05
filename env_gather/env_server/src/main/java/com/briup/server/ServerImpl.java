package com.briup.server;

import com.briup.bean.Environment;
import com.briup.dbstore.DbStoreImpl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author horry
 * @Description 服务器网络模块的实现类
 * @date 2024/8/23-16:56
 */
public class ServerImpl implements Server {
	// 循环条件
	private boolean flag = true;
	// 服务器对象
	private ServerSocket server;
	// 线程池对象
	private ThreadPoolExecutor threadPool;
	// 服务器的端口号
	private int serverPort = 9999;
	private int shutdownPort = 8989;
	// 入库模块的对象
	private DbStoreImpl dbStore = new DbStoreImpl();

	@Override
	public void receive() throws Exception {
		// 创建服务器，端口号要与客户端连接的端口号保持一致
		server = new ServerSocket(serverPort);
		System.out.println("服务器已启动，等待客户端连接");
		// 创建线程池对象
		threadPool = new ThreadPoolExecutor(5, 15,
				1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10));

		threadPool.execute(() -> {
			try {
				// 由线程池分配一个线程，调用shutdown方法，关闭服务器资源
				shutdown();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// 让服务器一直处于开启状态，一直接收客户端连接，并且接收客户端发送的数据
		while (flag) {
			// 接收客户端的连接，返回客户端的连接对象，如果没有客户端的连接，将一直处于阻塞状态
			Socket client = server.accept();
			System.out.println(client + "客户端已连接");
			// 采用多线程的方式实现，任务就是：异步处理各客户端的连接以及接收客户端传输的资源
			Runnable task = () -> {
				// 读取客户端传输的数据，先获取客户端输入流
				InputStream in = null;
				ObjectInputStream ois = null;
				try {
					in = client.getInputStream();
					// 使用对象输入流接收对象数据，使用字节缓存输入流提升传输速度
					ois = new ObjectInputStream(new BufferedInputStream(in));
					// 读取数据
					Object o = ois.readObject();
					if (!(o instanceof Collection)) {
						System.err.println("接收的数据有误：" + o);
					}
					// 如果是集合，那么直接强转
					Collection<Environment> list = (Collection<Environment>) o;
					// 输出读取的数据条数
					System.out.println("本次读取的数据条数为:" + list.size());
					// 将数据入库
					dbStore.dbStore(list);
				} catch (Exception e) {
					throw new RuntimeException(e);
				} finally {
					// 关闭资源
					try {
						assert ois != null;
						ois.close();
						in.close();
						client.close();
					} catch (IOException e) {
						System.out.println("出现异常了:" + e);
					}
				}
			};
			// 使用线程池，由线程池分配线程执行任务
			threadPool.execute(task);
		}
	}

	@Override
	public void shutdown() throws Exception {
		// 写一个监听器，一旦监听到某种信号，就立马执行后续的操作
		ServerSocket shutdownServer = new ServerSocket(shutdownPort);
		// 监听信号，一旦由客户端的连接，立马执行后续操作，如果没有客户端连接，将阻塞在本行代码中，后续操作不执行
		shutdownServer.accept();
		System.out.println("接收到关闭资源的信号");
		// 修改循环条件
		flag = false;
		// 关闭服务器资源以及线程池资源
		server.close();
		threadPool.shutdown();
		shutdownServer.close();
	}
}
