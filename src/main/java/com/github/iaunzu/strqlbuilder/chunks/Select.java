package com.github.iaunzu.strqlbuilder.chunks;

import org.apache.commons.lang3.StringUtils;

public class Select extends Chunk {

    private Aliases aliases;

    public Select() {
	super(null);
	aliases = new Aliases();
    }

    public Select select(String select, Object... values) {
	select = extractParams(select, values);
	for (String str : select.split(",")) {
	    if (StringUtils.isNotBlank(str)) {
		append(trim(str));
	    }
	}
	return this;
    }

    private void append(String str) {
	if (this.sb.length() != 0) {
	    sb.append(", ");
	}

	String alias = getAlias(str);
	if (StringUtils.isNotBlank(alias)) {
	    String fixedAlias = aliases.fixAliasForSql(alias);
	    if (!fixedAlias.equals(alias)) {
		str = str.replace(alias, fixedAlias);
	    }
	    aliases.add(alias);
	}
	this.sb.append(str);
    }

    /**
     * Return alias from a trimmed non-null string.
     * 
     * @param str
     * @return
     */
    private static String getAlias(String str) {
	// assert (!str.startsWith(" ") && !str.endsWith(" "));
	int asIndex = str.toLowerCase().indexOf(" as ");
	if (asIndex == -1) {
	    // no alias, using column name
	    if (str.toLowerCase().startsWith("distinct ")) {
		str = trim(StringUtils.substring(str, "distinct ".length()));
	    }
	    if (str.matches("^[a-zA-Z0-9_\\.]+$")) { // letters, numbers, underscores and dots
		if (str.indexOf('.') != -1) {
		    return StringUtils.substringAfter(str, ".");
		} else {
		    return str;
		}
	    }
	    // column name not recognized
	    return "";
	}
	return trim(StringUtils.substring(str, asIndex + " as ".length()));
    }

    public Aliases getAliases() {
	return aliases;
    }

    @Override
    public String build() {
	return "select " + sb.toString();
    }

}
