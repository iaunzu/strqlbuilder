package io.github.iaunzu.strqlbuilder.chunks;

import io.github.iaunzu.strqlbuilder.QueryBuilder;

public class Where<Q extends QueryBuilder<Q>> extends Chunk<Q> {

    public Where(Q sql) {
        super(sql);
    }

    @Override
    public String build() {
        return " where " + sb.toString();
    }
}
