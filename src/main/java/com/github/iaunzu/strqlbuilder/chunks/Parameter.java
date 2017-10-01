package com.github.iaunzu.strqlbuilder.chunks;

public class Parameter<T> {

    private String name;
    private T value;

    public Parameter() {
    }

    public Parameter(String name, T value) {
	this.name = name;
	this.value = value;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public T getValue() {
	return value;
    }

    public void setValue(T value) {
	this.value = value;
    }

    @Override
    public String toString() {
	return "Parameter [name=" + name + ", value=" + value + "]";
    }

}
