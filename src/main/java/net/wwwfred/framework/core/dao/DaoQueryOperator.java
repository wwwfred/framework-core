package net.wwwfred.framework.core.dao;

public enum DaoQueryOperator
{
	EQ("="),NEQ("!="),LT("<"),LTE("<="),GT(">"),GTE(">="),LIKE("like"),IN("in"),NIN("not in"),IS("is"),NIS("is not");
	private String value;
	public String getValue()
	{
		return value;
	}
	private DaoQueryOperator(String value)
	{
		this.value = value;
	}
}
