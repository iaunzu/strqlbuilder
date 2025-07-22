package io.github.iaunzu.strqlbuilder.hibernate;

import java.util.Map;

import io.github.iaunzu.strqlbuilder.chunks.Aliases;
import io.github.iaunzu.strqlbuilder.pagination.PojoFactoryAware;
import jakarta.persistence.TypedQuery;

public interface StrTypedQuery<X> extends TypedQuery<X>, PojoFactoryAware<X>, CustomPropertyEditorRegistrar {
	void setPositionParameters(Map<Integer, Object> parameters);

	void setParameters(Map<String, Object> parameters);

	void setAlias(Aliases aliases);

	String getSQL();

	void setTargetClass(Class<X> targetClass);

}
