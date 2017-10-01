package com.github.iaunzu.strqlbuilder;

import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.iaunzu.strqlbuilder.StrQLBuilder;
import com.github.iaunzu.strqlbuilder.apptest.TestApplication;
import com.github.iaunzu.strqlbuilder.apptest.domain.Person;
import com.github.iaunzu.strqlbuilder.apptest.dto.PersonDTO;
import com.github.iaunzu.strqlbuilder.apptest.repositories.PersonRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class StrQLBuilderNativeInSplitTest extends TestApplication {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    @Before
    public void init() {
	Person person = new Person();
	person.setId(1L);
	person.setName("Luis");
	person.setSurname("Labiano");
	person.setAge(11);
	person.setHeight(1.20f);
	person.setBirthday(Calendar.getInstance());
	person.setCreationDate(Calendar.getInstance().getTime());
	person.setEnabled(true);
	person.setAlive(true);
	personRepository.save(person);
    }

    @Test
    public void personDTOResultTest() {
	int l = 1005;
	List<Long> ids = new ArrayList<Long>(l);
	while (l-- > 0) {
	    ids.add((long) l);
	}
	StrQLBuilder sql = StrQLBuilder.createNative()
		.select("p.id_person as idPerson")
		.from("Person p")
		.where("p.id_person in (:list) or p.id_person > 1", ids);
	TypedQuery<PersonDTO> query = sql.createQuery(entityManager, PersonDTO.class);
	List<PersonDTO> persons = query.getResultList();

	assertThat(persons, hasSize(1));
	PersonDTO person = persons.get(0);
	assertThat(person, is(not(nullValue())));
	assertThat(person.getIdPerson(), is(not(nullValue())));
    }
}
