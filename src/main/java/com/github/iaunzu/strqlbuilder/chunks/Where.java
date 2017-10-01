package com.github.iaunzu.strqlbuilder.chunks;

import com.github.iaunzu.strqlbuilder.StrQLBuilder;

public class Where extends Chunk {

    public Where(StrQLBuilder sql) {
	super(sql);
    }

    @Override
    public String build() {
	return " where " + sb.toString();
    }

}
