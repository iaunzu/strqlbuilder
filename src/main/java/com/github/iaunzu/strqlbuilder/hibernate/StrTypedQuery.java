package com.github.iaunzu.strqlbuilder.hibernate;

import java.util.Map;

import com.github.iaunzu.strqlbuilder.chunks.Aliases;

import jakarta.persistence.TypedQuery;

public interface StrTypedQuery<X> extends TypedQuery<X> {
	void setPositionParameters(Map<Integer, Object> parameters);

	void setParameters(Map<String, Object> parameters);

	void setAlias(Aliases aliases);

	String getSQL();

}
