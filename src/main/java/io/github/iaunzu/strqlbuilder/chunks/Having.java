package io.github.iaunzu.strqlbuilder.chunks;

import io.github.iaunzu.strqlbuilder.QueryBuilder;

public class Having<Q extends QueryBuilder<Q>> extends Chunk<Q> {

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
