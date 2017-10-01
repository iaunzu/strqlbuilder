package com.github.iaunzu.strqlbuilder.apptest.domain;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Person {

    @Id
    @GeneratedValue
    @Column(name = "ID_PERSON", unique = true, nullable = false, updatable = false)
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private Float height;
    private Calendar birthday;
    private Date creationDate;
    private Boolean enabled;
    @Column(nullable = false)
    private boolean alive;
    @ManyToOne
    @JoinColumn(name = "ID_JOB")
    private Job job;

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
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

    public Boolean getEnabled() {
	return enabled;
    }

    public void setEnabled(Boolean enabled) {
	this.enabled = enabled;
    }

    public Boolean getAlive() {
	return alive;
    }

    public void setAlive(boolean alive) {
	this.alive = alive;
    }

    public Job getJob() {
	return job;
    }

    public void setJob(Job job) {
	this.job = job;
    }

}
