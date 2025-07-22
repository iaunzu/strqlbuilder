package io.github.iaunzu.strqlbuilder.pagination;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.TypedQuery;
import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;

import io.github.iaunzu.strqlbuilder.hibernate.StrTypedQuery;

public class PagedTypedQueryImpl<X> implements PagedTypedQuery<X> {

	protected TypedQuery<X> query;
	private TypedQuery<Long> countQuery;
	private Pageable pageable;

	public PagedTypedQueryImpl(TypedQuery<X> query, Pageable pageable, TypedQuery<Long> countQuery) {
		this.query = query;
		this.countQuery = countQuery;
		this.pageable = pageable;
	}

	@Override
	public void addCustomPropertyEditor(Class<?> clazz, IPropertyEditor propertyEditor) {
		if (query instanceof StrTypedQuery) {
			((StrTypedQuery<X>) query).addCustomPropertyEditor(clazz, propertyEditor);
		}
	}

	@Override
	public PagedTypedQueryImpl<X> setFirstResult(int startPosition) {
		if (query != null)
			query.setFirstResult(startPosition);

		return this;
	}

	@Override
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
