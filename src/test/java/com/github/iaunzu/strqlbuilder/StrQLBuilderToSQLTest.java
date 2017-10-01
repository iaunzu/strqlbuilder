package com.github.iaunzu.strqlbuilder;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.github.iaunzu.strqlbuilder.StrQLBuilder;

public class StrQLBuilderToSQLTest {

    @Test
    public void shouldResolveSelect() {
	StrQLBuilder sql = StrQLBuilder.createNative()
		.select("t.id as id")
		.select(":long as number", 1L)
		.select(":int as int", 1)
		.select(":str as string", "1")
		.from("table t");

	assertThat(sql.toSQL(), is("select t.id as id, 1 as number, 1 as int, '1' as string from table t"));
    }

    @Test
    public void shouldResolveIN() {
	StrQLBuilder sql = StrQLBuilder.createNative()
		.select("t.id as id")
		.from("table t")
		.where("t.id in (:list)", Arrays.asList(1L, 2L))
		.and("t.name in (:list2)", Arrays.asList("a", "b"));

	assertThat(sql.toSQL(), is("select t.id as id from table t where t.id in (1,2) and t.name in ('a','b')"));
    }

    @Test
    public void shouldResolveMultiples() {
	StrQLBuilder sql = StrQLBuilder.createNative()
		.select("t.id as id")
		.from("table t")
		.where("t.id in (:list)", Arrays.asList(1L, 2L))
		.and("t.name in (:list)");

	assertThat(sql.toSQL(), is("select t.id as id from table t where t.id in (1,2) and t.name in (1,2)"));
    }

    @Test
    public void shouldResolveBooleansWithoutFromStatement() {
	StrQLBuilder sql = StrQLBuilder.createNative()
		.select(":true as id", true);

	assertThat(sql.toSQL(), is("select true as id"));
    }

}
