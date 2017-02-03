package net.wwwfred.framework.core.dao.mybatis.dialect;

/**
*<p>Title: DialectResolver.java </p>
*@Description:
*@Author:JERRY
*@version:1.0
*@DATE:2013-9-3下午02:46:41
*@see
*/
public class DialectResolver {

	/**
	 * 获取数据库方言
	 * @param dialectName 数据库方言
	 * @return
	 */
	public final static Dialect resolveDialect(String dialectName) {

		if ("MYSQL".equalsIgnoreCase(dialectName)) {
			return new MySQLDialect();
		} else if ("oracle".equalsIgnoreCase(dialectName)) {
			return new OracleDialect();
		}
		return null;
	}
}
