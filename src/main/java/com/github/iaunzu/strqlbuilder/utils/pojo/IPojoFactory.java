package com.github.iaunzu.strqlbuilder.utils.pojo;

public interface IPojoFactory<T> {
    T newInstance();

    Class<T> getTargetClass();

    String getValueAsText(Object value);

    boolean isPrimitive();

    T parsePrimitive(Object rs);
}
