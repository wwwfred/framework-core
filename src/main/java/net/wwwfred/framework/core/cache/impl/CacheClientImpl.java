package net.wwwfred.framework.core.cache.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.wwwfred.framework.core.cache.CacheClient;
import net.wwwfred.framework.core.cache.RedisCache;
import net.wwwfred.framework.core.exception.TeshehuiRuntimeException;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.log.LogUtil;
import net.wwwfred.framework.util.reflect.AliasAnnotation;
import net.wwwfred.framework.util.reflect.ReflectUtil;
import net.wwwfred.framework.util.string.StringUtil;

public class CacheClientImpl implements CacheClient{
    
    /** idMap cacheKey */
    private String idMap = "idMap";
    /** id fieldName cacheKey */
    private String id = "id";
    /** fieldName分隔符 */
    private String fieldNameSeparatorTag = ",";
    
    /** redis客户端 */
    private RedisCache redisCache;
    
    /** 获取clazz映射的别名 */
    protected String getTableName(Class<?> clazz)
    {
        String tableName = null;
        Class<AliasAnnotation> annotationClass = AliasAnnotation.class;
        if(clazz.isAnnotationPresent(annotationClass))
        {
            tableName = clazz.getAnnotation(annotationClass).value();
        }
        if(CodeUtil.isEmpty(tableName))
        {
            tableName = clazz.getName();
        }
        return tableName;
    }
    
    /** 获取唯一标识的字段 */
    protected Field getIdField(Class<?> clazz) {
        Class<CacheModelIdAnnotation> annotationClass = CacheModelIdAnnotation.class;
        Map<Field, Method> fieldGetMethodMap = ReflectUtil.getFieldAndGetmethodMap(clazz);
        Set<Entry<Field, Method>> entrySet = fieldGetMethodMap.entrySet();
        for (Entry<Field, Method> entry : entrySet) {
            Field field = entry.getKey();
            if(field.isAnnotationPresent(annotationClass))
            {
                return field;
            }
        }
        return null;
    }
    
    /** 获取类唯一标识的字段类型 */
    protected Class<?> getIdFieldType(Class<?> clazz) {
        Field field = getIdField(clazz);
        if(field!=null)
        {
            return field.getType();
        }
        return null;
    }
    
    /** 获取类唯一标识的字段名 */
    protected String getIdFieldName(Class<?> clazz)
    {
        Field field = getIdField(clazz);
        if(field!=null)
        {
            return field.getName();
        }
        return null;
    }
    
    /** 获取类唯一标识的字段别名 */
    protected String getIdFieldAliasName(Class<?> clazz)
    {
        Field field = getIdField(clazz);
        if(field!=null)
        {
            return ReflectUtil.getFieldGetMethodAliasName(clazz, field);
        }
        return null;
    }
    
    /** 获取类中字段映射的别名 */
    protected String getColumnName(Class<?> clazz, String fieldName) {
        String result = null;

        Map<Field, Method> fieldGetMethodMap = ReflectUtil.getFieldAndGetmethodMap(clazz);
        Set<Entry<Field, Method>> entrySet = fieldGetMethodMap.entrySet();
        for (Entry<Field, Method> entry : entrySet) {
            Field field = entry.getKey();
            if (field.getName().equals(fieldName)) {
                result = ReflectUtil.getFieldGetMethodAliasName(clazz, field);
                break;
            }
        }
        if (CodeUtil.isEmpty(result)) {
            result = fieldName;
        }
        
        return result;
    }
    
    /** 获取单个对象的Key */
    protected String getIdCacheKey(Class<?> clazz, Long idValue)
    {
        CodeUtil.emptyCheck(null,"getIdCacheKey clazz="+clazz+",id="+idValue, new Object[]{clazz,idValue});
        return getTableName(clazz) + "." + id + idValue;
    }
    
    /** 获取所有对象的ID的Key */
    protected String getIdMapCacheKey(Class<?> clazz)
    {
        CodeUtil.emptyCheck(null,"getIdArrayCacheKey clazz="+clazz, new Object[]{clazz});
        return getTableName(clazz) + "." + idMap;
    }
    
    /** 获取单个对象中某个索引的Key */
    protected String getCacheKey(Object o, String indexFieldName)
    {
        CodeUtil.emptyCheck(null,"getCacheKey o="+o+",indexName="+indexFieldName, new Object[]{o,indexFieldName});
        Class<?> clazz = o.getClass();
        StringBuffer sb = new StringBuffer(getTableName(clazz));
        List<String> list = StringUtil.stringToList(indexFieldName, fieldNameSeparatorTag);
        for (String one : list) {
            String columnName = getColumnName(clazz,one);
            sb.append(".").append(columnName).append(ReflectUtil.getMethodInvoke(o, one));
        }
        return sb.toString();
    }
    
    protected String[] getFieldNameIndexArray(Class<?> clazz)
    {
        Class<CacheModelIndexAnnotation> annotationClass = CacheModelIndexAnnotation.class;
        if(clazz.isAnnotationPresent(annotationClass))
        {
            return clazz.getAnnotation(annotationClass).value();
        }
        return new String[]{};
    }
    protected List<String> getFieldNameIndexList(String... fieldNameArray)
    {
        List<String> result = new ArrayList<String>();
        int length = fieldNameArray.length;
        for (int i = length-1; i >=0; i--) {
            StringBuffer sb = new StringBuffer();
            for(int j=0; j<=i; j++)
            {
                sb.append(fieldNameArray[j]).append(fieldNameSeparatorTag);
            }
            String s = sb.toString();
            if(s.endsWith(fieldNameSeparatorTag))
            {
                s = s.substring(0,s.lastIndexOf(fieldNameSeparatorTag));
            }
            result.add(s);
        }
        return result;
    }
    protected String searchFieldNameIndex(String[] fieldNameIndexArray,List<String> fieldNameIndexList)
    {
        for (String one : fieldNameIndexArray) {
            for (String one2 : fieldNameIndexList) {
                if(one.equals(one2))
                    return one;
            }
        }    
        return null;
    }

    @Override
    public Long add(Object o)
    {
        long startTime = System.currentTimeMillis();
        CodeUtil.emptyCheck(null,"add one="+o, new Object[]{o});
        
        Long newId;
        Map<String, Object> oldMap = new LinkedHashMap<String, Object>();
        Map<String, Object> updateMap = new LinkedHashMap<String, Object>();
        try {
            // add idMap
            Class<?> clazz = o.getClass();
            String idMapCacheKey = getIdMapCacheKey(clazz);
            @SuppressWarnings("unchecked")
            Map<Long, Boolean> idMap = (Map<Long,Boolean>) redisCache.getObject(idMapCacheKey);
            oldMap.put(idMapCacheKey, idMap==null?null:new LinkedHashMap<Long, Boolean>(idMap));
            if(CodeUtil.isEmpty(new Object[]{idMap}))
            {
               idMap = new LinkedHashMap<Long, Boolean>();
            }
            int length = idMap.size();
            newId = length>0?new ArrayList<Long>(idMap.keySet()).get(length-1)+1L:1L;
            idMap.put(newId,false);
            updateMap.put(idMapCacheKey, idMap);
            
            // add index
            String[] fieldNameArray = getFieldNameIndexArray(clazz);
            for (String fieldName : fieldNameArray) {
                String cacheKey = getCacheKey(o, fieldName);
                @SuppressWarnings("unchecked")
                List<Long> idList = (List<Long>) redisCache.getObject(cacheKey);
                updateMap.put(cacheKey, idList);
                if(idList==null)
                {
                    idList = new ArrayList<Long>();
                }
                idList.add(newId);
                updateMap.put(cacheKey, idList);
            }    
            
            // add object
            String idCacheKey = getIdCacheKey(clazz,newId);
            Object oldObj = redisCache.getObject(idCacheKey);
            oldMap.put(idCacheKey, oldObj);
            Class<?> idFieldType = getIdFieldType(clazz);
            if(!CodeUtil.isEmpty(idFieldType))
            {
                String idFieldName = getIdFieldName(clazz);
                Object idValue = StringUtil.getValueFromString(StringUtil.toString(newId), idFieldType);
                ReflectUtil.setMethodInvoke(o, idFieldName, idValue);
            }
            updateMap.put(idCacheKey, o);
        } catch (Exception e) {
            TeshehuiRuntimeException te = (e instanceof TeshehuiRuntimeException)?(TeshehuiRuntimeException)e:new TeshehuiRuntimeException(e);
            throw te;
        }
        
        // update redisCache and roll back
        updateRedisCacheAndRollback(updateMap,null,oldMap);
        LogUtil.d(getClass().getName(),"useTime="+(System.currentTimeMillis()-startTime)+getClass()+".add, condition="+o);

        return newId;
    }

    private void updateRedisCacheAndRollback(Map<String, Object> updateMap, Set<String> deleteSet, Map<String, Object> oldMap) {
        long startTime = System.currentTimeMillis();
        try {
            Set<Entry<String, Object>> entrySet = updateMap.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                redisCache.setObject(entry.getKey(), entry.getValue());
            }
            // delete redisCache
            if(deleteSet!=null)
            {
                for (String key : deleteSet) {
                    redisCache.deleteObject(key);
                }
            }
        } catch (Exception e) {
            LogUtil.e(getClass().getName(),"useTime="+(System.currentTimeMillis()-startTime)+getClass()+".update redis cache illegal rollback.",e);
            
            Set<Entry<String, Object>> entrySet = oldMap.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                Object value = entry.getValue();
                if(value!=null)
                {
                    redisCache.setObject(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public <T> T get(T t)
    {
        long startTime = System.currentTimeMillis();
        CodeUtil.emptyCheck(null,"query one="+t, new Object[]{t});
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) t.getClass();
        
        // check id null
        Long id = null;
        String idFieldName = getIdFieldName(clazz);
        if(!CodeUtil.isEmpty(idFieldName))
        {
            Object idValue = ReflectUtil.getMethodInvoke(t, idFieldName);
            id = StringUtil.getValueFromString(StringUtil.toString(idValue), Long.class);
        }
        CodeUtil.emptyCheck(null,"query one id null, one="+t, new Object[]{id});
        
        // get object
        String idCacheKey = getIdCacheKey(clazz,id);
        Object mapObj = redisCache.getObject(idCacheKey);
        if(mapObj!=null)
        {
        	@SuppressWarnings("unchecked")
			T result = (T)mapObj;
        	if(result!=null&&clazz.isAssignableFrom(result.getClass()))
        	{
        		return result;
        	}
        }
        LogUtil.d(getClass().getName(),"useTime="+(System.currentTimeMillis()-startTime)+getClass()+".get, condition="+t);

        return null;
    }
    
    @Override
    public void delete(Object o)
    {
        long startTime = System.currentTimeMillis();
        CodeUtil.emptyCheck(null,"delete one="+o, new Object[]{o});
        
        Map<String, Object> oldMap = new LinkedHashMap<String, Object>();
        Map<String, Object> updateMap = new LinkedHashMap<String, Object>();
        Set<String> deleteSet = new LinkedHashSet<String>();
        try {
            // check id null
            Long id = null;
            Class<?> clazz = o.getClass();
            String idFieldName = getIdFieldName(clazz);
            if(!CodeUtil.isEmpty(idFieldName))
            {
                Object idValue = ReflectUtil.getMethodInvoke(o, idFieldName);
                id = idValue==null?null:StringUtil.getValueFromString(idValue.toString(), Long.class);
            }
            CodeUtil.emptyCheck(null,"delete one id null,clazz="+clazz, new Object[]{id});
            
            // isDeleted from idMap
            String idMapCacheKey = getIdMapCacheKey(clazz);
            @SuppressWarnings("unchecked")
            Map<Long, Boolean> idMap = (Map<Long,Boolean>) redisCache.getObject(idMapCacheKey);
            oldMap.put(idMapCacheKey, idMap==null?null:new LinkedHashMap<Long, Boolean>(idMap));
            CodeUtil.emptyCheck(null,"delete idMap empty", new Object[]{idMap});
            CodeUtil.emptyCheck(null,"delete idMap id not exist id="+id, new Object[]{idMap.get(id)});
            idMap.put(id, true);
            updateMap.put(idMapCacheKey, idMap);
            
            // delete from index
            String[] fieldNameArray = getFieldNameIndexArray(clazz);
            for (String fieldName : fieldNameArray) {
                String cacheKey = getCacheKey(o, fieldName);
                @SuppressWarnings("unchecked")
                List<Long> idList = (List<Long>) redisCache.getObject(cacheKey);
                oldMap.put(cacheKey, idList==null?null:new ArrayList<Long>(idList));
                CodeUtil.emptyCheck(null,"delete one from index,idList="+idList, new Object[]{idList});
                List<Long> newIdList = new ArrayList<Long>();
                for (Long one : idList) {
                    if(!one.equals(id))
                    {
                        newIdList.add(one);
                    }
                }
                updateMap.put(cacheKey, newIdList);
            }
            
            // check old object exist and delete object
            String idCacheKey = getIdCacheKey(clazz,id);
            Object oldMapObj = redisCache.getObject(idCacheKey);
            oldMap.put(idCacheKey, oldMapObj);
            if(oldMapObj!=null)
            {
//                CodeUtil.emptyCheck(null,"delete one not exist,idCacheKey="+idCacheKey, oldMapObj);
                deleteSet.add(idCacheKey);
            }
        } catch (Exception e) {
            TeshehuiRuntimeException te = (e instanceof TeshehuiRuntimeException)?(TeshehuiRuntimeException)e:new TeshehuiRuntimeException(e);
            throw te;
        }
        
        // update redisCache and roll back
        updateRedisCacheAndRollback(updateMap, deleteSet, oldMap);
        LogUtil.d(getClass().getName(),"useTime="+(System.currentTimeMillis()-startTime)+getClass()+".delete, condition="+o);

    }
    
    @Override
    public void update(Object o)
    {
        long startTime = System.currentTimeMillis();
        CodeUtil.emptyCheck(null,"update one="+o, new Object[]{o});
        Class<?> clazz = o.getClass();
        String idFieldName = getIdFieldName(clazz);
        CodeUtil.emptyCheck(null,"update one idFieldName null", new Object[]{idFieldName});
        Object idValue = ReflectUtil.getMethodInvoke(o, idFieldName);
        Long id = idValue==null?null:StringUtil.getValueFromString(idValue.toString(), Long.class);
        CodeUtil.emptyCheck(null,"update one id null,idValue="+idValue, new Object[]{id});
        
        Map<String, Object> oldMap = new LinkedHashMap<String, Object>();
        Map<String, Object> updateMap = new LinkedHashMap<String, Object>();
        String idCacheKey = getIdCacheKey(clazz,id);
        Object oldObj = redisCache.getObject(idCacheKey);
        oldMap.put(idCacheKey, oldObj);
        CodeUtil.emptyCheck(null,"update one not exist,idCacheKey="+idCacheKey, new Object[]{oldObj});
        ReflectUtil.cloneField(true, o, oldObj);
        Object newMapObj = oldObj;
        updateMap.put(idCacheKey, newMapObj);
        
        // update redisCache and roll back
        updateRedisCacheAndRollback(updateMap, null, oldMap);
        LogUtil.d(getClass().getName(),"useTime="+(System.currentTimeMillis()-startTime)+getClass()+".update, condition="+o);

    }
    
    @Override
    public <T> T addOrUpdate(T t)
    {
        T oldOne = get(t);
        if(oldOne==null)
        {
            add(t);
        }
        else
        {
            update(t);
        }
        return t;
    }
    
    @Override
    public <T> List<T> queryAll(Class<T> clazz)
    {
        long startTime = System.currentTimeMillis();
        List<T> result = new ArrayList<T>();
        
        // query idMap
        Map<String, Object> oldMap = new LinkedHashMap<String, Object>();
        Map<String, Object> updateMap = new LinkedHashMap<String, Object>();
        String idMapCacheKey = getIdMapCacheKey(clazz);
        @SuppressWarnings("unchecked")
        Map<Long, Boolean> idMap = (Map<Long,Boolean>) redisCache.getObject(idMapCacheKey);
        oldMap.put(idMapCacheKey, idMap==null?null:new LinkedHashMap<Long, Boolean>(idMap));
        if(idMap==null)
        {
           idMap = new LinkedHashMap<Long, Boolean>();
        }
        Map<Long, Boolean> newIdMap = new LinkedHashMap<Long, Boolean>(idMap);
        Set<Entry<Long, Boolean>> idEntrySet = idMap.entrySet();
        for (Entry<Long, Boolean> entry : idEntrySet) {
            Long id = entry.getKey();
            Boolean isDeleted = entry.getValue();
            if(!isDeleted)
            {
                // get object
                String idCacheKey = getIdCacheKey(clazz, id);
                Object oldObj = redisCache.getObject(idCacheKey);
//                CodeUtil.emptyCheck(null,"queryAll one not exist,idCacheKey="+idCacheKey, oldObj);
                if(oldObj==null)
                {
                    isDeleted = true;
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    T oldT = (T) oldObj;
                    result.add(oldT);
                }
            }
            newIdMap.put(id, isDeleted);
        }
        updateMap.put(idMapCacheKey, newIdMap);
        
        // update redisCache and roll back
        updateRedisCacheAndRollback(updateMap, null, oldMap);
        LogUtil.d(getClass().getName(),"useTime="+(System.currentTimeMillis()-startTime)+getClass()+".queryAll, clazz="+clazz);
        return result;
    }
    
    @Override
    public <T> T queryOne(T t, String... fieldNameArray)
    {
        long startTime = System.currentTimeMillis();
        
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) t.getClass();
        
        if(CodeUtil.isEmpty(new Object[]{fieldNameArray}))
            throw new TeshehuiRuntimeException("queryOne fieldNameArray="+fieldNameArray);
        
        // search index list
        List<T> list;
        Map<String, Object> oldMap = new LinkedHashMap<String, Object>();
        Map<String, Object> updateMap = new LinkedHashMap<String, Object>();
        String[] fieldNameIndexArray = getFieldNameIndexArray(clazz);
        List<String> fieldNameIndexList = getFieldNameIndexList(fieldNameArray);
        String fieldName = searchFieldNameIndex(fieldNameIndexArray, fieldNameIndexList);
        if(fieldName==null)
        {
            list = queryAll(clazz);
        }
        else
        {
            String cacheKey = getCacheKey(t, fieldName);
            @SuppressWarnings("unchecked")
            List<Long> idList = (List<Long>) redisCache.getObject(cacheKey);
            oldMap.put(cacheKey, idList==null?null:new ArrayList<Long>(idList));
            if(idList==null)
            {
                idList = new ArrayList<Long>();
            }
            List<Long> newIdList = new ArrayList<Long>();
            list = new ArrayList<T>();
            for (Long one : idList) {
                // get object
                String idCacheKey = getIdCacheKey(clazz, one);
                Object oldObj = redisCache.getObject(idCacheKey);
//                CodeUtil.emptyCheck(null,"queryAll one not exist,idCacheKey="+idCacheKey, oldObj);
                if(oldObj==null)
                {
                    // do nothing
                }
                else
                {
                    newIdList.add(one);
                    
                    @SuppressWarnings("unchecked")
                    T oldT = (T) oldObj;
                    list.add(oldT);
                }
            }
            updateMap.put(cacheKey, newIdList);
        }
        
        // update redisCache roll back
        updateRedisCacheAndRollback(updateMap, null, oldMap);

        // filter list by fieldValue
//        List<T> newList = new ArrayList<T>();
        for (T one : list) {
            boolean fieldSame = true;
            if(!CodeUtil.isEmpty(new Object[]{fieldNameArray}))
            {
                for (String oneFieldName : fieldNameArray) {
                    Object fieldValue1 = ReflectUtil.getMethodInvoke(t, oneFieldName);
                    Object fieldValue2 = ReflectUtil.getMethodInvoke(one, oneFieldName);
                    if(fieldValue1==null)
                    {
                        if(fieldValue2!=null)
                        {
                            fieldSame = false;
                        }
                    }
                    else
                    {
                        if(!fieldValue1.equals(fieldValue2))
                        {
                            fieldSame = false;
                        }
                    }
                }
                if(fieldSame)
                {
                    return one;
                }
            }
        }
        LogUtil.test("useTime="+(System.currentTimeMillis()-startTime)+getClass()+".queryOne, condition="+t+",fieldNameArray="+fieldNameArray);
        return null;
    }
    @Override
    public <T> List<T> query(T t, String... fieldNameArray)
    {
        long startTime = System.currentTimeMillis();
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) t.getClass();
        
        if(CodeUtil.isEmpty(new Object[]{fieldNameArray}))
            return queryAll(clazz);
        
        // search index list
        List<T> list;
        Map<String, Object> oldMap = new LinkedHashMap<String, Object>();
        Map<String, Object> updateMap = new LinkedHashMap<String, Object>();
        String[] fieldNameIndexArray = getFieldNameIndexArray(clazz);
        List<String> fieldNameIndexList = getFieldNameIndexList(fieldNameArray);
        String fieldName = searchFieldNameIndex(fieldNameIndexArray, fieldNameIndexList);
        if(fieldName==null)
        {
            list = queryAll(clazz);
        }
        else
        {
            String cacheKey = getCacheKey(t, fieldName);
            @SuppressWarnings("unchecked")
            List<Long> idList = (List<Long>) redisCache.getObject(cacheKey);
            oldMap.put(cacheKey, idList==null?null:new ArrayList<Long>(idList));
            if(idList==null)
            {
                idList = new ArrayList<Long>();
            }
            List<Long> newIdList = new ArrayList<Long>();
            list = new ArrayList<T>();
            for (Long one : idList) {
                // get object
                String idCacheKey = getIdCacheKey(clazz, one);
                Object oldObj = redisCache.getObject(idCacheKey);
//                CodeUtil.emptyCheck(null,"queryAll one not exist,idCacheKey="+idCacheKey, oldObj);
                if(oldObj==null)
                {
                    //do nothing
                }
                else
                {
                    newIdList.add(one);
                    
                    @SuppressWarnings("unchecked")
                    T oldT = (T) oldObj;
                    list.add(oldT);
                }
            }
            updateMap.put(cacheKey, newIdList);
        }
        
        // update redis cache roll back
        updateRedisCacheAndRollback(updateMap, null, oldMap);

        // filter list by fieldValue
        List<T> newList = new ArrayList<T>();
        for (T one : list) {
            boolean fieldSame = true;
            if(!CodeUtil.isEmpty(new Object[]{fieldNameArray}))
            {
                for (String oneFieldName : fieldNameArray) {
                    Object fieldValue1 = ReflectUtil.getMethodInvoke(t, oneFieldName);
                    Object fieldValue2 = ReflectUtil.getMethodInvoke(one, oneFieldName);
                    if(fieldValue1==null)
                    {
                        if(fieldValue2!=null)
                        {
                            fieldSame = false;
                        }
                    }
                    else
                    {
                        if(!fieldValue1.equals(fieldValue2))
                        {
                            fieldSame = false;
                        }
                    }
                }
                if(fieldSame)
                {
                    newList.add(one);
                }
            }
        }
        LogUtil.test("useTime="+(System.currentTimeMillis()-startTime)+getClass()+".query, condition="+t+",fieldNameArray="+fieldNameArray);
        return newList;
    }

    public RedisCache getRedisCache() {
        return redisCache;
    }
    public void setRedisCache(RedisCache redisCache) {
        this.redisCache = redisCache;
    }
    public String getIdMapCacheKey() {
        return idMap;
    }
    public void setIdMapCacheKey(String idMapCacheKey) {
        this.idMap = idMapCacheKey;
    }
    public String getIdFieldName() {
        return id;
    }
    public void setIdFieldName(String idFieldName) {
        this.id = idFieldName;
    }

    public String getFieldNameSeparatorTag() {
        return fieldNameSeparatorTag;
    }

    public void setFieldNameSeparatorTag(String fieldNameSeparatorTag) {
        this.fieldNameSeparatorTag = fieldNameSeparatorTag;
    }
    
}
