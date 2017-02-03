package net.wwwfred.framework.core.dao.mybatis.impl;

import net.wwwfred.framework.core.dao.DaoQueryCondition;

public class MapperParameterType {

	private String dialect;
//    private Long id;
    private String tableName;
    private String tableSeqName;
    private String[] columnArray;
    private ColumnValue[] columnValueArray;
    private Object[] valueArray;
    private String distinct;
    private DaoQueryCondition[] conditionArray;
    
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String[] getColumnArray() {
		return columnArray;
	}
	public void setColumnArray(String[] columnArray) {
		this.columnArray = columnArray;
	}
	public ColumnValue[] getColumnValueArray() {
        return columnValueArray;
    }
    public void setColumnValueArray(ColumnValue[] columnValueArray) {
        this.columnValueArray = columnValueArray;
    }
    public Object[] getValueArray() {
        return valueArray;
    }
    public void setValueArray(Object[] valueArray) {
        this.valueArray = valueArray;
    }
    public DaoQueryCondition[] getConditionArray() {
        return conditionArray;
    }
    public void setConditionArray(DaoQueryCondition[] conditionArray) {
        this.conditionArray = conditionArray;
    }
//    public Long getId() {
//        return id;
//    }
//    public void setId(Long id) {
//        this.id = id;
//    }
    public String getDistinct() {
        return distinct;
    }
    public void setDistinct(String distinct) {
        this.distinct = distinct;
    }
    public String getTableSeqName() {
        return tableSeqName;
    }
    public void setTableSeqName(String tableSeqName) {
        this.tableSeqName = tableSeqName;
    }
    public String getDialect() {
		return dialect;
	}
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public static class ColumnValue
    {
        private String columnName;
        private Object fieldValue;
        public String getColumnName() {
            return columnName;
        }
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
        public Object getFieldValue() {
            return fieldValue;
        }
        public void setFieldValue(Object fieldValue) {
            this.fieldValue = fieldValue;
        }
    }
}
