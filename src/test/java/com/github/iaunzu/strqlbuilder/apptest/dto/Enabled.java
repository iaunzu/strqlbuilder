package com.github.iaunzu.strqlbuilder.apptest.dto;

import com.github.iaunzu.beanwrapper.dto.DatabaseClass;

public enum Enabled implements DatabaseClass<Boolean> {
    ENABLED, DISABLED;

    @Override
    public Boolean getDatabaseValue() {
	return this == ENABLED;
    }

}
