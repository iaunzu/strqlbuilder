package com.github.iaunzu.strqlbuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import com.github.iaunzu.strqlbuilder.utils.StrQLUtils;

public class SqlUtilsTest {

	@Test
	public void replaceTest() {
		StringBuilder sb1 = new StringBuilder("(:sql) (:sql)");
		StrQLUtils.replaceStringBuilder(sb1, ":sql", "select 1 from dual");
		assertThat(sb1.toString(), is("(select 1 from dual) (select 1 from dual)"));

		StringBuilder sb2 = new StringBuilder("(:sql1) (:sql2)");
		StrQLUtils.replaceStringBuilder(sb2, ":sql1", "select 1 from dual");
		StrQLUtils.replaceStringBuilder(sb2, ":sql2", "select 2 from dual");
		assertThat(sb2.toString(), is("(select 1 from dual) (select 2 from dual)"));

		StringBuilder sb3 = new StringBuilder("(:sql)");
		StrQLUtils.replaceStringBuilder(sb3, ":sql", "select 1 from (:sql)");
		assertThat(sb3.toString(), is("(select 1 from (:sql))"));
	}

}
