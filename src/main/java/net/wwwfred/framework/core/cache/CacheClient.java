package net.wwwfred.framework.core.cache;

import java.util.List;

public interface CacheClient {

    /** 增加 */
    Long add(Object o);
    /** 查询单个 */
    <T> T get(T t);
    /** 删除 */
    void delete(Object o);
    /** 修改 */
    void update(Object o);
    /** 增加或修改 */
    <T> T addOrUpdate(T t);
    /** 查询所有 */
    <T> List<T> queryAll(Class<T> clazz);
    /** 查询缓存中一个数据 */
    <T> T queryOne(T t, String... fieldNameArray);
    /** 查询缓存中数据 */
    <T> List<T> query(T t, String... fieldNameArray);

}