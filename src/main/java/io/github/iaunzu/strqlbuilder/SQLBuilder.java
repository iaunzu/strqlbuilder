package io.github.iaunzu.strqlbuilder;

public class SQLBuilder {

    public static JPQLQueryBuilder createJPQL() {
        return JPQLSqlQueryBuilder.create();
    }

    public static NativeQueryBuilder createNative() {
        return NativeSqlQueryBuilder.create();
    }
}
