package net.wwwfred.framework.core.dao.mybatis;

import java.util.List;

public interface BaseDao {
	
	/**
	 * 插入记录
	 * @param t
	 * @return
	 */
	int insert(Object t);

    /**
     * 更新记录
     * @param t
     * @return
     */
    int update(Object t);
	
    /**
     * 删除记录
     * @param t
     * @return
     */
    int delete(Object t);
    

    /**
     * 查询单条记录
     * @param condition
     * @return
     */
    <T> T queryOne(T condition);
    
    /**
     * 统计记录数目
     * @param t
     * @return
     */
    Long countListPage(Object t);
    
    /**
     * 【分页】查询多条记录
     * @param t
     * @param pageNo
     * @param pageSize
     * @return
     */
    <T> List<T> queryListPage(T t, Integer pageNo, Integer pageSize);
    
}
