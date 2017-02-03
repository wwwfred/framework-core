package net.wwwfred.framework.core.dao.mybatis.dialect;

/**
*<p>Title: MySQLDialect.java </p>
*@Description:
*@Author:JERRY
*@version:1.0
*@DATE:2013-7-18下午04:38:37
*@see
*/
public class MySQLDialect implements Dialect {

	@Override
	public String getPageSql(String sql, int offset, int limit) {
		return sql + " limit " + offset + "," + limit;
	}

	@Override
	public String getSelectGUIDString() {
		return "select uuid()";
	}

}
