package com.github.iaunzu.strqlbuilder.chunks;

public class Having extends Chunk {

    public Having() {
	super(null);
    }

    public void having(String having, Object... values) {
	having = extractParams(having, values);
	sb.append(having);
    }

    @Override
    public String build() {
	return " having " + sb.toString();
    }

}