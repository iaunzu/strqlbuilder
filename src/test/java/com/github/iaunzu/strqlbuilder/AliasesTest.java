package com.github.iaunzu.strqlbuilder;

import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.iaunzu.strqlbuilder.chunks.Aliases;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class AliasesTest {

    @Test
    public void fixTest() {
	Aliases aliases = new Aliases();
	aliases.add("12345678901234567890123456789");
	aliases.add("123456789012345678901234567890");
	aliases.add("1234567890123456789012345678901");

	assertThat(aliases.getOriginalAliases(), hasSize(3));
	assertThat(aliases.getSqlAliases(), hasSize(3));

	assertThat(aliases.getOriginalAliases().get(0), is("12345678901234567890123456789"));
	assertThat(aliases.getBeanAliases().get(0), is("12345678901234567890123456789"));
	assertThat(aliases.getSqlAliases().get(0), is("12345678901234567890123456789"));
	assertThat(aliases.getOriginalAliases().get(1), is("123456789012345678901234567890"));
	assertThat(aliases.getBeanAliases().get(1), is("123456789012345678901234567890"));
	assertThat(aliases.getSqlAliases().get(1), is("123456789012345678901234567_01"));
	assertThat(aliases.getOriginalAliases().get(2), is("1234567890123456789012345678901"));
	assertThat(aliases.getBeanAliases().get(2), is("1234567890123456789012345678901"));
	assertThat(aliases.getSqlAliases().get(2), is("123456789012345678901234567_02"));
    }

    @Test
    public void beanTest() {
	Aliases aliases = new Aliases();
	aliases.add("property.name");
	aliases.add("\"property.name\"");
	aliases.add("property.reallyLongPropertyNameToCheckTrunk");
	aliases.add("\"property.reallyLongPropertyNameToCheckTrunk\"");

	assertThat(aliases.getOriginalAliases(), hasSize(4));
	assertThat(aliases.getSqlAliases(), hasSize(4));

	assertThat(aliases.getOriginalAliases().get(0), is("property.name"));
	assertThat(aliases.getBeanAliases().get(0), is("property.name"));
	assertThat(aliases.getSqlAliases().get(0), is("property_name"));
	assertThat(aliases.getOriginalAliases().get(1), is("\"property.name\""));
	assertThat(aliases.getBeanAliases().get(1), is("property.name"));
	assertThat(aliases.getSqlAliases().get(1), is("property_name"));
	assertThat(aliases.getOriginalAliases().get(2), is("property.reallyLongPropertyNameToCheckTrunk"));
	assertThat(aliases.getBeanAliases().get(2), is("property.reallyLongPropertyNameToCheckTrunk"));
	assertThat(aliases.getSqlAliases().get(2), is("property_reallyLongProperty_01"));
	assertThat(aliases.getOriginalAliases().get(3), is("\"property.reallyLongPropertyNameToCheckTrunk\""));
	assertThat(aliases.getBeanAliases().get(3), is("property.reallyLongPropertyNameToCheckTrunk"));
	assertThat(aliases.getSqlAliases().get(3), is("property_reallyLongProperty_02"));
    }

}
