package com.github.iaunzu.strqlbuilder.pagination;

import org.springframework.data.domain.Page;

import com.github.iaunzu.strqlbuilder.hibernate.CustomPropertyEditorRegistrar;

public interface PagedTypedQuery<T> extends CustomPropertyEditorRegistrar {

	Page<T> getResultList();

	PagedTypedQuery<T> setMaxResult(int maxResult);

	PagedTypedQuery<T> setFirstResult(int startPosition);

}
