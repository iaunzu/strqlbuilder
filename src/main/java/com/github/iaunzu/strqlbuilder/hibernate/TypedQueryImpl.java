package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Query;
import org.hibernate.jpa.internal.QueryImpl;
import org.hibernate.jpa.spi.AbstractEntityManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

import com.github.iaunzu.beanwrapper.IBeanWrapper;
import com.github.iaunzu.beanwrapper.impl.BeanWrapper;
import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;
import com.github.iaunzu.strqlbuilder.chunks.Aliases;
import com.github.iaunzu.strqlbuilder.exceptions.ParseSqlException;
import com.github.iaunzu.strqlbuilder.hibernate.propertyeditor.BeanPropertyEditors;
import com.github.iaunzu.strqlbuilder.utils.pojo.DefaultPojoFactory;
import com.github.iaunzu.strqlbuilder.utils.pojo.IPojoFactory;

@SuppressWarnings("unchecked")
public class TypedQueryImpl<X> extends QueryImpl<X> {

    private static final Logger log = LoggerFactory.getLogger(TypedQueryImpl.class);

    private Aliases aliases;
    private Class<X> targetClass;
    private IPojoFactory<X> pojoFactory;

    public TypedQueryImpl(Query query, AbstractEntityManagerImpl em) {
	super(query, em);
    }

    public X getSingleResult() {
	Object rs = super.getSingleResult();
	return parseResultSet(rs);
    }

    public List<X> getResultList() {
	List<X> list = new ArrayList<X>();
	for (Object res : super.getResultList()) {
	    X obj = parseResultSet(res);
	    list.add(obj);
	}

	return list;
    }

    private X parseResultSet(Object rs) {
	try {

	    IPojoFactory<X> pojoFactory = getPojoFactory();
	    if (pojoFactory.isPrimitive()) {
		return pojoFactory.parsePrimitive(rs);
	    }

	    Object[] row;
	    if (rs instanceof Object[]) {
		row = (Object[]) rs;
	    } else {
		row = new Object[] { rs };
	    }

	    if (row.length < aliases.size()) {
		throw new ParseSqlException(
			"En número de alias de la select no coindicen con el numero de columnas devueltas por la query. Alias definidos: " + aliases);
	    }

	    X result = pojoFactory.newInstance();
	    IBeanWrapper bean = new BeanWrapper(result);
	    prepareBeanWrapper(bean);

	    int index = 0;
	    for (String propertyName : aliases.getBeanAliases()) {
		Object value = row[index++];
		bean.setPropertyValue(propertyName, value);
	    }

	    return result;
	} catch (BeansException e) {
	    log.error("Error haciendo set " + targetClass, e);
	}
	return null;
    }

    public void setParameters(Map<String, Object> parameters) {
	if (parameters == null) {
	    return;
	}
	for (Entry<String, Object> entry : parameters.entrySet()) {
	    setParameter(entry.getKey(), entry.getValue());
	}
    }

    public void setPositionParameters(Map<Integer, Object> parameters) {
	if (parameters == null) {
	    return;
	}
	for (Entry<Integer, Object> entry : parameters.entrySet()) {
	    setParameter(entry.getKey(), entry.getValue());
	}
    }

    public void setTargetClass(Class<X> targetClass) {
	this.targetClass = targetClass;
    }

    public void setAlias(Aliases aliases) {
	this.aliases = aliases;
    }

    private void prepareBeanWrapper(IBeanWrapper bean) {
	Map<Class<?>, IPropertyEditor> defaultPropertyEditors = BeanPropertyEditors.getBeanPropertyEditors();
	for (Entry<Class<?>, IPropertyEditor> entry : defaultPropertyEditors.entrySet()) {
	    bean.addPropertyEditor(entry.getKey(), entry.getValue());
	}
    }

    private IPojoFactory<X> getPojoFactory() {
	if (pojoFactory == null) {
	    pojoFactory = new DefaultPojoFactory<X>(targetClass);
	}
	return pojoFactory;
    }

    public void setPojoFactory(IPojoFactory<X> pojoFactory) {
	this.pojoFactory = pojoFactory;
    }

}
