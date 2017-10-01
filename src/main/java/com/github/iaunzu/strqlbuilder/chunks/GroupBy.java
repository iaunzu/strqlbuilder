package com.github.iaunzu.strqlbuilder.chunks;

public class GroupBy extends Chunk {

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