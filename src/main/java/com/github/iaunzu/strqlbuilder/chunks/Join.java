package com.github.iaunzu.strqlbuilder.chunks;

import org.apache.commons.lang3.StringUtils;

import com.github.iaunzu.strqlbuilder.StrQLBuilder;

public class Join extends Chunk {

    public Join(StrQLBuilder sql) {
	super(sql);
    }

    private JoinType joinType;

    public JoinOn join(JoinType joinType, String table) {
	this.joinType = joinType;
	table = trim(table);
	return new JoinOn(sql, this, table);
    }

    @Override
    public String build() {
	return " " + joinType.getType() + " " + sb.toString();
    }

    public static enum JoinType {
	INNER_JOIN("join"), LEFT_JOIN("left join"), RIGHT_JOIN("right join"), JOIN_FETCH("join fetch"), LEFT_JOIN_FETCH("left join fetch"), FULL_JOIN(
		"full join");
	String type;

	private JoinType(String type) {
	    this.type = type;
	}

	public String getType() {
	    return type;
	}
    }

    public class JoinOn {
	private StrQLBuilder sql;
	private Join join;
	private StringBuilder tableName;

	public JoinOn(StrQLBuilder sql, Join join, String tableName) {
	    this.sql = sql;
	    this.join = join;
	    this.tableName = new StringBuilder(tableName);
	}

	/**
	 * Returns the original {@link StrQLBuilder}, ignoring the <tt>ON</tt> clause.
	 * 
	 * @return the original instance of {@link StrQLBuilder}.
	 */
	public StrQLBuilder on() {
	    join.sb.append(tableName);
	    return sql;
	}

	/**
	 * Appends the condition to the <tt>JOIN</tt> clause using the <tt>ON</tt> operator. Returns the original {@code StrQLBuilder}.
	 *
	 * @param str
	 *            a {@code String} with the condition of the <tt>JOIN</tt> clause.
	 * @return the original instance of {@link StrQLBuilder}.
	 */
	public StrQLBuilder on(String str) {
	    return on(str, new Object[0]);
	}

	/**
	 * Appends the condition to the <tt>JOIN</tt> clause using the <tt>ON</tt> operator, and the parameters associated to them. Returns the original
	 * {@code StrQLBuilder}.
	 *
	 * @param str
	 *            a {@code String} with the condition of the <tt>JOIN</tt> clause.
	 * @return the original instance of {@link StrQLBuilder}.
	 */
	public StrQLBuilder on(String str, Object... values) {
	    if (StringUtils.isBlank(str)) {
		return on();
	    }
	    str = extractParams(str, values);
	    join.sb.append(tableName).append(" on ").append(str);
	    return sql;
	}

	/**
	 * Returns the original {@link StrQLBuilder}, ignoring the <tt>WITH</tt> clause.
	 * 
	 * @return the original instance of {@link StrQLBuilder}.
	 */
	public StrQLBuilder with() {
	    join.sb.append(tableName);
	    return sql;
	}

	/**
	 * Appends the condition to the <tt>JOIN</tt> clause using the <tt>WITH</tt> operator. Returns the original {@code StrQLBuilder}.
	 *
	 * @param str
	 *            a {@code String} with the condition of the <tt>JOIN</tt> clause.
	 * @return the original instance of {@link StrQLBuilder}.
	 */
	public StrQLBuilder with(String str) {
	    return with(str, new Object[0]);
	}

	/**
	 * Appends the condition to the <tt>JOIN</tt> clause using the <tt>WITH</tt> operator, and the parameters associated to them. Returns the original
	 * {@code StrQLBuilder}.
	 *
	 * @param str
	 *            a {@code String} with the condition of the <tt>JOIN</tt> clause.
	 * @return the original instance of {@link StrQLBuilder}.
	 */
	public StrQLBuilder with(String str, Object... values) {
	    if (StringUtils.isBlank(str)) {
		return with();
	    }
	    str = extractParams(str, values);
	    join.sb.append(tableName).append(" with ").append(str);
	    return sql;
	}

	/**
	 * Appends an alias to the table/entity. Invoke this method only if you did not specify an alias previously.
	 * 
	 * @param alias
	 *            a {@code String} with the alias of the table/entity name.
	 * @return a reference to this object.
	 */
	public JoinOn as(String alias) {
	    tableName.append(" as ").append(alias);
	    return this;
	}
    }
}
