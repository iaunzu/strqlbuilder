package io.github.iaunzu.strqlbuilder.chunks;

import io.github.iaunzu.strqlbuilder.NativeQueryBuilder;

public class With extends Chunk<NativeQueryBuilder> {

    public With(NativeQueryBuilder sql) {
        super(sql);
    }

    public WithAs with(String temporaryTableName) {
        temporaryTableName = trim(temporaryTableName);
        return new WithAs((NativeQueryBuilder) sql, this, temporaryTableName);
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

        private NativeQueryBuilder sql;

        private With with;

        private StringBuilder tableName;

        public WithAs(NativeQueryBuilder sql, With with, String temporaryTableName) {
            this.sql = sql;
            this.with = with;
            this.tableName = new StringBuilder(temporaryTableName);
        }

        public NativeQueryBuilder as(String str) {
            return as(str, new Object[0]);
        }

        public NativeQueryBuilder as(String str, Object... values) {
            str = extractParams(str, values);
            with.sb.append(tableName).append(" as (").append(str).append(") ");
            return sql;
        }

        public NativeQueryBuilder as(NativeQueryBuilder subquery) {
            with.sb.append(tableName).append(" as (").append(subquery.build()).append(") ");
            return sql;
        }
    }
}
