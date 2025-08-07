package io.github.iaunzu.strqlbuilder.hibernate;

import com.github.iaunzu.beanwrapper.propertyeditor.IPropertyEditor;

public interface CustomPropertyEditorRegistrar {

    void addCustomPropertyEditor(Class<?> clazz, IPropertyEditor propertyEditor);
}
