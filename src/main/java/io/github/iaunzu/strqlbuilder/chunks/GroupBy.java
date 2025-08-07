package io.github.iaunzu.strqlbuilder.chunks;

import io.github.iaunzu.strqlbuilder.QueryBuilder;

public class GroupBy<Q extends QueryBuilder<Q>> extends Chunk<Q> {

    public GroupBy() {
        super(null);
    }

    public void groupBy(String groupBy) {
        sb.append(groupBy);
    }

    @Override
    public String build() {
        return " group by " + sb.toString();
    }
}
