package com.github.iaunzu.strqlbuilder.chunks;

import static com.github.iaunzu.strqlbuilder.chunks.OrderBy.Direction.ASC;

import java.util.ArrayList;
import java.util.List;

public class OrderBy extends Chunk {

    private Aliases selectAliases;
    private List<Direction> direction;
    private List<String> alias;

    public OrderBy(String alias) {
	this(alias, ASC);
    }

    public OrderBy(String alias, Direction direction) {
	super(null);
	this.direction = new ArrayList<Direction>();
	this.direction.add(direction);

	this.alias = new ArrayList<String>();
	this.alias.add(alias);
    }

    public static OrderBy by(String alias) {
	return new OrderBy(alias);
    }

    public static OrderBy by(String alias, Direction direction) {
	return new OrderBy(alias, direction);
    }

    public OrderBy and(String alias) {
	return and(alias, ASC);
    }

    public OrderBy and(String alias, Direction direction) {
	this.alias.add(alias);
	this.direction.add(direction);
	return this;
    }

    public void setSelectAliases(Aliases selectAliases) {
	this.selectAliases = selectAliases;
    }

    @Override
    public boolean isNotEmpty() {
	return alias != null && !alias.isEmpty();
    }

    @Override
    public String build() {
	sb = new StringBuilder();
	for (int i = 0; i < alias.size(); i++) {
	    if (i != 0) {
		sb.append(",");
	    }
	    String fixedAlias = selectAliases.getFixedAliasForOriginal(alias.get(i));
	    sb.append(fixedAlias).append(" ").append(direction.get(i).getAsString());
	}

	return " order by " + sb.toString();
    }

    public static enum Direction {
	ASC("asc"), DESC("desc");

	private String direction;

	private Direction(String direction) {
	    this.direction = direction;
	}

	public String getAsString() {
	    return direction;
	}

	public static Direction fromString(String str) {
	    if (str == null) {
		return null;
	    }
	    for (Direction direction : Direction.values()) {
		if (str.equals(direction.getAsString())) {
		    return direction;
		}
	    }
	    throw new IllegalArgumentException("No matching Direction for string " + str);
	}

    }

}