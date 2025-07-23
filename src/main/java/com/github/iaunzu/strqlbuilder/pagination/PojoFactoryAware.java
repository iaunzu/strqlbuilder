package com.github.iaunzu.strqlbuilder.pagination;

import com.github.iaunzu.strqlbuilder.utils.pojo.IPojoFactory;

public interface PojoFactoryAware<T> {

    void setPojoFactory(IPojoFactory<T> pojoFactory);

}
