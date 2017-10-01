package com.github.iaunzu.strqlbuilder.utils.pojo;

import java.lang.reflect.Constructor;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;

/**
 * Instanciador de objetos por defecto
 * 
 * @param <T>
 */
public class DefaultPojoFactory<T> implements IPojoFactory<T> {

    private static Logger log = LoggerFactory.getLogger(DefaultPojoFactory.class);

    private Class<T> clazz;

    private CustomBooleanEditor booleanEditor;

    public DefaultPojoFactory(Class<T> clazz) {
	this.clazz = clazz;
    }

    @Override
    public T newInstance() {
	try {
	    return this.clazz.newInstance();
	} catch (InstantiationException e) {
	    log.error("Error instanciando " + clazz, e);
	} catch (IllegalAccessException e) {
	    log.error("Error instanciando " + clazz, e);
	}
	return null;
    }

    @Override
    public Class<T> getTargetClass() {
	return clazz;
    }

    @Override
    public String getValueAsText(Object value) {
	if (value == null) {
	    return "";
	}
	if (value.getClass().isArray()) {
	    Object[] arr = ((Object[]) value);
	    if (arr.length == 0) {
		return "";
	    }
	    // Este caso se produce cuando intentamos obtener un objeto primitivo (String, Long...)
	    // a partir de la segunda página en base de datos Oracle (arr[1] es numrow)
	    value = arr[0];
	}
	String result;
	if (clazz.isAssignableFrom(Boolean.class) && value != null) {
	    CustomBooleanEditor editor = getBooleanEditor();
	    editor.setAsText(String.valueOf(value));
	    result = editor.getAsText();
	} else {
	    result = String.valueOf(value);
	}

	return result;
    }

    @Override
    public boolean isPrimitive() {
	return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isAssignableFrom(String.class);
    }

    @Override
    public T parsePrimitive(Object rs) {
	if (rs == null)
	    return null;
	try {
	    Constructor<T> cons = clazz.getConstructor(String.class);
	    return cons.newInstance(getValueAsText(rs));

	} catch (Exception e) {
	    log.error("Error parseando primitivo " + clazz, e);
	}

	return null;
    }

    private CustomBooleanEditor getBooleanEditor() {
	if (booleanEditor == null)
	    booleanEditor = new CustomBooleanEditor(true);

	return booleanEditor;
    }
}
