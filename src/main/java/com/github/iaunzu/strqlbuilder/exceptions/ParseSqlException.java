package com.github.iaunzu.strqlbuilder.exceptions;

public class ParseSqlException extends RuntimeException {
    public static final long serialVersionUID = -1;

    public ParseSqlException(String message) {
	super(message);
    }
}