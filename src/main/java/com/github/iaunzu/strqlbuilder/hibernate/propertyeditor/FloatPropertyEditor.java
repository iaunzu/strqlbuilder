package com.github.iaunzu.strqlbuilder.hibernate.propertyeditor;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;

public class FloatPropertyEditor implements IPropertyEditor {

    public Object getValue(Object value) {
	if (value instanceof Number) {
	    return ((Number) value).floatValue();
	}
	return null;
    }
}
