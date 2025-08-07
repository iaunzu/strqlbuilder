package io.github.iaunzu.strqlbuilder.pagination;

import io.github.iaunzu.strqlbuilder.utils.pojo.IPojoFactory;

public interface PojoFactoryAware<T> {

    void setPojoFactory(IPojoFactory<T> pojoFactory);
}
