package com.github.iaunzu.strqlbuilder.chunks;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Aliases {

    public static final Integer MAX_LENGTH = 30;

    private List<String> original = new ArrayList<String>();
    private List<String> bean = new ArrayList<String>();
    private List<String> sql = new ArrayList<String>();

    public List<String> getOriginalAliases() {
	return original;
    }

    public List<String> getBeanAliases() {
	return bean;
    }

    public List<String> getSqlAliases() {
	return sql;
    }

    public final void add(String alias) {
	original.add(alias);
	bean.add(fixAliasForBean(alias));
	sql.add(fixAliasForSql(alias));
    }

    public int size() {
	return original.size();
    }

    public String fixAliasForBean(String alias) {
	if (alias.startsWith("\"") && alias.endsWith("\"")) {
	    return StringUtils.substring(alias, 1, -1);
	}
	return alias;
    }

    public String fixAliasForSql(String alias) {
	alias = unquote(alias);
	alias = alias.replaceAll("\\.", "_"); // replace all dots with underscore. No quotes are allowed in JPQL

	if (alias.length() < MAX_LENGTH) {
	    return alias;
	}

	final int trunkSuffix = 3;
	String aliasprefix = StringUtils.substring(alias, 0, MAX_LENGTH - trunkSuffix);
	int i = 1;
	String indexSuffix = nextIndexSuffix(i);
	String candidate = getCandidate(aliasprefix, indexSuffix);
	while (sql.contains(candidate)) {
	    indexSuffix = nextIndexSuffix(++i);
	    candidate = getCandidate(aliasprefix, indexSuffix);
	}
	return candidate;
    }

    public String getFixedAliasForOriginal(String originalAlias) {
	int i = original.indexOf(originalAlias);
	if (i == -1) {
	    return originalAlias;
	}
	return sql.get(i);
    }

    private String nextIndexSuffix(int i) {
	return StringUtils.leftPad(i + "", 2, '0');
    }

    private String getCandidate(String aliasprefix, String indexSuffix) {
	String result = aliasprefix + "_" + indexSuffix;
	return result;
    }

    private boolean isQuoted(String str) {
	return str.startsWith("\"") && str.endsWith("\"");
    }

    @SuppressWarnings("unused")
    private String quote(String str) {
	if (isQuoted(str)) {
	    return str;
	} else {
	    return "\"" + str + "\"";
	}
    }

    private String unquote(String str) {
	if (isQuoted(str)) {
	    return StringUtils.substring(str, 1, -1);
	} else {
	    return str;
	}
    }

}