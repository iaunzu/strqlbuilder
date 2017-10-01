package com.github.iaunzu.strqlbuilder.hibernate.propertyeditor;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;


public class BeanPropertyEditors {

    public static final Map<Class<?>, IPropertyEditor> map = new HashMap<Class<?>, IPropertyEditor>();

    static {
	map.put(Byte.class, new BytePropertyEditor());
	map.put(Double.class, new DoublePropertyEditor());
	map.put(Float.class, new FloatPropertyEditor());
	map.put(Integer.class, new IntegerPropertyEditor());
	map.put(Long.class, new LongPropertyEditor());
	map.put(Short.class, new ShortPropertyEditor());
	map.put(Boolean.class, new BooleanPropertyEditor());

	map.put(Calendar.class, new CalendarPropertyEditor());
    }

    private BeanPropertyEditors() {
    }

    public static Map<Class<?>, IPropertyEditor> getBeanPropertyEditors() {
	return map;
    }
}
