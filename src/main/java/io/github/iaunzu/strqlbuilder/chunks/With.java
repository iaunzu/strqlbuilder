package io.github.iaunzu.strqlbuilder.chunks;

import io.github.iaunzu.strqlbuilder.StrQLBuilder;

public class With extends Chunk {

    public With(StrQLBuilder sql) {
        super(sql);
    }

    public WithAs with(String temporaryTableName) {
        temporaryTableName = trim(temporaryTableName);
        return new WithAs(sql, this, temporaryTableName);
    }

    public String buildFirst() {
        return " with " + sb.toString();
    }

    public String buildSubsequent() {
        return ", " + sb.toString();
    }

    @Override
    public String build() {
        throw new UnsupportedOperationException(
                "Use buildFirst() or buildSubsequent() instead of build() for With clause.");
    }

    public class WithAs {

        private StrQLBuilder sql;

        private With with;

        private StringBuilder tableName;

        public WithAs(StrQLBuilder sql, With with, String temporaryTableName) {
            this.sql = sql;
            this.with = with;
            this.tableName = new StringBuilder(temporaryTableName);
        }

        public StrQLBuilder as(String str) {
            return as(str, new Object[0]);
        }

        public StrQLBuilder as(String str, Object... values) {
            str = extractParams(str, values);
            with.sb.append(tableName).append(" as (").append(str).append(") ");
            return sql;
        }

        public StrQLBuilder as(StrQLBuilder subquery) {
            with.sb.append(tableName).append(" as (").append(subquery.build()).append(") ");
            return sql;
        }
    }
}
