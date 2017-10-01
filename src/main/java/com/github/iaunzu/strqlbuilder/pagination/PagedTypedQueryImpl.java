package com.github.iaunzu.strqlbuilder.pagination;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PagedTypedQueryImpl<X> implements PagedTypedQuery<X> {

    protected TypedQuery<X> query;
    private TypedQuery<Long> countQuery;
    private Pageable pageable;

    public PagedTypedQueryImpl(TypedQuery<X> query, Pageable pageable, TypedQuery<Long> countQuery) {
	this.query = query;
	this.countQuery = countQuery;
	this.pageable = pageable;
    }

    public PagedTypedQueryImpl<X> setFirstResult(int startPosition) {
	if (query != null)
	    query.setFirstResult(startPosition);

	return this;
    }

    public PagedTypedQueryImpl<X> setMaxResult(int maxResult) {
	if (query != null)
	    query.setMaxResults(maxResult);
	return this;
    }

    @Override
    public Page<X> getResultList() {
	List<X> list = query.getResultList();
	if (list == null) {
	    list = new ArrayList<X>();
	}
	long total;
	if (list.isEmpty() && !pageable.hasPrevious()) {
	    // first page, no results
	    total = 0;
	} else if (!list.isEmpty() && list.size() < pageable.getPageSize()) {
	    // última página
	    total = list.size() + pageable.getOffset();
	} else {
	    total = countQuery.getSingleResult();
	}
	return new PageImpl<X>(list, pageable, total);
    }

}
