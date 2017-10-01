package com.github.iaunzu.strqlbuilder.hibernate.propertyeditor;

import java.util.Calendar;
import java.util.Date;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;

public class CalendarPropertyEditor implements IPropertyEditor {

    public Object getValue(Object value) {
	if (value instanceof Calendar) {
	    return (Calendar) value;
	} else if (value instanceof Date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime((Date) value);
	    return cal;
	}
	return null;
    }
}
