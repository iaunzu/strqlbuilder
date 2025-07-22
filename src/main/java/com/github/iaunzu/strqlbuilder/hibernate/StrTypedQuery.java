package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.Map;

import javax.persistence.TypedQuery;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;
import com.github.iaunzu.strqlbuilder.chunks.Aliases;
import com.github.iaunzu.strqlbuilder.utils.pojo.IPojoFactory;

public interface StrTypedQuery<X> extends TypedQuery<X>
{
	void setPositionParameters(Map<Integer, Object> parameters);

	void setParameters(Map<String, Object> parameters);

	void setAlias(Aliases aliases);

	void setTargetClass(Class<X> targetClass);

	void addCustomPropertyEditor(Class<?> clazz, IPropertyEditor propertyEditor);

	void setPojoFactory(IPojoFactory<X> pojoFactory);
}
