package net.wwwfred.framework.core.dao.mybatis.dialect;

/**
*<p>Title: OracleDialect.java </p>
*@Description:
*@Author:JERRY
*@version:1.0
*@DATE:2013-7-18下午04:39:23
*@see
*/
public class OracleDialect implements Dialect {

	@Override
	public String getPageSql(String sql, int offset, int limit) {

		sql = sql.trim();
		StringBuffer pagingSelect = new StringBuffer(sql.length() + 100);
		pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
		pagingSelect.append(sql);
		pagingSelect.append(" ) row_ ) where rownum_ <= ").append(offset+limit).append(" and rownum_ > ")
				.append(offset);

		return pagingSelect.toString();

	}

	@Override
	public String getSelectGUIDString() {
		return "select rawtohex(sys_guid()) from dual";
	}
}
