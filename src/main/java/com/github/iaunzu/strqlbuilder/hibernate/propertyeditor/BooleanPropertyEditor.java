package com.github.iaunzu.strqlbuilder.hibernate.propertyeditor;

import org.apache.commons.lang3.ArrayUtils;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;

public class BooleanPropertyEditor implements IPropertyEditor {

    private String[] falseValues = { "FALSE", "0", "F", "NULL", "NIL", "" }; // Any other String returns true

    public Object getValue(Object value) {
	if (value instanceof Number) {
	    return ((Number) value).intValue() == 1;
	} else if (value instanceof Boolean) {
	    return (Boolean) value;
	} else if (value instanceof CharSequence) {
	    String seq = ((CharSequence) value).toString().toUpperCase();
	    return !ArrayUtils.contains(falseValues, seq);
	}
	return null;
    }

    public void setFalseValues(String... values) {
	falseValues = values;
    }
}
