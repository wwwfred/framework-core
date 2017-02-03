package net.wwwfred.framework.core.dao.mybatis.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import net.wwwfred.framework.core.dao.ColumnAnnotaion;
import net.wwwfred.framework.core.dao.DaoException;
import net.wwwfred.framework.core.dao.DaoQueryCondition;
import net.wwwfred.framework.core.dao.DaoQueryOperator;
import net.wwwfred.framework.core.dao.DbDialectEnum;
import net.wwwfred.framework.core.dao.IdAnnotaion;
import net.wwwfred.framework.core.dao.TableAnnotaion;
import net.wwwfred.framework.core.dao.mybatis.BaseDao;
import net.wwwfred.framework.core.dao.mybatis.MybatisDao;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.log.LogUtil;
import net.wwwfred.framework.util.reflect.ReflectUtil;
import net.wwwfred.framework.util.string.StringUtil;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * MyBatisDao 通用处理数据增删该查到数据库
 * @author wangwenwu
 * @since 2015-06-26
 */
public class MybatisDaoImpl implements BaseDao,MybatisDao,ApplicationContextAware {
    
    /** 查询语句中去重的关键字 */
    public static String SQL_DISTINCT = "DISTINCT";
    
    /** 字段名之间分隔符 */
    public static String FIELD_NAME_SEPARATOR_TAG = ".";
    
    // 条件语句中数组值的符号
    public static String SQL_ARRAY_LEFT_TAG = "(";
    public static String SQL_ARRAY_SEPARATOR_TAG = ",";
    public static String SQL_ARRAY_RIGHT_TAG = ")";
    
    /** sql语句中值的引号 */
    public static String SQL_VALUE_QUOTE_TAG = "'";

    /** mybatisDao.xml中命名空间namespace与id之间的分隔符 */
    public static String SQL_STATEMENT_SEPARATOR = ".";
    
    /** 指定命名空间下获取表字段语句唯一标识 */
    public static String SQL_SELECT_COLUMNS = "selectColumns";
    public static String SQL_SELECT_COLUMNS_VALUE = "<script>SELECT COLUMN_NAME FROM  <if test=\"dialect=='oracle'\"> 	all_tab_columns 	WHERE 	TABLE_NAME = #{tableName} </if> <if test=\"dialect=='mysql'\"> 	information_schema.COLUMNS 	WHERE 	TABLE_NAME = #{tableName} </if></script>";
    /** 指定命名空间下获取插入语句唯一标识 */
    public static String SQL_GET_ID = "getId";
    public static String SQL_GET_ID_VALUE = "<script>SELECT <if test=\"dialect=='oracle'\"> 	${tableSeqName}.NEXTVAL  	FROM  	DUAL </if> <if test=\"dialect=='mysql'\"> 	LAST_INSERT_ID() </if></script>";
    /** 指定命名空间下插入语句唯一标识 */
    public static String SQL_INSERT_ONE = "insertOne";
    public static String SQL_INSERT_ONE_VALUE = "<script>INSERT ${tableName} ( <foreach item=\"item\" index=\"index\" collection=\"columnArray\" 	open=\"\" separator=\",\" close=\"\"> 	${item} </foreach> ) VALUES <foreach item=\"item\" index=\"index\" collection=\"valueArray\" 	open=\"(\" separator=\",\" close=\")\"> 	#{item} </foreach></script>";
    /** 指定命名空间下修改语句唯一标识 */
    public static String SQL_UPDATE_ONE = "updateOne";
    public static String SQL_UPDATE_ONE_VALUE = "<script>UPDATE ${tableName} SET <foreach item=\"item\" index=\"index\" collection=\"columnValueArray\" 	open=\"\" separator=\",\" close=\"\"> 	${item.columnName} 	= 	#{item.fieldValue} </foreach> <if test=\"conditionArray != null\"> 	<where> 		<foreach item=\"item\" index=\"index\" collection=\"conditionArray\" 			open=\"(\" separator=\"AND\" close=\")\"> 			${item.fieldName} ${item.operator.value} ${item.fieldValue} 		</foreach> 	</where> </if></script>";
    /** 指定命名空间下删除语句唯一标识 */
    public static String SQL_DELETE_ONE = "deleteOne";
    public static String SQL_DELETE_ONE_VALUE = "<script>DELETE FROM ${tableName} <if test=\"conditionArray != null\"> 	<where> 		<foreach item=\"item\" index=\"index\" collection=\"conditionArray\" 			open=\"(\" separator=\"AND\" close=\")\"> 			${item.fieldName} ${item.operator.value} ${item.fieldValue} 		</foreach> 	</where> </if></script>";
    /** 指定命名空间下查询唯一条记录语句唯一标识 */
    public static String SQL_SELECT_ONE = "selectOne"; 
    public static String SQL_SELECT_ONE_VALUE = "<script>SELECT <foreach item=\"item\" index=\"index\" collection=\"columnArray\" 	open=\"\" separator=\",\" close=\"\"> 	${item} </foreach> FROM ${tableName} <if test=\"conditionArray != null\"> 	<where> 		<foreach item=\"item\" index=\"index\" collection=\"conditionArray\" 			open=\"(\" separator=\"AND\" close=\")\"> 			${item.fieldName} ${item.operator.value} ${item.fieldValue} 		</foreach> 	</where> </if></script>"; 
    /** 指定命名空间下查询多条记录语句唯一标识 */
    public static String SQL_SELECT_LIST = "selectList";  
    public static String SQL_SELECT_LIST_VALUE = "<script>SELECT <foreach item=\"item\" index=\"index\" collection=\"columnArray\" 	open=\"\" separator=\",\" close=\"\"> 	${item} </foreach> FROM ${tableName} <if test=\"conditionArray != null\"> 	<where> 		<foreach item=\"item\" index=\"index\" collection=\"conditionArray\" 			open=\"(\" separator=\"AND\" close=\")\"> 			${item.fieldName} ${item.operator.value} ${item.fieldValue} 		</foreach> 	</where> </if></script>";  
    /** 指定命名空间下统计记录语句唯一标识 */
    public static String SQL_COUNT = "count";
    public static String SQL_COUNT_VALUE = "<script>SELECT COUNT( ${distinct} <foreach item=\"item\" index=\"index\" collection=\"columnArray\" 	open=\"\" separator=\",\" close=\"\"> 	${item} </foreach> ) FROM ${tableName} <if test=\"conditionArray != null\"> 	<where> 		<foreach item=\"item\" index=\"index\" collection=\"conditionArray\" 			open=\"(\" separator=\"AND\" close=\")\"> 			${item.fieldName} ${item.operator.value} ${item.fieldValue} 		</foreach> 	</where> </if></script>";
    
    /** 注入spring context */
    protected ApplicationContext applicationContext;
    /** 注入 sqlSessionTemplate */
    protected SqlSessionTemplate sqlSessionTemplate;
    
    /** 数据库语言 */
    private DbDialectEnum dialect;
    /** dialect mysql */
    private static String FLAG_MYSQL = "mysql";
    /** dialect oracle */
    private static String FLAG_ORACLE = "oracle";
    
    /** 缓存的类与表名的Map */
    private Map<Class<?>, String> clazzTableNameMap = new LinkedHashMap<Class<?>, String>();
    /** 缓存的类与表序列名的Map */
    private Map<Class<?>, String> clazzTableSeqNameMap = new LinkedHashMap<Class<?>, String>();
    /** 缓存的类下字段与数据表中字段名的Map */
    private Map<Class<?>, Map<Field, String>> clazzFieldColumnMap = new LinkedHashMap<Class<?>, Map<Field,String>>();
    /** 缓存的类下字段名与数据表中字段名的Map */
    private Map<Class<?>, Map<String, String>> clazzFieldNameColumnMap = new LinkedHashMap<Class<?>, Map<String,String>>();
    /** 缓存类与表中主键字段名的Map */
    private Map<Class<?>, String> clazzIdColumnMap = new LinkedHashMap<Class<?>, String>();
    /** 缓存类与其主键字段类型对应Map */
    private Map<Class<?>, Class<?>> clazzIdFieldTypeMap = new LinkedHashMap<Class<?>, Class<?>>();
    /** 缓存类与主键字段名对应的Map */
    private Map<Class<?>, String> clazzIdFieldNameMap = new LinkedHashMap<Class<?>, String>();
    
    /** 静态集合缓存已初始化过的sqlSessionTemplate */
    private static Set<SqlSessionTemplate> initedSqlSessionTemplateSet = new HashSet<SqlSessionTemplate>();
    private static Map<SqlSessionTemplate,DbDialectEnum> dbDialectMap = new HashMap<SqlSessionTemplate, DbDialectEnum>();
    private static Map<SqlSessionTemplate,Map<Class<?>, String>> dbClazzTableNameMap = new HashMap<SqlSessionTemplate, Map<Class<?>,String>>();
    private static Map<SqlSessionTemplate,Map<Class<?>, String>> dbClazzTableSeqNameMap = new HashMap<SqlSessionTemplate, Map<Class<?>,String>>();
    private static Map<SqlSessionTemplate,Map<Class<?>, Map<Field, String>>> dbClazzFieldColumnMap = new HashMap<SqlSessionTemplate, Map<Class<?>,Map<Field,String>>>();
    private static Map<SqlSessionTemplate,Map<Class<?>, Map<String, String>>> dbClazzFieldNameColumnMap = new HashMap<SqlSessionTemplate, Map<Class<?>,Map<String,String>>>();
    private static Map<SqlSessionTemplate,Map<Class<?>, String>> dbClazzIdColumnMap = new HashMap<SqlSessionTemplate, Map<Class<?>,String>>();
    private static Map<SqlSessionTemplate,Map<Class<?>, Class<?>>> dbClazzIdFieldTypeMap = new HashMap<SqlSessionTemplate, Map<Class<?>,Class<?>>>();
    private static Map<SqlSessionTemplate,Map<Class<?>, String>> dbClazzIdFieldNameMap = new HashMap<SqlSessionTemplate, Map<Class<?>,String>>();
    
	public SqlSessionTemplate getSqlSessionTemplate() {
		return sqlSessionTemplate;
	}
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		
		// initialize sqlSessionTemplate
		getCurrentSession();
		
		if(!initedSqlSessionTemplateSet.contains(sqlSessionTemplate))
		{
			// initialize database dialect
	    	initDbDialect();
	    	
	    	// initialize default sqlMapper
	    	initDefaultSqlMapper();
	    	
	    	// initialize class mapperTo table
	    	initDbTable();
	    	
			initedSqlSessionTemplateSet.add(sqlSessionTemplate);
			dbDialectMap.put(sqlSessionTemplate, dialect);
			dbClazzTableNameMap.put(sqlSessionTemplate, clazzTableNameMap);
			dbClazzTableSeqNameMap.put(sqlSessionTemplate, clazzTableSeqNameMap);
			dbClazzFieldColumnMap.put(sqlSessionTemplate, clazzFieldColumnMap);
			dbClazzFieldNameColumnMap.put(sqlSessionTemplate, clazzFieldNameColumnMap);
			dbClazzIdColumnMap.put(sqlSessionTemplate, clazzIdColumnMap);
			dbClazzIdFieldTypeMap.put(sqlSessionTemplate, clazzIdFieldTypeMap);
			dbClazzIdFieldNameMap.put(sqlSessionTemplate, clazzIdFieldNameMap);
		}
		else
		{
			dialect = dbDialectMap.get(sqlSessionTemplate);
			clazzTableNameMap = dbClazzTableNameMap.get(sqlSessionTemplate);
			clazzTableSeqNameMap = dbClazzTableSeqNameMap.get(sqlSessionTemplate);
			clazzFieldColumnMap = dbClazzFieldColumnMap.get(sqlSessionTemplate);
			clazzFieldNameColumnMap = dbClazzFieldNameColumnMap.get(sqlSessionTemplate);
			clazzIdColumnMap = dbClazzIdColumnMap.get(sqlSessionTemplate);
			clazzIdFieldTypeMap = dbClazzIdFieldTypeMap.get(sqlSessionTemplate);
			clazzIdFieldNameMap = dbClazzIdFieldNameMap.get(sqlSessionTemplate);
			
		}
	}
	
	protected void initDbDialect() {
		try {
			String url = getCurrentSession().getConfiguration().getDatabaseId().toLowerCase();
			if(url.contains(FLAG_MYSQL))
			{
				dialect = DbDialectEnum.mysql;
			}
			else if(url.contains(FLAG_ORACLE))
			{
				dialect = DbDialectEnum.oracle;
			}
		} catch (Exception e) {
			throw new DaoException("can not get dataSource url",e);
		}
		if(dialect==null)
			throw new DaoException("not supported database dialect");
	}
	
	private void initDefaultSqlMapper() {
		Configuration config = getCurrentSession().getConfiguration();
		Class<?> paramerterType = MapperParameterType.class;
		
		Collection<MappedStatement> mappedStatementCollection = config.getMappedStatements();
		Set<String> idSet = new HashSet<String>();
//		// Mybatis原生的一个小bug，集合中放的数据不完全全为MappedStatement类型
//		for (Object one : mappedStatementCollection) {
//			if(one instanceof MappedStatement)
//			{
//				idSet.add(((MappedStatement)one).getId());
//			}
//		}
		List<Object> list = new ArrayList<Object>(mappedStatementCollection);
		for (int i = 0; i < list.size(); i++) {
			Object one = list.get(i);
			if(one instanceof MappedStatement)
			{
				idSet.add(((MappedStatement)one).getId());
			}
		}

//		SQL_SELECT_COLUMNS
		if(!idSet.contains(getStatement(SQL_SELECT_COLUMNS)))
		{
			Class<?> resultType = String.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_SELECT_COLUMNS_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_SELECT_COLUMNS), sqlSource, SqlCommandType.SELECT)
				.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);	
		}
		
//		SQL_GET_ID
		if(!idSet.contains(getStatement(SQL_GET_ID)))
		{
			Class<?> resultType = Long.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_GET_ID_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_GET_ID), sqlSource, SqlCommandType.SELECT)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
//		SQL_COUNT
		if(!idSet.contains(getStatement(SQL_COUNT)))
		{
			Class<?> resultType = Long.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_COUNT_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_COUNT), sqlSource, SqlCommandType.SELECT)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
//		SQL_INSERT_ONE
		if(!idSet.contains(getStatement(SQL_INSERT_ONE)))
		{
			Class<?> resultType = Integer.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_INSERT_ONE_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_INSERT_ONE), sqlSource, SqlCommandType.INSERT)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
//		SQL_DELETE_ONE
		if(!idSet.contains(getStatement(SQL_DELETE_ONE)))
		{
			Class<?> resultType = Integer.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_DELETE_ONE_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_DELETE_ONE), sqlSource, SqlCommandType.DELETE)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
//		SQL_UPDATE_ONE
		if(!idSet.contains(getStatement(SQL_UPDATE_ONE)))
		{
			Class<?> resultType = Integer.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_UPDATE_ONE_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_UPDATE_ONE), sqlSource, SqlCommandType.UPDATE)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
//		SQL_SELECT_ONE
		if(!idSet.contains(getStatement(SQL_SELECT_ONE)))
		{
			Class<?> resultType = HashMap.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_SELECT_ONE_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_SELECT_ONE), sqlSource, SqlCommandType.SELECT)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
//		SQL_SELECT_LIST
		if(!idSet.contains(getStatement(SQL_SELECT_LIST)))
		{
			Class<?> resultType = HashMap.class;
			SqlSource sqlSource = config.getLanguageRegistry().getDriver(XMLLanguageDriver.class).createSqlSource(config, SQL_SELECT_LIST_VALUE, paramerterType);
			MappedStatement ms = new MappedStatement.Builder(config, getStatement(SQL_SELECT_LIST), sqlSource, SqlCommandType.SELECT)
			.resultMaps(Arrays.asList(new ResultMap.Builder(config,"",resultType,new ArrayList<ResultMapping>()).build())).build();
			config.addMappedStatement(ms);
		}
		
	}
	
	protected void initDbTable() {
		// 待映射的模型model集合
    	Class<TableAnnotaion> tableAnnotationClass = TableAnnotaion.class;
    	Map<String, Object> modelMap = applicationContext.getBeansWithAnnotation(tableAnnotationClass);	
    	Collection<Object> modelCollection = modelMap.values();
        for (Object model : modelCollection) {
			Class<?> modelClass = model.getClass();
			Class<?> realClass = modelClass;
//        	while(true)
//        	{
//        		if(realClass.getSuperclass().equals(BaseModel.class))
//        		{
//        			break;
//        		}
//        		realClass = realClass.getSuperclass();
//        	}
			
        	String classSimpleName = realClass.getSimpleName();
        	
        	TableAnnotaion tableAnnotation = realClass.getAnnotation(tableAnnotationClass);
        	String dbName = tableAnnotation.dbValue();
        	String url = null;
        	DataSource dataSource = getCurrentSession().getConfiguration().getEnvironment().getDataSource();
        	if(dataSource instanceof DruidDataSource)
        	{
        		url = ((DruidDataSource)dataSource).getUrl();
        	}
        	
        	if(CodeUtil.isEmpty(dbName)||(url!=null&&url.contains(dbName)))
        	{
        		
            	String tableName = tableAnnotation.value();
            	tableName = (CodeUtil.isEmpty(tableName))?(classSimpleName.substring(0,1).toLowerCase()+classSimpleName.substring(1)):tableName;
            	clazzTableNameMap.put(realClass, tableName);
            	
            	String seqTableName = tableAnnotation.seqTableName();
            	clazzTableSeqNameMap.put(realClass, seqTableName);
            	
            	Set<String> tableColumnSet = getTableColumnNameSet(tableName);
            	
        		Class<IdAnnotaion> idAnnotationClass = IdAnnotaion.class;
        		Class<ColumnAnnotaion> columnAnnotationClass = ColumnAnnotaion.class;
            	clazzFieldColumnMap.put(realClass, new LinkedHashMap<Field, String>());
            	clazzFieldNameColumnMap.put(realClass, new LinkedHashMap<String, String>());
            	Map<Field, Method> fieldGetMethodMap = ReflectUtil.getFieldAndGetmethodMap(realClass);
            	Set<Field> fieldSet = fieldGetMethodMap.keySet();
            	Field idField = null;
            	String idColumnName = null;
            	for (Field one : fieldSet) {
            		String fieldName = ReflectUtil.getFieldAliasName(one);
            		
            		String columnName = null;
            		if(one.isAnnotationPresent(idAnnotationClass))
            		{
            			IdAnnotaion idAnnotation = one.getAnnotation(idAnnotationClass);
            			columnName = idAnnotation.value();
            			
            			if(CodeUtil.isEmpty(idField,idColumnName))
            			{
            				idField = one;
            				idColumnName = columnName;
            			}
            		}
            		if(one.isAnnotationPresent(columnAnnotationClass))
            		{
            			ColumnAnnotaion columnAnnotation = one.getAnnotation(columnAnnotationClass);
            			columnName = columnAnnotation.value();
            		}
            		columnName = (CodeUtil.isEmpty(columnName))?fieldName:columnName;
            		
            		if(tableColumnSet.contains(columnName)||tableColumnSet.contains(columnName.toLowerCase())||tableColumnSet.contains(columnName.toUpperCase()))
            		{
            			clazzFieldColumnMap.get(realClass).put(one, columnName);
    				
            			clazzFieldNameColumnMap.get(realClass).put(fieldName, columnName);
            		}
    			}
            	
//            	if(clazzFieldNameColumnMap.get(realClass).isEmpty())
//            		throw new DaoException(getClass().getName() + " init class tableName config illegal,clazz="+realClass+",tableName="+tableName);

            	if(!CodeUtil.isEmpty(clazzFieldColumnMap.get(realClass)))
            	{
                	idField = CodeUtil.isEmpty(idField)?(clazzFieldColumnMap.get(realClass).keySet().iterator().next()):idField;
                	clazzIdFieldTypeMap.put(realClass, idField.getType());
                	
                	String idFieldName = ReflectUtil.getFieldAliasName(idField);
                	clazzIdFieldNameMap.put(realClass, idFieldName);
                	
                	idColumnName = clazzFieldNameColumnMap.get(realClass).get(idFieldName);
                	clazzIdColumnMap.put(realClass, idColumnName);
            	}
        	}
        	
		}
        Map<Class<?>, Map<String, String>> validClazzFieldNameColumnMap = new LinkedHashMap<Class<?>, Map<String,String>>();
        Set<Entry<Class<?>, Map<String,String>>> clazzFieldNameColumnEntrySet = clazzFieldNameColumnMap.entrySet();
        for (Entry<Class<?>, Map<String, String>> entry : clazzFieldNameColumnEntrySet) {
			Map<String,String> fieldNameColumnMap = entry.getValue();
        	if(!CodeUtil.isEmpty(fieldNameColumnMap))
			{
				validClazzFieldNameColumnMap.put(entry.getKey(), fieldNameColumnMap);
			}
		}
        LogUtil.i(getClass().getName() + " " + getCurrentSession() + ",init clazzFieldNameColumnMap=" + validClazzFieldNameColumnMap);
    }
	
	protected SqlSession getCurrentSession()
    {
//        ExecutorType executorType = sqlSessionFactory.getConfiguration().getDefaultExecutorType();
//        PersistenceExceptionTranslator exceptionTranslator = new MyBatisExceptionTranslator(
//              sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
//        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionFactory,executorType,exceptionTranslator);
//        if(!SqlSessionUtils.isSqlSessionTransactional(sqlSession, sqlSessionFactory))
//        {
//          throw new DaoException("mybatis sqlSession not wrap with spring transactional.");
//        }
//        try {
//            sqlSessionTemplate.getConnection().setAutoCommit(false);
//        } catch (SQLException e) {
//            throw new DaoException(MybatisDao.class+".getCurrentSession setAutoCommit false illegal",e);
//        }
    	if(sqlSessionTemplate==null)
    	{
    		String sqlSessionTemplateName = getSqlSessionTemplateName();
    		return applicationContext.getBean(sqlSessionTemplateName,SqlSession.class);
    	}
    	return sqlSessionTemplate;
    }
    
    protected String getSqlSessionTemplateName() {
		throw new DaoException(getClass()+" must override getSqlSessionTemplateName");
	}
	
	/** 组装映射mapper中唯一方法 */
    protected String getStatement(String sqlOperateId)
    {
    	String baseNameSpace = MybatisDaoImpl.class.getName();
    	String nameSpace = this.getClass().getName();
    	if(SQL_GET_ID.equals(sqlOperateId)||SQL_INSERT_ONE.equals(sqlOperateId)
    			||SQL_UPDATE_ONE.equals(sqlOperateId)||SQL_DELETE_ONE.equals(sqlOperateId)
    			||SQL_SELECT_ONE.equals(sqlOperateId)||SQL_SELECT_LIST.equals(sqlOperateId)
    			||SQL_SELECT_COLUMNS.equals(sqlOperateId)||SQL_COUNT.equals(sqlOperateId))
    	{
//    		if(!nameSpace.equals(baseNameSpace))
//    		{
//    			LogUtil.w("sqlOperateId="+sqlOperateId+" exist in baseNameSpace="+baseNameSpace+", if you not overwrite the base mapper then it not effect",null);
//    		}
    		nameSpace = baseNameSpace;
    	}
        return nameSpace + SQL_STATEMENT_SEPARATOR + sqlOperateId;
    }
	
	private Set<String> getTableColumnNameSet(String tableName) {
		
		MapperParameterType parameterType = new MapperParameterType();
		parameterType.setTableName(tableName);
		parameterType.setDialect(dialect.name());
		
		List<String> list = getCurrentSession().selectList(getStatement(SQL_SELECT_COLUMNS), parameterType);
		return new HashSet<String>(list);
	}
    
    private Map<String, String> getColumnFieldNameMap(Map<String, String> fieldNameColumnMap) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if(!CodeUtil.isEmpty(fieldNameColumnMap))
        {
            Set<Entry<String, String>> entrySet = fieldNameColumnMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
                map.put(entry.getValue(), entry.getKey());
            }
        }
        return map;
    }
    
    private Map<String, Object> getResultFieldNameMap(Map<String, String> columnFieldNameMap,
            Map<String, Object> resultMap) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        if(!CodeUtil.isEmpty(resultMap))
        {
            Set<Entry<String, Object>> entrySet = resultMap.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                String columnName = entry.getKey();
                String fieldName = columnFieldNameMap.get(columnName);
                if(!CodeUtil.isEmpty(fieldName))
                {
                    map.put(fieldName, entry.getValue());
                }
            }
        }
        return map;
    }
    
    private DaoQueryCondition[] transferFieldNameToColumnConditionArray(Map<String, String> fieldNameColumnMap,
            DaoQueryCondition[] conditionArray) {
        DaoQueryCondition[] realConditionArray;
        if(CodeUtil.isEmpty(new Object[]{conditionArray}))
        {
            realConditionArray = null;
        }
        else
        {
            int length = conditionArray.length;
            realConditionArray = new DaoQueryCondition[length];
            for (int i = 0; i < length; i++) {
                DaoQueryCondition condition = conditionArray[i];
                String fieldName = condition.getFieldName();
                String columnName = fieldNameColumnMap.get(fieldName);
                Object fieldValue = condition.getFieldValue();
                if(fieldValue==null)
                {
                }
                else if(fieldValue.getClass().isArray())
                {
                    Object[] array = (Object[]) fieldValue;
                    fieldValue = Arrays.asList(array);
                }
                
                if(fieldValue instanceof Collection)
                {
                    @SuppressWarnings("unchecked")
                    Collection<? extends Object> collection = (Collection<? extends Object>) fieldValue;
                    List<? extends Object> list = new ArrayList<Object>(new HashSet<Object>(collection));
                    fieldValue = SQL_ARRAY_LEFT_TAG + SQL_VALUE_QUOTE_TAG + StringUtil.listToString(list, SQL_VALUE_QUOTE_TAG+SQL_ARRAY_SEPARATOR_TAG+SQL_VALUE_QUOTE_TAG) + SQL_VALUE_QUOTE_TAG + SQL_ARRAY_RIGHT_TAG;
                }
                else
                {
                    fieldValue = fieldValue==null?"NULL":(SQL_VALUE_QUOTE_TAG + StringUtil.toString(fieldValue) + SQL_VALUE_QUOTE_TAG);
                }
                realConditionArray[i] = new DaoQueryCondition(columnName, condition.getOperator(), fieldValue);
            }
        }
        
        return realConditionArray;
    }
    
//    public void init() {
//    	
//    	Class<TableAnnotaion> tableAnnotationClass = TableAnnotaion.class;
//    	Map<String, Object> modelMap = applicationContext.getBeansWithAnnotation(tableAnnotationClass);	
//    	Collection<Object> modelCollection = modelMap.values();
//        for (Object model : modelCollection) {
//			Class<?> modelClass = model.getClass();
//			Class<?> realClass = modelClass;
////        	while(true)
////        	{
////        		if(realClass.getSuperclass().equals(BaseModel.class))
////        		{
////        			break;
////        		}
////        		realClass = realClass.getSuperclass();
////        	}
//        	
//        	String classSimpleName = realClass.getSimpleName();
//        	
//        	TableAnnotaion tableAnnotation = realClass.getAnnotation(tableAnnotationClass);
//        	String tableName = tableAnnotation.value();
//        	tableName = (CodeUtil.isEmpty(tableName))?(classSimpleName.substring(0,1).toLowerCase()+classSimpleName.substring(1)):tableName;
//        	clazzTableNameMap.put(realClass, tableName);
//        	
//        	String seqTableName = tableAnnotation.seqTableName();
//        	clazzTableSeqNameMap.put(realClass, seqTableName);
//        	
//        	Set<String> tableColumnSet = getTableColumnNameSet(tableName);
//        	
//    		Class<IdAnnotaion> idAnnotationClass = IdAnnotaion.class;
//    		Class<ColumnAnnotaion> columnAnnotationClass = ColumnAnnotaion.class;
//        	clazzFieldColumnMap.put(realClass, new HashMap<Field, String>());
//        	clazzFieldNameColumnMap.put(realClass, new HashMap<String, String>());
//        	Map<Field, Method> fieldGetMethodMap = ReflectUtil.getFieldAndGetmethodMap(realClass);
//        	Set<Field> fieldSet = fieldGetMethodMap.keySet();
//        	Field idField = null;
//        	String idColumnName = null;
//        	for (Field one : fieldSet) {
//        		String fieldName = ReflectUtil.getFieldAliasName(one);
//        		
//        		String columnName = null;
//        		if(one.isAnnotationPresent(idAnnotationClass))
//        		{
//        			IdAnnotaion idAnnotation = one.getAnnotation(idAnnotationClass);
//        			columnName = idAnnotation.value();
//        			
//        			if(CodeUtil.isEmpty(idField,idColumnName))
//        			{
//        				idField = one;
//        				idColumnName = columnName;
//        			}
//        		}
//        		if(one.isAnnotationPresent(columnAnnotationClass))
//        		{
//        			ColumnAnnotaion columnAnnotation = one.getAnnotation(columnAnnotationClass);
//        			columnName = columnAnnotation.value();
//        		}
//        		columnName = (CodeUtil.isEmpty(columnName))?fieldName:columnName;
//        		
//        		if(tableColumnSet.contains(columnName)||tableColumnSet.contains(columnName.toLowerCase())||tableColumnSet.contains(columnName.toUpperCase()))
//        		{
//        			clazzFieldColumnMap.get(realClass).put(one, columnName);
//				
//        			clazzFieldNameColumnMap.get(realClass).put(fieldName, columnName);
//        		}
//			}
//        	
//        	if(clazzFieldNameColumnMap.get(realClass).isEmpty())
//        		throw new DaoException(getClass().getName() + " init class tableName config illegal,clazz="+realClass+",tableName="+tableName);
//        	
//        	idField = CodeUtil.isEmpty(idField)?(fieldSet.iterator().next()):idField;
//        	clazzIdFieldTypeMap.put(realClass, idField.getType());
//        	
//        	String idFieldName = ReflectUtil.getFieldAliasName(idField);
//        	clazzIdFieldNameMap.put(realClass, idFieldName);
//        	
//        	idColumnName = CodeUtil.isEmpty(idColumnName)?(idField.getName()):idColumnName;
//        	clazzIdColumnMap.put(realClass, idColumnName);
//		}
//        LogUtil.i(getClass().getName() + " init tableNameCollection=" + clazzTableNameMap.values());
//    }
	
	@Override
    public <T> T insertOne(T one)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".insert param empty,one="+one,new Object[]{one});
        
        Class<?> clazz = one.getClass();
        String tableName = clazzTableNameMap.get(clazz);
        String tableSeqName = clazzTableSeqNameMap.get(clazz);
        String idFieldName = clazzIdFieldNameMap.get(clazz);
        Class<?> idFieldType = clazzIdFieldTypeMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setTableSeqName(tableSeqName);
        parameterType.setDialect(dialect.name());
        if(DbDialectEnum.oracle.equals(dialect))
        {
            Object id = getCurrentSession().selectOne(getStatement(SQL_GET_ID), parameterType);
            if(!CodeUtil.isEmpty(id)&&idFieldType!=null)
            {
                Object idFieldValue = StringUtil.getValueFromString(id.toString(), idFieldType);
                ReflectUtil.setMethodInvoke(one, idFieldName, idFieldValue);
            }
        }
        
        // 获取所有的columnName value map,包括fieldName中存在Obj1.field1的value与column1
        Map<String, Object> columnValueMap = getColumnValueMap(one, fieldNameColumnMap);
        // 过滤去掉 value=null
        Map<String, Object> insertColumnValueMap = getInsertColumnValueMap(columnValueMap);
        List<String> columnList = new ArrayList<String>(insertColumnValueMap.keySet());
        String[] columnArray = columnList.toArray(new String[]{});
        Object[] valueArray = new ArrayList<Object>(insertColumnValueMap.values()).toArray(new Object[]{});
        parameterType.setColumnArray(columnArray);
        parameterType.setValueArray(valueArray);
        parameterType.setTableName(tableName);
        
        getCurrentSession().insert(getStatement( SQL_INSERT_ONE), parameterType);
        
        if(DbDialectEnum.mysql.equals(dialect))
        {
            Long id = getCurrentSession().selectOne(getStatement( SQL_GET_ID), parameterType);
            if(!CodeUtil.isEmpty(id)&&idFieldType!=null)
            {
                Object idFieldValue = StringUtil.getValueFromString(id.toString(), idFieldType);
                ReflectUtil.setMethodInvoke(one, idFieldName, idFieldValue);
            }
        }
        
//        Long id = parameterType.getId();
//        if(!CodeUtil.isEmpty(id)&&idFieldType!=null)
//        {
//            Object idFieldValue = StringUtil.getValueFromString(id.toString(), idFieldType);
//            ReflectUtil.setMethodInvoke(one, idFieldName, idFieldValue);
//        }
        
        return one;
    }
    
    public int insert(String sqlOperateId, Object parameter)
    {
    	return getCurrentSession().insert(getStatement(sqlOperateId), parameter);
    }
    
	public int insert(Object t) {
		return insert("insert", t);
	}

    @Override
    public <T> T updateOne(T one)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".update param empty,object="+one,new Object[]{one});
        
        Class<?> clazz = one.getClass();
        String tableName = clazzTableNameMap.get(clazz);
        String idColumn = clazzIdColumnMap.get(clazz);
        String idFieldName = clazzIdFieldNameMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        Map<String, Object> columnValueMap = getColumnValueMap(one, fieldNameColumnMap);
        MapperParameterType.ColumnValue[] columnValueArray = getUpdateColumnValueArray(columnValueMap);
        Object idValue = ReflectUtil.getMethodInvoke(one, idFieldName);
        DaoQueryCondition[] queryConditionArray = new DaoQueryCondition[]{new DaoQueryCondition(idColumn, DaoQueryOperator.EQ, StringUtil.toString(idValue))};
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setColumnValueArray(columnValueArray);
        parameterType.setConditionArray(queryConditionArray);
        parameterType.setTableName(tableName);
        
        getCurrentSession().update(getStatement( SQL_UPDATE_ONE), parameterType);
        return one;
    }
    
    public int update(String sqlOperateId, Object parameter)
    {
    	return getCurrentSession().update(getStatement(sqlOperateId), parameter);
    }
    
	public int update(Object t) {
		return update("update",t);
	}
    
    public int update(Map<String, Object> fieldValueMap, Class<?> clazz ,DaoQueryCondition... conditionArray)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".update param empty,fieldValueMap="+fieldValueMap+",clazz="+clazz, new Object[]{fieldValueMap,clazz});
        
        String tableName = clazzTableNameMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        Map<String, Object> columnValueMap = getColumnValueMapFromMap(fieldValueMap,fieldNameColumnMap);
        MapperParameterType.ColumnValue[] columnValueArray = getUpdateColumnValueArray(columnValueMap);
        DaoQueryCondition[] realConditionArray = transferFieldNameToColumnConditionArray(fieldNameColumnMap, conditionArray);
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setColumnValueArray(columnValueArray);
        parameterType.setConditionArray(realConditionArray);
        parameterType.setTableName(tableName);
        
        return getCurrentSession().update(getStatement( SQL_UPDATE_ONE), parameterType);
    }
    
    @Override
    public <T> int deleteOne(Class<T> clazz, Object idValue)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".delete param empty,clazz="+clazz+",idValue="+idValue,new Object[]{clazz,idValue});
        
        String tableName = clazzTableNameMap.get(clazz);
        String idColumn = clazzIdColumnMap.get(clazz);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        DaoQueryCondition[] queryConditionArray = new DaoQueryCondition[]{new DaoQueryCondition(idColumn, DaoQueryOperator.EQ, StringUtil.toString(idValue))};
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setConditionArray(queryConditionArray);
        parameterType.setTableName(tableName);
        
        return getCurrentSession().delete(getStatement( SQL_DELETE_ONE), parameterType);
    }
    
    public <T> int deleteList(Class<T> clazz, DaoQueryCondition... conditionArray)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".deleteList param empty,clazz="+clazz+",conditionArray="+conditionArray,new Object[]{clazz,conditionArray});
        
        String tableName = clazzTableNameMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        DaoQueryCondition[] realConditionArray = transferFieldNameToColumnConditionArray(fieldNameColumnMap, conditionArray);
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setConditionArray(realConditionArray);
        parameterType.setTableName(tableName);
        
        return getCurrentSession().delete(getStatement( SQL_DELETE_ONE), parameterType);
    }
    
    public int delete(String sqlOperateId, Object parameter)
    {
    	return getCurrentSession().delete(getStatement(sqlOperateId), parameter);
    }
    
	public int delete(Object t){
		return delete("delete", t);
	}
    
    @Override
    public <T> long count(String resultFieldName,boolean distinct, Class<T> clazz, DaoQueryCondition... conditionArray)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".count param empty,clazz="+clazz,new Object[]{clazz});
        
        String tableName = clazzTableNameMap.get(clazz);
        String idColumn = clazzIdColumnMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        String distictString = distinct?SQL_DISTINCT:"";    
        String[] columnArray = getCountColumns(idColumn,resultFieldName,clazz,fieldNameColumnMap);
        DaoQueryCondition[] realConditionArray = transferFieldNameToColumnConditionArray(fieldNameColumnMap, conditionArray);
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setDistinct(distictString);
        parameterType.setColumnArray(columnArray);
        parameterType.setConditionArray(realConditionArray);
        parameterType.setTableName(tableName);
        
        Long result = getCurrentSession().selectOne(getStatement( SQL_COUNT), parameterType);
        return result==null?0L:result;
    }
    
	public Long countListPage(Object t) {
		return selectOne("countListPage", t);
	}
    
    @Override
    public <T> T get(Class<T> clazz, Object idValue)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".get param empty,clazz="+clazz+",idValue="+idValue,new Object[]{clazz,idValue});
        
        String tableName = clazzTableNameMap.get(clazz);
        String idColumn = clazzIdColumnMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        Map<String, String> columnFieldNameMap = getColumnFieldNameMap(fieldNameColumnMap);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        String[] columnArray = new ArrayList<String>(fieldNameColumnMap.values()).toArray(new String[]{});
        DaoQueryCondition[] queryConditionArray = new DaoQueryCondition[]{new DaoQueryCondition(idColumn, DaoQueryOperator.EQ, StringUtil.toString(idValue))};
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setColumnArray(columnArray);
        parameterType.setConditionArray(queryConditionArray);
        parameterType.setTableName(tableName);

        // simple field type
        Map<String, Object> resultMap = getCurrentSession().selectOne(getStatement(SQL_SELECT_ONE), parameterType);
        Map<String, Object> resultFieldNameMap = getResultFieldNameMap(columnFieldNameMap,resultMap);
        
//        // relation field
//        initRelationField(clazz,resultFieldNameMap);
        
        return ReflectUtil.mapToObject(resultFieldNameMap, clazz);
    }

//    /**
//     * 获取记录，根据主键获取数据记录
//     * @param clazz 要获取的数据类型
//     * @param DaoQueryCondition 要获取的数据的条件
//     * @return 指定类型指定主键的数据记录
//     */
//    private <T> T getNoRelated(Class<T> clazz, DaoQueryCondition condition)
//    {
//        CodeUtil.emptyCheck(null,getClass().getName()+".get param empty,clazz="+clazz+",condition="+condition,new Object[]{clazz,condition});
//        
//        String tableName = clazzTableNameMap.get(clazz);
//        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
//        CodeUtil.emptyCheck(null,getClass().getName()+".get tableName config illegal,tableName="+tableName+",fieldNameColumnMap="+fieldNameColumnMap+",in"+MYBATIS_MAPPER_CONFIG_FILE_NAME, new Object[]{tableName,fieldNameColumnMap});
//        
//        String columns = getQueryColumns(fieldNameColumnMap);
//        DaoQueryCondition[] queryConditionArray = transferFieldNameToColumnConditionArray(fieldNameColumnMap, new DaoQueryCondition[]{condition});
//        MapperParameterType parameterType = new MapperParameterType();
//    	  parameterType.setDialect(dialect.name());
//        parameterType.setColumns(columns);
//        parameterType.setConditionArray(queryConditionArray);
//        parameterType.setTableName(tableName);
//
//        // simple field type
//        Map<String, ?> resultMap = getCurrentSession().selectOne(getStatement( SQL_GET), parameterType);
//        
//        return ReflectUtil.mapToObject(resultMap, clazz);
//    }
    
    @Override
    public <R,T> R selectOne(String[] resultFieldName, Class<T> clazz, DaoQueryCondition... conditionArray)
    {
//        List<?> list = selectList(resultFieldName, true, clazz,1,1, conditionArray);
//        if(list.isEmpty())
//            return null;
//        return list.get(0);
    	
        CodeUtil.emptyCheck(null,getClass().getName()+".selectOne param empty,clazz="+clazz,new Object[]{clazz});
        
        String tableName = clazzTableNameMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        Map<String, String> columnFieldNameMap = getColumnFieldNameMap(fieldNameColumnMap);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        String[] columnArray = getQueryColumns(resultFieldName,clazz,fieldNameColumnMap);
        DaoQueryCondition[] realConditionArray = transferFieldNameToColumnConditionArray(fieldNameColumnMap, conditionArray);
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setColumnArray(columnArray);
        parameterType.setConditionArray(realConditionArray);
        parameterType.setTableName(tableName);

        // simple field type
        Map<String, Object> resultMap = getCurrentSession().selectOne(getStatement(SQL_SELECT_ONE), parameterType);
        Map<String, Object> resultFieldNameMap = getResultFieldNameMap(columnFieldNameMap,resultMap);
        
        R result = null;
        if(resultFieldName!=null&&resultFieldName.length==1)
        {
            String fieldName = resultFieldName[0];
            // relation field
            if(!CodeUtil.isEmpty(resultFieldNameMap))
            {
//            	initRelationField(clazz,resultFieldNameMap);
                
            	@SuppressWarnings("unchecked")
            	R r = (R) resultFieldNameMap.get(fieldName);
            	result = r;
            }
        }
        else
        {
            // relation field
        	if(!CodeUtil.isEmpty(resultFieldNameMap))
            {
//            	initRelationField(clazz,resultFieldMap);
                
            	@SuppressWarnings("unchecked")
				R r = (R) ReflectUtil.mapToObject(resultFieldNameMap, clazz);
                result = r;
            }
        }
        
        return result;
    }
    
    public <T> T selectOne(String sqlOperateId, Object parameter)
    {
    	return getCurrentSession().selectOne(getStatement(sqlOperateId), parameter);
    }
    
	public <T> T queryOne(T condition){
		return selectOne("queryOne", condition);
	}
    
    @Override
    public <R,T> List<R> selectList(String[] resultFieldName, boolean distinct, Class<T> clazz,Integer pageNo, Integer pageSize, DaoQueryCondition... conditionArray)
    {
        CodeUtil.emptyCheck(null,getClass().getName()+".selectList param empty,clazz="+clazz,new Object[]{clazz});
        
        String tableName = clazzTableNameMap.get(clazz);
        Map<String, String> fieldNameColumnMap = clazzFieldNameColumnMap.get(clazz);
        Map<String, String> columnFieldNameMap = getColumnFieldNameMap(fieldNameColumnMap);
        CodeUtil.emptyCheck(null,clazz+" should decorator by spring and " + TableAnnotaion.class,new Object[]{tableName});

        String distictString = distinct?SQL_DISTINCT:"";
        String[] columnArray = getQueryColumns(resultFieldName,clazz,fieldNameColumnMap);
        DaoQueryCondition[] realConditionArray = transferFieldNameToColumnConditionArray(fieldNameColumnMap, conditionArray);
        MapperParameterType parameterType = new MapperParameterType();
        parameterType.setDialect(dialect.name());
        parameterType.setDistinct(distictString);
        parameterType.setColumnArray(columnArray);
        parameterType.setConditionArray(realConditionArray);
        parameterType.setTableName(tableName);
        
        List<Map<String, Object>> resultMapList;
        if(pageNo==null||pageNo<1||pageSize==null||pageSize<0)
        {
            resultMapList = getCurrentSession().selectList(getStatement(SQL_SELECT_LIST), parameterType);
        }
        else
        {
            resultMapList = getCurrentSession().selectList(getStatement(SQL_SELECT_LIST), parameterType, new RowBounds((pageNo-1)*pageSize, pageSize));
        }
        
        List<R> result;
        if(resultFieldName!=null&&resultFieldName.length==1)
        {
            String fieldName = resultFieldName[0];
            result = new ArrayList<R>();
            for (Map<String, Object> one : resultMapList) {

                Map<String, Object> resultFieldMap = getResultFieldNameMap(columnFieldNameMap, one);
                
                // relation field
                if(!CodeUtil.isEmpty(resultFieldMap))
                {
//                    initRelationField(clazz,resultFieldMap);
                	
                	@SuppressWarnings("unchecked")
					R r = (R) resultFieldMap.get(fieldName);
                    result.add(r);
                }
            }
        }
        else
        {
            result = new ArrayList<R>();
            for (Map<String, Object> one : resultMapList) {
                
                Map<String, Object> resultFieldMap = getResultFieldNameMap(columnFieldNameMap, one);
                
                // relation field
                if(!CodeUtil.isEmpty(resultFieldMap))
                {
//                    initRelationField(clazz,resultFieldMap);
                
                	@SuppressWarnings("unchecked")
					R r = (R) ReflectUtil.mapToObject(resultFieldMap, clazz);
                    result.add(r);
                }
            }
        }
        
        return result;
    }
    
    public <T> List<T> selectList(String sqlOperateId, Object parameter, Integer pageNo, Integer pageSize)
    {
    	if(CodeUtil.isEmpty(pageNo,pageSize))
        {
    		return getCurrentSession().selectList(getStatement(sqlOperateId), parameter);
        }
        else
        {
        	return getCurrentSession().selectList(getStatement(sqlOperateId), parameter, new RowBounds((pageNo-1)*pageSize, pageSize));
        }
    }
    
	public <T> List<T> queryListPage(T t, Integer pageNo, Integer pageSize) {
		return selectList("queryListPage", t, pageNo, pageSize);
	}
    
	/** 获取columnValue map */
    private Map<String, Object> getColumnValueMap(Object obj, Map<String, String> fieldNameColumnMap) {
        Map<String, Object> columnValueMap = new LinkedHashMap<String, Object>();
        
        Set<Entry<String, String>> entrySet = fieldNameColumnMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            String column = entry.getValue();
            if(column!=null)
            {
                String fieldName = entry.getKey();
                Object fieldValue = getFieldValue(obj,FIELD_NAME_SEPARATOR_TAG,fieldName);
                columnValueMap.put(column, fieldValue);
            }
        }
        
        return columnValueMap;
    }
    
    /** 获取columnValue map */
    private Map<String, Object> getColumnValueMapFromMap(Map<String, Object> map, Map<String, String> fieldNameColumnMap) {
        Map<String, Object> columnValueMap = new LinkedHashMap<String, Object>();
        
        Set<Entry<String, String>> entrySet = fieldNameColumnMap.entrySet();
        for (Entry<String, String> entry : entrySet) {
            String fieldName = entry.getKey();
            String column = entry.getValue();
            if(column!=null&&map.containsKey(fieldName))
            {
                Object fieldValue = getFieldValueFromMap(map,FIELD_NAME_SEPARATOR_TAG,fieldName);
                columnValueMap.put(column, fieldValue);
            }
        }
        
        return columnValueMap;
    }
    
    /** 获取插入的key Value Map */
    private Map<String, Object> getInsertColumnValueMap(Map<String, Object> columnValueMap) {
        
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        Set<Entry<String, Object>> entrySet = columnValueMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if(value!=null)
            {
                result.put(key, value);
            }
        }
        
        return result;
    }
    
    /** 获取更新的key Value Map */
    private MapperParameterType.ColumnValue[] getUpdateColumnValueArray(Map<String, Object> columnValueMap) {
        List<MapperParameterType.ColumnValue> list = new ArrayList<MapperParameterType.ColumnValue>();
        Set<Entry<String, Object>> entrySet = columnValueMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            String key = entry.getKey();
            Object value = entry.getValue();
            MapperParameterType.ColumnValue columnValue = new MapperParameterType.ColumnValue();
            columnValue.setColumnName(key);
            columnValue.setFieldValue(value);
            
            list.add(columnValue);
        }
        
        return list.toArray(new MapperParameterType.ColumnValue[]{});
    }
    
    /** 获取一个对象指定key的值 */
    private Object getFieldValue(Object obj,String fieldNameSeparatorTag, String fieldName) {
        int index = fieldName.indexOf(fieldNameSeparatorTag);
        if(index<0)
        {
            return ReflectUtil.getMethodInvoke(obj, fieldName);
        }
        else
        {
            String fieldName1 = fieldName.substring(0,index);
            Object fieldName1Value = ReflectUtil.getMethodInvoke(obj, fieldName1);
            if(fieldName1Value!=null)
            {
                String fieldName2 = fieldName.substring(index+fieldNameSeparatorTag.length());
                return getFieldValue(fieldName1Value, fieldNameSeparatorTag, fieldName2);
            }
            return null;
        }
    }
    
    /** 获取一个对象指定key的值 */
    private Object getFieldValueFromMap(Map<String,Object> map,String fieldNameSeparatorTag, String fieldName) {
        int index = fieldName.indexOf(fieldNameSeparatorTag);
        if(index<0)
        {
            return map.get(fieldName);
        }
        else
        {
            String fieldName1 = fieldName.substring(0,index);
            Object fieldName1Value = map.get(fieldName1);
            if(fieldName1Value!=null&&fieldName1Value instanceof Map)
            {
                @SuppressWarnings("unchecked")
                Map<String, Object> map1 = (Map<String, Object>)fieldName1Value;
                String fieldName2 = fieldName.substring(index+fieldNameSeparatorTag.length());
                return getFieldValue(map1, fieldNameSeparatorTag, fieldName2);
            }
            return null;
        }
    }
    
    private String[] getQueryColumns(String[] resultFieldName,Class<?> clazz,Map<String,String> fieldNameColumnMap) {
        
    	List<String> resultColumnList = new ArrayList<String>();
        if(CodeUtil.isEmpty(new Object[]{resultFieldName}))
        {
            resultColumnList.addAll(fieldNameColumnMap.values());
        }
        else
        {
            for (String one : resultFieldName) {
				if(!fieldNameColumnMap.containsKey(one))
				{
					throw new DaoException(getClass().getName()+".getQueryColumns fieldName not exist in class "+clazz.getName()+",fieldName="+one);
				}
				resultColumnList.add(fieldNameColumnMap.get(one));
			}
        }
        return resultColumnList.toArray(new String[]{});
    }
    
    private String[] getCountColumns(String idCoumn,String resultFieldName,Class<?> clazz, Map<String, String> fieldNameColumnMap) {
      
        if(CodeUtil.isEmpty(resultFieldName))
            return new String[]{idCoumn};
        
        String columnName = fieldNameColumnMap.get(resultFieldName);
        CodeUtil.emptyCheck(getClass().getName()+".getCountColumns fieldName not exist in class "+clazz.getName()+",fieldName="+resultFieldName,new Object[]{columnName});
        
        return new String[]{columnName};
    }
    
//    /** 初始化resultMap中key为关联的类型 */
//    private void initRelationField(Class<?> clazz, Map<String, Object> resultMap) {
//        
//        Map<String, Method> fieldNameGetMethodMap = ReflectUtil.getFieldNameAndGetmethodMap(clazz);
//        String fieldNameSeparatorTag = FIELD_NAME_SEPARATOR_TAG;
//        
//        Set<String> resultFieldNameSet = resultMap.keySet();
//        for (String fieldName : resultFieldNameSet) {
//            int index = fieldName.indexOf(fieldNameSeparatorTag);
//            if(index<0)
//            {
//                // do nothing
//            }
//            else
//            {
//                Object oldFieldValue = resultMap.get(fieldName);
//                String fieldName1 = fieldName.substring(0,index);
//                Method getMethod = fieldNameGetMethodMap.get(fieldName1);
//                CodeUtil.emptyCheck(null,getClass().getName()+".initRelationField fieldName getMethod not exist,fieldName="+fieldName1, new Object[]{getMethod});
//                String fieldName2 = fieldName.substring(index+fieldNameSeparatorTag.length());
//                Object newFieldValue = getNoRelated(getMethod.getReturnType(), new DaoQueryCondition(fieldName2, DaoQueryOperator.EQ, StringUtil.toString(oldFieldValue)));
//                
//                resultMap.put(fieldName, newFieldValue);
//            }
//            
//        }
//    }
}
