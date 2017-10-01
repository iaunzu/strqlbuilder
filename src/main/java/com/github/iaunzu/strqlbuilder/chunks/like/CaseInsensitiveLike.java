package com.github.iaunzu.strqlbuilder.chunks.like;

public class CaseInsensitiveLike extends AbstractLike {

    public CaseInsensitiveLike(String alias, String paramName, String value) {
	super(alias, paramName, value);
    }

    @Override
    public String getStatement() {
	return "UPPER(" + getAlias() + ") like UPPER(" + getPattern() + " )";
    };

    @Override
    public String getValue() {
	return "%" + super.getValue().toUpperCase() + "%";
    }
}
