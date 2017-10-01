package com.github.iaunzu.strqlbuilder.chunks;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.github.iaunzu.strqlbuilder.StrQLBuilder;
import com.github.iaunzu.strqlbuilder.chunks.like.Like;
import com.github.iaunzu.strqlbuilder.exceptions.ParseSqlException;
import com.github.iaunzu.strqlbuilder.utils.ConditionValues;
import com.github.iaunzu.strqlbuilder.utils.ConditionValuesParser;
import com.github.iaunzu.strqlbuilder.utils.StrQLUtils;

public abstract class Chunk {

    protected StrQLBuilder sql;

    protected StringBuilder sb;

    private List<Parameter<?>> params;
    public List<Parameter<StrQLBuilder>> sqls;

    public Chunk(StrQLBuilder sql) {
	this.sql = sql;
	sb = new StringBuilder();
	params = new ArrayList<Parameter<?>>();
	sqls = new ArrayList<Parameter<StrQLBuilder>>();
    }

    protected String extractParams(String str, Object... values) {
	int args = values == null ? 0 : values.length;
	if (args == 0) {
	    return str;
	}
	if (StringUtils.isBlank(str)) {
	    return str;
	}
	if (args != StringUtils.countMatches(str, ":")) {
	    throw new ParseSqlException("El número de parámetros no coincide en el predicado " + str);
	}
	ConditionValues cv = ConditionValuesParser.getInstance(str, values).parse();
	str = cv.getCondition();
	values = cv.getValues();

	Matcher m = Pattern.compile(":([^\\s\\)]+)").matcher(str);
	int i = 0;
	while (m.find()) {
	    String paramName = m.group(1);
	    Object value = values[i++];
	    if (value instanceof StrQLBuilder) {
		sqls.add(new Parameter<StrQLBuilder>(paramName, (StrQLBuilder) value));
		continue;
	    }
	    if (value != null && value.getClass().isArray()) {
		value = StrQLUtils.arrayToList(value); // convert array to list
	    }
	    params.add(new Parameter<Object>(paramName, value));
	}
	return str;
    }

    public boolean isNotEmpty() {
	return sb.length() > 0;
    }

    public List<Parameter<?>> getParams() {
	return params;
    }

    public List<Parameter<StrQLBuilder>> getSqls() {
	return sqls;
    }

    public static final String trim(String str) {
	return StringUtils.trim(str);
    }

    public abstract String build();

    public StrQLBuilder and(String and, Object... values) {
	and = extractParams(and, values);
	and = trim(and);
	if (this.sb.length() != 0) {
	    this.sb.append(" and ");
	}
	this.sb.append(and);
	return sql;
    }

    public StrQLBuilder andlike(Like like) {
	if (like.getValue() == null)
	    return sql;
	// REGEXP_LIKE es de oracle
	// return and(" REGEXP_LIKE(" + alias + ", " + var + " )", value.replaceAll("(.)", "[[=$1=]]"));
	return and(like.getStatement(), like.getValue());
    }

}
