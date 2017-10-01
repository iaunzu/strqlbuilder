package com.github.iaunzu.strqlbuilder.hibernate.propertyeditor;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;

public class DoublePropertyEditor implements IPropertyEditor {

    public Object getValue(Object value) {
	if (value instanceof Number) {
	    return ((Number) value).doubleValue();
	}
	return null;
    }
}
