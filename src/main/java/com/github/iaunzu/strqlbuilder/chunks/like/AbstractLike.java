package com.github.iaunzu.strqlbuilder.chunks.like;

public abstract class AbstractLike implements Like {

    private String alias;
    private String pattern;
    private String value;

    public AbstractLike(String alias, String paramName, String value) {
	this.alias = alias;
	this.pattern = paramName;
	this.value = value;
    }

    public String getAlias() {
	return alias;
    }

    public String getPattern() {
	return pattern;
    }

    @Override
    public String getValue() {
	return value;
    }

}