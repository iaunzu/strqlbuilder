package com.github.iaunzu.strqlbuilder.chunks;

public class From extends Chunk {

    public From() {
	super(null);
    }

    public From from(String from, Object... values) {
	from = extractParams(from, values);
	from = trim(from);
	if (this.sb.length() != 0 && !from.startsWith(",")) {
	    this.sb.append(", ");
	}
	this.sb.append(from);
	return this;
    }

    @Override
    public String build() {
	return " from " + sb.toString();
    }

}
