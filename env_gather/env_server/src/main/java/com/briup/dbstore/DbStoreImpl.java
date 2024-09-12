package com.briup.dbstore;

import com.briup.backup.Backup;
import com.briup.bean.Environment;
import com.briup.log.Log;
import com.briup.utils.JdbcUtil;
import lombok.Setter;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author horry
 * @Description 入库模块的实现类
 * @date 2024/8/26-16:54
 */
@Setter
public class DbStoreImpl implements DbStore {

	private String backupFile;
	private Log log;
	private Backup backup;

	@Override
	public void dbStore(Collection<Environment> environments) throws Exception {
		if (new File(backupFile).exists()) {
			// 如果备份文件存在，那么说明存在上一次还未入库完的数据
			@SuppressWarnings("unchecked")
			Collection<Environment> c = backup.load(backupFile, Collection.class, true);
			// 将上一次未入库完的数据，添加到本次入库数据之前
			c.addAll(environments);
			// 将所有等待入库的数据赋值给environments变量
			environments = c;
		}

		log.info("准备入库，入库的数据条数为:" + environments.size());
		// 获取数据库连接
		Connection conn = JdbcUtil.getConnection();

		// day 用于记录某一批待入库数据属于 哪一天/哪一个数据库 的数据
		int day = 0;
		// count 用于记录遍历的数据条数
		int count = 0;
		// commitCount 用于记录以及提交的 数据条数
		int commitCount = 0;

		// statement对象
		PreparedStatement ps = null;
		// 将采集到的数据 根据数据的采集日期 分别 放入 31张表中
		try {
			for (Environment env : environments) {
				Timestamp gatherDate = env.getGather_date();
				LocalDateTime localDateTime = gatherDate.toLocalDateTime();
				// 获取本条环境数据采集的日期 在当月的第几天
				int currentDay = localDateTime.getDayOfMonth();

				// 根据日期决定将数据存入哪个表中，根据日期决定是否要提交入库的表
				if (currentDay != day) {
					// 在替换PreparedStatement前将本批未提交的数据及时提交
					if (ps != null) {
						ps.executeBatch();
						conn.commit();
						ps.close();
						// 在提交后，记录已经提交到的数据条数
						commitCount = count;
					}
					// 如果本条数据所在日期，与本批入库的日期不一致，那么需要替换入库的表
					String sql = "insert into env_detail_" + currentDay + "(name, src_id, des_id, dev_id, " +
							"sersor_address, count, cmd, status, data, gather_date)" +
							" values(?,?,?,?,?,?,?,?,?,?)";
					// 替换PreparedStatement，即替换待入库的表
					ps = conn.prepareStatement(sql);
					day = currentDay;
				}
				assert ps != null;
				initPs(ps, env);

				/*if (count == 37899) {
					int i = 10 / 0;
				}*/

				// 执行语句，每遍历500条数据提交一次
				if (++count % 500 == 0 || count == environments.size()) {
					ps.executeBatch();
					// 提交事务
					conn.commit();
					commitCount = count;
				}
			}
			log.info("数据入库成功，本次入库的数据条数为:" + environments.size());
		} catch (Exception e) {
			// 当出现异常时，对数据进行回滚
			log.error(String.format("出现异常了，本次入库%s条数据，准备回滚", commitCount));
			conn.rollback();
			// 在数据回滚后，将本次未及时入库的数据备份到文件中
			// 截取需要本分的数据子集
			List<Environment> backupList = environments.stream().skip(commitCount)
					.collect(Collectors.toList());
			// 将数据备份到文件中
			backup.store(backupFile, backupList, false);
		} finally {
			// 关闭资源
			JdbcUtil.close(conn, ps);
		}
	}

	/**
	 * 将环境对象的数据填充到PreparedStatement中
	 *
	 * @param ps  PreparedStatement对象
	 * @param env 环境数据对象
	 */
	private void initPs(PreparedStatement ps, Environment env) throws SQLException {
		// 将环境对象的数据，依次填充到 PS中即可
		ps.setString(1, env.getName());
		ps.setString(2, env.getSrcId());
		ps.setString(3, env.getDesId());
		ps.setString(4, env.getDevId());
		ps.setString(5, env.getSersorAddress());
		ps.setInt(6, env.getCount());
		ps.setString(7, env.getCmd());
		ps.setInt(8, env.getStatus());
		ps.setFloat(9, env.getData());
		ps.setTimestamp(10, env.getGather_date());
		// 将sql语句添加到批处理中
		ps.addBatch();
	}
}
