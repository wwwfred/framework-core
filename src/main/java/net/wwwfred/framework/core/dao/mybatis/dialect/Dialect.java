package net.wwwfred.framework.core.dao.mybatis.dialect;

/**
*<p>Title: Dialect.java </p>
*@Description:数据库方言接口
*@Author:JERRY
*@version:1.0
*@DATE:2013-7-18下午04:35:00
*@see
*/
public interface Dialect {

	public static enum Type {
		MYSQL {
			public String getValue() {
				return "mysql";
			}
		},
		SQLSERVER {
			public String getValue() {
				return "sqlserver";
			}
		},
		ORACLE {
			public String getValue() {
				return "oracle";
			}
		};
		public abstract String getValue();
	}

	/**
	 * 获取分页sql
	 * @param sql 原始查询sql
	 * @param offset 开始记录索引（从0开始计数）
	 * @param limit 每页记录大小
	 * @return 数据库相关的分页sql
	 */
	public String getPageSql(String sql, int offset, int limit);

	/**
	 * 获取系统唯一标示
	 * @return
	 */
	public String getSelectGUIDString();

}
