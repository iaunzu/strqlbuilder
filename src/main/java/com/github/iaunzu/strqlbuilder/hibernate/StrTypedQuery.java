package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.Map;

import javax.persistence.TypedQuery;

import com.github.iaunzu.strqlbuilder.chunks.Aliases;
import com.github.iaunzu.strqlbuilder.pagination.PojoFactoryAware;

public interface StrTypedQuery<X> extends TypedQuery<X>, PojoFactoryAware<X>, CustomPropertyEditorRegistrar {
	void setPositionParameters(Map<Integer, Object> parameters);

	void setParameters(Map<String, Object> parameters);

	void setAlias(Aliases aliases);

	void setTargetClass(Class<X> targetClass);

}
