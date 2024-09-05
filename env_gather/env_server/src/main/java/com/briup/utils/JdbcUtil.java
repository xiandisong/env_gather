package com.briup.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

/**
 * JDBC工具类
 */
public class JdbcUtil {

	// 核心的数据源
	private static final DataSource pool;
	// 定义类型与ResultSet中获取列数据方法的关系映射集合
	private static final Map<String, Method> MAP = new HashMap<>();

	static {
		// 加载数据源，创建数据库连接池，放在静态代码块中，只会执行一次
		// 读取配置文件
		InputStream in = null;
		try {
			in = JdbcUtil.class.getClassLoader()
					.getResourceAsStream("database_config.properties");
			Properties properties = new Properties();
			properties.load(in);
			// 创建数据库连接池
			pool = DruidDataSourceFactory.createDataSource(properties);
			// 初始化 映射集合
			initMap();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void initMap() throws Exception {
		Class<ResultSet> resultSetClass = ResultSet.class;
		// 将getString方法放入到集合中
		// 将配置文件中类型与方法名字之间的关系，变为类型与方法之间的关系，维护在Map集合中
		InputStream in = JdbcUtil.class.getClassLoader()
				.getResourceAsStream("type2Method.properties");
		Properties prop = new Properties();
		prop.load(in);
		Set<Map.Entry<Object, Object>> entries = prop.entrySet();
		// 将映射关系放入到集合中
		for (Map.Entry<Object, Object> entry : entries) {
			MAP.put((String) entry.getKey(),
					resultSetClass.getDeclaredMethod((String) entry.getValue(), String.class));
		}
	}

	/**
	 * 从数据库连接池中获取数据库连接对象的方法
	 *
	 * @return 数据库连接对象
	 */
	public static Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	/**
	 * 关闭资源，DDL和DML语句及其其他语句关闭资源
	 *
	 * @param conn  数据库连接对象，将资源放回连接池中
	 * @param state statement或其子类对象
	 */
	public static void close(Connection conn, Statement state) throws SQLException {
		close(conn, state, null);
	}

	/**
	 * 针对DQL语句操作的资源关闭
	 *
	 * @param conn  数据库连接对象
	 * @param state statement及其子类对象
	 * @param rs    数据集资源对象
	 */
	public static void close(Connection conn, Statement state, ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
		}
		if (state != null) {
			state.close();
		}
		if (conn != null) {
			conn.close();
		}
	}

	/**
	 * 针对DDL语句及DML语句sql语句执行的封装
	 *
	 * @param sql sql语句
	 */
	public static void execute(String sql) throws SQLException {
		// 1. 获取数据库连接对象
		Connection conn = getConnection();
		// 2. 获取statement对象
		Statement statement = conn.createStatement();
		try {
			// 3. 执行sql语句
			boolean execute = statement.execute(sql);
			// 4. 上述代码执行无误，提交事务
			conn.commit();
		} catch (Exception e) {
			// 执行过程中出现异常，全部回滚
			conn.rollback();
		} finally {
			// 4. 关闭资源
			close(conn, statement);
		}
	}

	/**
	 * 在执行完sql后，将sql执行后影响的数据行数返回
	 */
	public static int executeUpdate(String sql) throws SQLException {
		// 1. 获取数据库连接对象
		Connection conn = getConnection();
		// 2. 获取statement对象
		Statement statement = conn.createStatement();
		try {
			// 3. 执行sql语句
			int i = statement.executeUpdate(sql);
			// 4. 上述代码执行无误，提交事务
			conn.commit();
			return i;
		} catch (Exception e) {
			System.err.println("出现异常了:" + e.getCause());
			// 执行过程中出现异常，全部回滚
			conn.rollback();
			// 返回-1表示sql执行失败
			return -1;
		} finally {
			// 4. 关闭资源
			close(conn, statement);
		}
	}

	/**
	 * 使用预处理的方式去执行sql语句
	 *
	 * @param function 用于创建PreparedStatement对象的函数
	 * @return sql执行的结果
	 */
	public static int executeUpdateByPrepared(Function<Connection, PreparedStatement> function) throws SQLException {
		// 1. 获取数据库连接对象
		Connection conn = getConnection();
		// 2. 获取PreparedStatement对象，在创建过程中要包含参数初始化的过程
		PreparedStatement ps = function.apply(conn);
		try {
			// 3. 执行sql语句
			int i = ps.executeUpdate();
			// 4. 提交事务
			conn.commit();
			return i;
		} catch (Exception e) {
			System.err.println("出现异常了:" + e.getCause());
			conn.rollback();
			return -1;
		} finally {
			close(conn, ps);
		}
	}

	//DQL语句执行过程的封装
	public static <T> List<T> executeQuery(String sql, Function<ResultSet, List<T>> function) throws SQLException {
		// 1. 获取数据库连接对象
		Connection conn = getConnection();
		// 2. 获取statement对象
		Statement statement = conn.createStatement();
		// 3. 执行sql语句
		ResultSet resultSet = statement.executeQuery(sql);
		// 4. 处理结果集，将查询的结果 与 Java中对应类的对象进行映射
		List<T> list = function.apply(resultSet);
		// 5. 关闭资源
		close(conn, statement, resultSet);
		return list;
	}

	/**
	 * 使用反射的方式处理结果集，实现结果数据 与 Java类型对象 映射的自动化
	 *
	 * @param sql    待执行的sql语句
	 * @param tClass 结果集映射的 类型 所在类的字节码对象
	 * @param <T>    结果类型
	 * @return 结果集转换为Java对象后的集合
	 */
	public static <T> List<T> executeQuery(String sql, Class<T> tClass) throws Exception {
		// 1. 获取数据库连接对象
		Connection conn = getConnection();
		// 2. 获取statement对象
		Statement statement = conn.createStatement();
		// 3. 执行sql语句
		ResultSet resultSet = statement.executeQuery(sql);
		// 创建结果集的集合
		List<T> ts = new ArrayList<>();
		// 4. 处理结果集
		while (resultSet.next()) {
			// 每遍历一次，创建一个类对象，用于存储本条数据；通过无参构造器创建
			T t = tClass.getDeclaredConstructor().newInstance();
			// 解读本行中的数据，将数据设置到 Java类中对应的属性中
			// 遍历类中所有的属性，将根据属性名从ResultSet中获取值
			for (Field field : tClass.getDeclaredFields()) {
				// 获取属性名及其对应的类型
				String name = field.getName();
				String type = field.getType().getSimpleName();

				// 根据属性的类型，决定如何从ResultSet中获取对应的值
				Object value = MAP.get(type.toLowerCase()).invoke(resultSet, name);
				// 给属性赋值，使用直接赋值的方式，先需要将属性设置为可访问的，此方式会破坏封装性，不推荐使用
				// 应该使用属性对应的set方法
				invokeSetMethod(tClass, field, t, value);
			}
			ts.add(t);
		}
		return ts;
	}

	/**
	 * 执行属性对应的set方法，给属性赋值
	 *
	 * @param tClass 字节码文件对象
	 * @param field  属性的对象
	 * @param args   调用方法时，传递的参数
	 * @param t      调用方法的对象
	 */
	private static <T> void invokeSetMethod(Class<T> tClass, Field field, T t, Object... args) throws Exception {
		String name = field.getName();
		Class<?> type = field.getType();
		// 拼接属性对应的set方法，set方法的名称通常为 setXxx，如setName()，而方法的参数类型与属性的类型一致
		String methodName = "set" + name.substring(0, 1).toUpperCase()
				+ name.substring(1);
		// 根据方法名与方法的参数列表类型可以获取到指定的方法
		Method method = tClass.getDeclaredMethod(methodName, type);
		// 执行set方法
		if (method.getModifiers() != 2) {
			// 指定调用方法的对象，以及指定该方法的参数是什么
			method.invoke(t, args);
		}
	}
}
