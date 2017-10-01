package com.github.iaunzu.strqlbuilder.utils;

public class ConditionValues {

    private String condition;
    private Object[] values;

    public ConditionValues() {
    }

    public ConditionValues(String condition, Object... values) {
	this.condition = condition;
	this.values = values;
    }

    public String getCondition() {
	return condition;
    }

    public void setCondition(String condition) {
	this.condition = condition;
    }

    public Object[] getValues() {
	return values;
    }

    public void setValues(Object[] values) {
	this.values = values;
    }

}
