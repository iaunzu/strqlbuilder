package io.github.iaunzu.strqlbuilder.pagination;

import io.github.iaunzu.strqlbuilder.hibernate.CustomPropertyEditorRegistrar;
import org.springframework.data.domain.Page;

public interface PagedTypedQuery<T> extends CustomPropertyEditorRegistrar {

    Page<T> getResultList();

    PagedTypedQuery<T> setMaxResult(int maxResult);

    PagedTypedQuery<T> setFirstResult(int startPosition);
}
