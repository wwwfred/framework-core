package net.wwwfred.framework.core.dao.mybatis;

import java.util.List;
import java.util.Map;

import net.wwwfred.framework.core.dao.DaoQueryCondition;

public interface MybatisDao {
	
    /**
     * 插入记录
     * @param one 要插入到表中数据对象
     * @return 返回要插入的对象（主键已被赋值）
     */
    <T> T insertOne(T one);
    
    /**
     * 更新记录，根据主键获取老的数据记录，并把非主键的字段的数据更新到老的数据记录中
     * @param one 要更新的数据记录
     * @return 要更新的数据
     */
    <T> T updateOne(T one);
    
    /**
     * 根据查询条件更新指定字段的值，并返回受影响的记录数
     * @param fieldValueMap
     * @param clazz
     * @param conditionArray
     * @return
     */
    int update(Map<String, Object> fieldValueMap, Class<?> clazz, DaoQueryCondition... conditionArray);
    
    /**
     * 删除记录，根据主键获取老的数据记录，并删除该记录
     * @param clazz 要删除的对象类型
     * @param idValue 删除的对象的主键值
     * @return 实际删除影响的记录数一般都为1
     */
    <T> int deleteOne(Class<T> clazz, Object idValue);
    
    /**
     * 根据查询的结果删除记录
     * @param clazz 要删除的对象类型
     * @param conditionArray 删除的对象满足的条件
     * @return 实际删除的记录数
     */
    <T> int deleteList(Class<T> clazz, DaoQueryCondition... conditionArray);
    
    /**
     * 获取记录，根据主键获取数据记录
     * @param clazz 要获取的数据类型
     * @param idValue 要获取的数据主键的值
     * @return 指定类型指定主键的数据记录
     */
    <T> T get(Class<T> clazz, Object idValue);

    /**
     * 根据 指定的结果集字段类型（可多个，当为空时查询所有的字段;当为1一个时只查询指定的字段的值，否则都是查询指定类型的对象），指定的类型，指定的查询条件，查询出满足条件的数据记录集合中第一条记录，若没查到返回空
     * @param resultFieldName 查询数据的结果集字段类型（可多个，当为空时查询所有的字段;当为1一个时只查询指定的字段的值，否则都是查询指定类型的对象）
     * @param clazz 要查询的类型
     * @param conditionArray 要查询的记录的条件（字段名，字段名与值的操作符，字段值）（可多个）
     * @return 查询出满足条件的数据记录集合中第一条记录，若没查到返回空
     */
    <R,T> R selectOne(String[] resultFieldName, Class<T> clazz, DaoQueryCondition... conditionArray);

    /**
     * 根据指定的结果集字段类型（可多个，当为空时查询所有字段;当为1一个时只查询指定的字段的List集合，否则都是查询指定类型的对象的List集合），是否去重，指定类型，指定查询条件，分页查询出满足条件的数据记录集合，若没有查到返回空集合
     * @param resultFieldName 指定的结果集字段类型（可多个，当为空时查询所有字段;当为1一个时只查询指定的字段的List集合，否则都是查询指定类型的对象的List集合）
     * @param distinct 是否去重
     * @param clazz 指定类型
     * @param pageNo 指定页码
     * @param pageSize 指定每页记录数
     * @param conditionArray  要查询的记录的条件（字段名，字段名与值的操作符，字段值）（可多个）
     * @return 查询出满足条件的数据记录集合，若没有查到返回空集合
     */
    <R,T> List<R> selectList(String[] resultFieldName, boolean distinct, Class<T> clazz, Integer pageNo,
            Integer pageSize, DaoQueryCondition... conditionArray);

    /**
     * 统计指定字段的记录，是否去重，指定类型，指定条件，若统计查询无结果返回0，否则返回统计结果数
     * @param resultFieldName 要统计的指定字段
     * @param distinct 是否去重
     * @param clazz 指定要统计的类型
     * @param conditionArray 指定查询条件
     * @return
     */
    <T> long count(String resultFieldName, boolean distinct, Class<T> clazz,
            DaoQueryCondition... conditionArray);

}