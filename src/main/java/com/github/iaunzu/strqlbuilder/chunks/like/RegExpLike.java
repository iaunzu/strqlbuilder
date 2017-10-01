package com.github.iaunzu.strqlbuilder.chunks.like;

public class RegExpLike extends AbstractLike {

    public RegExpLike(String alias, String var, String value) {
	super(alias, var, value);
    }

    @Override
    public String getStatement() {
	return " REGEXP_LIKE(" + getAlias() + ", " + getPattern() + " )";
    }

    @Override
    public String getValue() {
	return super.getValue().replaceAll("(.)", "[[=$1=]]");
    }

}
