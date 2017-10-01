package com.github.iaunzu.strqlbuilder;

import static org.hamcrest.collection.IsArrayWithSize.arrayWithSize;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.iaunzu.strqlbuilder.utils.ConditionValues;
import com.github.iaunzu.strqlbuilder.utils.ConditionValuesParser;

import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ConditionValuesTest {

    @Test
    public void shouldDoNothingWithEqualsToConstant() {
	ConditionValues cv1 = new ConditionValues("x.id = 1");
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("x.id = 1"));
	assertThat(cv.getValues(), is(arrayWithSize(0)));
    }

    @Test
    public void shouldDoNothingWithEqualsToParameter() {
	ConditionValues cv1 = new ConditionValues("x.id = :id", 1L);
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("x.id = :id"));
	assertThat(cv.getValues(), is(arrayWithSize(1)));
	assertThat((Long) cv.getValues()[0], is(1L));
    }

    @Test
    public void shouldDoNothingWithInConstant() {
	ConditionValues cv1 = new ConditionValues("x.id in (2)");
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("x.id in (2)"));
	assertThat(cv.getValues(), is(arrayWithSize(0)));
    }

    @Test
    public void shouldDoNothingWithInSmallList() {
	ConditionValues cv1 = new ConditionValues("x.id in (:list)", Arrays.asList(10L, 20L, 30L));
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("x.id in (:list)"));
	assertThat(cv.getValues(), is(arrayWithSize(1)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list = (List<Long>) cv.getValues()[0];
	assertThat(list, hasSize(3));
	assertThat(list, containsInRelativeOrder(10L, 20L, 30L));
    }

    @Test
    public void shouldSplitInIntoMultiples() {
	ConditionValues cv1 = new ConditionValues("x.id in (:list)", Arrays.asList(10L, 20L, 30L, 40L));
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("(x.id in (:list__0) or x.id in (:list__1))"));
	assertThat(cv.getValues(), is(arrayWithSize(2)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	assertThat(cv.getValues()[1], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list0 = (List<Long>) cv.getValues()[0];
	assertThat(list0, hasSize(3));
	assertThat(list0, containsInRelativeOrder(10L, 20L, 30L));
	@SuppressWarnings("unchecked")
	List<Long> list1 = (List<Long>) cv.getValues()[1];
	assertThat(list1, hasSize(1));
	assertThat(list1, containsInRelativeOrder(40L));
    }

    @Test
    public void shouldSplitNotInIntoMultiples() {
	ConditionValues cv1 = new ConditionValues("x.id not in (:list)", Arrays.asList(10L, 20L, 30L, 40L));
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("(x.id not in (:list__0) and x.id not in (:list__1))"));
	assertThat(cv.getValues(), is(arrayWithSize(2)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	assertThat(cv.getValues()[1], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list0 = (List<Long>) cv.getValues()[0];
	assertThat(list0, hasSize(3));
	assertThat(list0, containsInRelativeOrder(10L, 20L, 30L));
	@SuppressWarnings("unchecked")
	List<Long> list1 = (List<Long>) cv.getValues()[1];
	assertThat(list1, hasSize(1));
	assertThat(list1, containsInRelativeOrder(40L));
    }

    @Test
    public void shouldDoNothingWithAnotherExpression() {
	ConditionValues cv1 = new ConditionValues("1<>2 or x.id in :list", Arrays.asList(10L, 20L));
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("1<>2 or x.id in (:list)"));
	assertThat(cv.getValues(), is(arrayWithSize(1)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list0 = (List<Long>) cv.getValues()[0];
	assertThat(list0, hasSize(2));
	assertThat(list0, containsInRelativeOrder(10L, 20L));
    }

    @Test
    public void shouldSplitInWithAnotherExpression() {
	ConditionValues cv1 = new ConditionValues("1<>2 or x.id in :list", Arrays.asList(10L, 20L, 30L, 40L));
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("1<>2 or (x.id in (:list__0) or x.id in (:list__1))"));
	assertThat(cv.getValues(), is(arrayWithSize(2)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	assertThat(cv.getValues()[1], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list0 = (List<Long>) cv.getValues()[0];
	assertThat(list0, hasSize(3));
	assertThat(list0, containsInRelativeOrder(10L, 20L, 30L));
	@SuppressWarnings("unchecked")
	List<Long> list1 = (List<Long>) cv.getValues()[1];
	assertThat(list1, hasSize(1));
	assertThat(list1, containsInRelativeOrder(40L));
    }

    @Test
    public void shouldSplitInWithAnotherParam() {
	ConditionValues cv1 = new ConditionValues("(x.id not in (:list)) and x.id = :id", Arrays.asList(10L, 20L, 30L, 40L), "A");
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("((x.id not in (:list__0) and x.id not in (:list__1))) and x.id = :id"));
	assertThat(cv.getValues(), is(arrayWithSize(3)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	assertThat(cv.getValues()[1], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list0 = (List<Long>) cv.getValues()[0];
	assertThat(list0, hasSize(3));
	assertThat(list0, containsInRelativeOrder(10L, 20L, 30L));
	@SuppressWarnings("unchecked")
	List<Long> list1 = (List<Long>) cv.getValues()[1];
	assertThat(list1, hasSize(1));
	assertThat(list1, containsInRelativeOrder(40L));
	String param = (String) cv.getValues()[2];
	assertThat(param, is("A"));
    }

    @Test
    public void fullTest() {
	String condition = "(x.id in (:list) and x.id in (2) or (x.id not in (:list_2)) and x.id = :id) and (1<>2 or x.id in :list3)";
	ConditionValues cv1 = new ConditionValues(condition, //
		Arrays.asList(1L, 2L, 3L, 4L), //
		Arrays.asList(10L, 20L, 30L, 40L), //
		"A", //
		Arrays.asList(100L, 200L, 300L, 400L));
	ConditionValues cv = new ConditionValuesParser(cv1, 3).parse();

	assertThat(cv.getCondition(), is("((x.id in (:list__0) or x.id in (:list__1)) "
		+ "and x.id in (2) or ((x.id not in (:list_2__0) and x.id not in (:list_2__1))) and x.id = :id) "
		+ "and (1<>2 or (x.id in (:list3__0) or x.id in (:list3__1)))"));
	assertThat(cv.getValues(), is(arrayWithSize(7)));
	assertThat(cv.getValues()[0], instanceOf(List.class));
	assertThat(cv.getValues()[1], instanceOf(List.class));
	assertThat(cv.getValues()[2], instanceOf(List.class));
	assertThat(cv.getValues()[3], instanceOf(List.class));
	assertThat(cv.getValues()[4], instanceOf(String.class));
	assertThat(cv.getValues()[5], instanceOf(List.class));
	assertThat(cv.getValues()[6], instanceOf(List.class));
	@SuppressWarnings("unchecked")
	List<Long> list__0 = (List<Long>) cv.getValues()[0];
	@SuppressWarnings("unchecked")
	List<Long> list__1 = (List<Long>) cv.getValues()[1];
	@SuppressWarnings("unchecked")
	List<Long> list_2__0 = (List<Long>) cv.getValues()[2];
	@SuppressWarnings("unchecked")
	List<Long> list_2__1 = (List<Long>) cv.getValues()[3];
	String param = (String) cv.getValues()[4];
	@SuppressWarnings("unchecked")
	List<Long> list3__0 = (List<Long>) cv.getValues()[5];
	@SuppressWarnings("unchecked")
	List<Long> list3__1 = (List<Long>) cv.getValues()[6];
	assertThat(list__0, hasSize(3));
	assertThat(list__0, containsInRelativeOrder(1L, 2L, 3L));
	assertThat(list__1, hasSize(1));
	assertThat(list__1, containsInRelativeOrder(4L));
	assertThat(list_2__0, hasSize(3));
	assertThat(list_2__0, containsInRelativeOrder(10L, 20L, 30L));
	assertThat(list_2__1, hasSize(1));
	assertThat(list_2__1, containsInRelativeOrder(40L));
	assertThat(param, is("A"));
	assertThat(list3__0, hasSize(3));
	assertThat(list3__0, containsInRelativeOrder(100L, 200L, 300L));
	assertThat(list3__1, hasSize(1));
	assertThat(list3__1, containsInRelativeOrder(400L));

    }

}
