package com.github.iaunzu.strqlbuilder.apptest.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table
public class Job {

	@Id
	@Column(name = "ID_JOB", unique = true, nullable = false, updatable = false)
	private Long id;
	private String name;
	@OneToMany(mappedBy = "job")
	private List<Person> persons;

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

}
