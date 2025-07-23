package io.github.iaunzu.strqlbuilder.chunks;

import io.github.iaunzu.strqlbuilder.QueryBuilder;
import org.apache.commons.lang3.StringUtils;

public class Join<Q extends QueryBuilder<Q>> extends Chunk<Q> {

    public Join(Q sql) {
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
        INNER_JOIN("join"),
        LEFT_JOIN("left join"),
        RIGHT_JOIN("right join"),
        JOIN_FETCH("join fetch"),
        LEFT_JOIN_FETCH("left join fetch"),
        FULL_JOIN("full join");
        String type;

        private JoinType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public class JoinOn {
        private Q sql;

        private Join<Q> join;

        private StringBuilder tableName;

        public JoinOn(Q sql, Join<Q> join, String tableName) {
            this.sql = sql;
            this.join = join;
            this.tableName = new StringBuilder(tableName);
        }

        /**
         * Returns the original {@link SQLBuilder}, ignoring the <code>ON</code> clause.
         *
         * @return the original instance of {@link SQLBuilder}.
         */
        public Q on() {
            join.sb.append(tableName);
            return sql;
        }

        /**
         * Appends the condition to the <code>JOIN</code> clause using the <code>ON</code> operator. Returns the original {@code StrQLBuilder}.
         *
         * @param str
         *            a {@code String} with the condition of the <code>JOIN</code> clause.
         * @return the original instance of {@link SQLBuilder}.
         */
        public Q on(String str) {
            return on(str, new Object[0]);
        }

        /**
         * Appends the condition to the <code>JOIN</code> clause using the <code>ON</code> operator, and the parameters associated to them. Returns the original
         * {@code StrQLBuilder}.
         *
         * @param str
         *            a {@code String} with the condition of the <code>JOIN</code> clause.
         * @param values
         *            parameter values of the condition
         * @return the original instance of {@link SQLBuilder}.
         */
        public Q on(String str, Object... values) {
            if (StringUtils.isBlank(str)) {
                return on();
            }
            str = extractParams(str, values);
            join.sb.append(tableName).append(" on ").append(str);
            return sql;
        }

        /**
         * Returns the original {@link SQLBuilder}, ignoring the <code>WITH</code> clause.
         *
         * @return the original instance of {@link SQLBuilder}.
         */
        public Q with() {
            join.sb.append(tableName);
            return sql;
        }

        /**
         * Appends the condition to the <code>JOIN</code> clause using the <code>WITH</code> operator. Returns the original {@code StrQLBuilder}.
         *
         * @param str
         *            a {@code String} with the condition of the <code>JOIN</code> clause.
         * @return the original instance of {@link SQLBuilder}.
         */
        public Q with(String str) {
            return with(str, new Object[0]);
        }

        /**
         * Appends the condition to the <code>JOIN</code> clause using the <code>WITH</code> operator, and the parameters associated to them. Returns the original
         * {@code StrQLBuilder}.
         *
         * @param str
         *            a {@code String} with the condition of the <code>JOIN</code> clause.
         * @param values
         *            parameter values of the condition
         * @return the original instance of {@link SQLBuilder}.
         */
        public Q with(String str, Object... values) {
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
