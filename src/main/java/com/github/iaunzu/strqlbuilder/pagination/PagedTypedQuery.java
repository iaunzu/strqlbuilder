package com.github.iaunzu.strqlbuilder.pagination;

import org.springframework.data.domain.Page;

public interface PagedTypedQuery<T> {

    Page<T> getResultList();

    PagedTypedQuery<T> setMaxResult(int maxResult);

    PagedTypedQuery<T> setFirstResult(int startPosition);

}
