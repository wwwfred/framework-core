package net.wwwfred.framework.core.dao;
public class DaoQueryCondition
{
    private String fieldName;
    private DaoQueryOperator operator;
    private Object fieldValue;
    public DaoQueryCondition(String fieldName, DaoQueryOperator operator,Object fieldValue) {
        super();
        this.fieldName = fieldName;
        this.operator = operator;
        this.fieldValue = fieldValue;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public DaoQueryOperator getOperator() {
        return operator;
    }
    public void setOperator(DaoQueryOperator operator) {
        this.operator = operator;
    }
    public Object getFieldValue() {
        return fieldValue;
    }
    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }
    
}