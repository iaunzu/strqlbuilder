package com.github.iaunzu.strqlbuilder.apptest.dto;

import java.util.Calendar;
import java.util.Date;

public class PersonDTO {

    private Long idPerson;
    private String name;
    private String surname;
    private Integer age;
    private Float height;
    private Calendar birthday;
    private Date creationDate;
    private Enabled enabled;
    private boolean alive;
    private String reallyLongPropertyToTestLongAliases;

    private PersonDTO parent;

    public Long getIdPerson() {
	return idPerson;
    }

    public void setIdPerson(Long idPerson) {
	this.idPerson = idPerson;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getSurname() {
	return surname;
    }

    public void setSurname(String surname) {
	this.surname = surname;
    }

    public Integer getAge() {
	return age;
    }

    public void setAge(Integer age) {
	this.age = age;
    }

    public Float getHeight() {
	return height;
    }

    public void setHeight(Float height) {
	this.height = height;
    }

    public Calendar getBirthday() {
	return birthday;
    }

    public void setBirthday(Calendar birthday) {
	this.birthday = birthday;
    }

    public Date getCreationDate() {
	return creationDate;
    }

    public void setCreationDate(Date creationDate) {
	this.creationDate = creationDate;
    }

    public Enabled getEnabled() {
	return enabled;
    }

    public void setEnabled(Enabled enabled) {
	this.enabled = enabled;
    }

    public boolean getAlive() {
	return alive;
    }

    public void setAlive(boolean alive) {
	this.alive = alive;
    }

    public String getReallyLongPropertyToTestLongAliases() {
	return reallyLongPropertyToTestLongAliases;
    }

    public void setReallyLongPropertyToTestLongAliases(String reallyLongPropertyToTestLongAliases) {
	this.reallyLongPropertyToTestLongAliases = reallyLongPropertyToTestLongAliases;
    }

    public PersonDTO getParent() {
	return parent;
    }

    public void setParent(PersonDTO parent) {
	this.parent = parent;
    }

}
