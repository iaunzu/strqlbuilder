package io.github.iaunzu.strqlbuilder;

import io.github.iaunzu.strqlbuilder.chunks.With.WithAs;

public interface NativeQueryBuilder extends QueryBuilder<NativeQueryBuilder> {

    UnionType getUnionType();

    void setUnionType(UnionType unionType);

    /**
     * Creates a new WITH clause with a Common Table Expression (CTE) name.
     *
     * @param cteName
     * @return a {@link WithAs} instance that allows you to specify the CTE
     */
    WithAs with(String cteName);

    /**
     * Appends a <code>UNION</code> operator and returns a new {@link NativeQueryBuilder}
     * instance. Should invoke {@link #endUnion()} once you are done with the
     * new statement.
     *
     * @return a reference to the new {@code StrQLBuilder} instance.
     */
    NativeQueryBuilder union();

    /**
     * Appends a <code>UNION ALL</code> operator and returns a new
     * {@link NativeQueryBuilder} instance. Should invoke {@link #endUnion()} once you
     * are done with the new statement.
     *
     * @return a reference to the new {@code StrQLBuilder} instance.
     */
    NativeQueryBuilder unionAll();

    /**
     * Appends new query through a <code>UNION</code> operator. No calls to
     * {@link #endUnion()} are needed.
     *
     * @param sql
     *            a {@code StrQLBuilder} instance to append with <code>UNION</code>.
     * @return a reference to this object.
     */
    NativeQueryBuilder union(NativeQueryBuilder sql);

    /**
     * Appends new query through a <code>UNION ALL</code> operator. No calls to
     * {@link #endUnion()} are needed.
     *
     * @param sql
     *            a {@code StrQLBuilder} instance to append with
     *            <code>UNION ALL</code>.
     * @return a reference to this object.
     */
    NativeQueryBuilder unionAll(NativeQueryBuilder sql);

    /**
     * Returns the root {@code StrQLBuilder} instance.
     * <p>
     * Invoke this once you are done building the new instance after invoking
     * {@link #union()}.
     *
     * @return a reference to the root {@link SQLBuilder} instance.
     */
    NativeQueryBuilder endUnion();

    public enum UnionType {
        NO_UNION(""),
        UNION(" union "),
        UNIONALL(" union all ");

        private String operator;

        private UnionType(String operator) {
            this.operator = operator;
        }

        String getOperator() {
            return operator;
        }
    }
}
