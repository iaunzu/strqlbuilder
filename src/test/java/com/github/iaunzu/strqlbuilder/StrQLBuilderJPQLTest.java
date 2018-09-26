package com.github.iaunzu.strqlbuilder;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.iaunzu.strqlbuilder.StrQLBuilder;
import com.github.iaunzu.strqlbuilder.apptest.TestApplication;
import com.github.iaunzu.strqlbuilder.apptest.domain.Person;
import com.github.iaunzu.strqlbuilder.apptest.dto.Enabled;
import com.github.iaunzu.strqlbuilder.apptest.dto.PersonDTO;
import com.github.iaunzu.strqlbuilder.apptest.repositories.PersonRepository;
import com.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery;

import static com.github.iaunzu.strqlbuilder.chunks.OrderBy.by;
import static com.github.iaunzu.strqlbuilder.chunks.OrderBy.Direction.DESC;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

//@ActiveProfiles("h2")
//@ActiveProfiles("mysql")
public class StrQLBuilderJPQLTest extends TestApplication {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    @Before
    public void init() {
	Person fakePerson = new Person();
	fakePerson.setId(1L);
	fakePerson.setName("Luis");
	fakePerson.setSurname("Fake Person");
	fakePerson.setAlive(false);
	personRepository.save(fakePerson);

	Person person = new Person();
	person.setId(2L);
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
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.id as idPerson")
		.select("p.name as name")
		.select("p.surname as \"surname\"")
		.select("p.enabled as enabled, p.alive as alive")
		.select("p.creationDate as creationDate")
		.select("p.age as age, p.height as height")
		.select("p.name as reallyLongPropertyToTestLongAliases")
		.select("p.name as \"reallyLongPropertyToTestLongAliases\"")
		.select("p.birthday")
		.select("p.name as \"parent.name\"")
		.from("Person p")
		.leftjoin("p.job j").with()
		.where("p.name = :pname", "Luis")
		.andlike("p.name", ":name_pattern", "uis")
		.and("p.name IN :pnamearray", (Object) new String[] { "Luis" })
		.and("p.name IN :pnamelist", Arrays.asList("Luis"))
		.and("p.name IN (:pnamelist2)", Arrays.asList("Luis"))
		.order(by("surname", DESC).and("name"));

	TypedQuery<PersonDTO> query = jpa.createQuery(entityManager, PersonDTO.class);
	query.setFirstResult(0);
	query.setMaxResults(1);
	List<PersonDTO> persons = query.getResultList();

	assertThat(persons, hasSize(1));
	PersonDTO person = persons.get(0);
	assertThat(person, is(not(nullValue())));
	assertThat(person.getIdPerson(), is(not(nullValue())));
	assertThat(person.getName(), is("Luis"));
	assertThat(person.getSurname(), is("Labiano"));
	assertThat(person.getAge(), is(11));
	assertThat(person.getHeight(), is(1.20f));
	assertThat(person.getBirthday(), is(not(nullValue())));
	assertThat(person.getCreationDate(), is(not(nullValue())));
	assertThat(person.getEnabled(), is(Enabled.ENABLED));
	assertThat(person.getAlive(), is(true));
	assertThat(person.getReallyLongPropertyToTestLongAliases(), is("Luis"));
	assertThat(person.getParent().getName(), is("Luis"));
    }

    @Test
    public void orderAliasTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.name as reallyLongPropertyToTestLongAliases")
		.from("Person p")
		.order(by("reallyLongPropertyToTestLongAliases", DESC));

	TypedQuery<PersonDTO> query = jpa.createQuery(entityManager, PersonDTO.class);
	List<PersonDTO> persons = query.getResultList();

	assertThat(persons, is(not(empty())));
    }

    @Test
    public void orderAliasQuotedTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.name as \"reallyLongPropertyToTestLongAliases\"")
		.from("Person p")
		.order(by("\"reallyLongPropertyToTestLongAliases\"", DESC));

	TypedQuery<PersonDTO> query = jpa.createQuery(entityManager, PersonDTO.class);
	List<PersonDTO> persons = query.getResultList();

	assertThat(persons, is(not(empty())));
    }

    @Test
    public void groupHavingTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.name")
		.from("Person p")
		.leftjoin("p.job j").on()
		.where("p.id > :min", 0L)
		.groupBy("p.name")
		.having("count(j.id) >= :counting", 0L);
	TypedQuery<String> query = jpa.createQuery(entityManager, String.class);
	List<String> persons = query.getResultList();
	assertThat(persons, is(not(empty())));
    }

    @Test
    public void stringResultTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.name")
		.from("Person p")
		.leftjoin("p.job").on()
		.where("p.id > :min", 0L);
	TypedQuery<String> query = jpa.createQuery(entityManager, String.class);
	List<String> persons = query.getResultList();
	assertThat(persons, is(not(empty())));
    }

    @Test
    public void longResultTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.id")
		.from("Person p");
	TypedQuery<Long> query = jpa.createQuery(entityManager, Long.class);
	List<Long> persons = query.getResultList();
	assertThat(persons, is(not(empty())));
    }

    @Test
    public void subSelectTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.id")
		.from("Person p")
		.where("not exists (:jpa)", StrQLBuilder.createNative()
			.select("1")
			.from("Job j")
			.where("j.id = p.id")
			.and("1 = :val", 1)
			.and("exists (:jpa2)", StrQLBuilder.createNative()
				.select("1")
				.from("Person p")
				.where("1 = :val2", 1)));
	TypedQuery<Long> query = jpa.createQuery(entityManager, Long.class);
	List<Long> persons = query.getResultList();
	assertThat(persons, is(not(empty())));
    }

    @Test
    public void countTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.count("*")
		.from("Person p");
	TypedQuery<Long> query = jpa.createQuery(entityManager, Long.class);
	Long persons = query.getSingleResult();
	assertThat(persons, is(2L));
    }

    @Test
    public void pagedTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.name")
		.from("Person p");
	Pageable pageable = PageRequest.of(0, 10);
	PagedTypedQuery<String> query = jpa.createPagedQuery(entityManager, String.class, pageable);
	Page<String> persons = query.getResultList();
	assertThat(persons, is(not(nullValue())));
	assertThat(persons.getContent(), hasSize(2));
	assertThat(persons.getNumberOfElements(), is(2));
	assertThat(persons.getTotalElements(), is(2L));
    }

    @Test
    public void page0Test() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.surname")
		.from("Person p");
	Pageable pageable = PageRequest.of(0, 1);
	PagedTypedQuery<String> query = jpa.createPagedQuery(entityManager, String.class, pageable);
	Page<String> persons = query.getResultList();
	assertThat(persons, is(not(nullValue())));
	assertThat(persons.getContent(), hasSize(1));
	assertThat(persons.getNumberOfElements(), is(1));
	assertThat(persons.getTotalElements(), is(2L));
	assertThat(persons.getContent().get(0), is("Fake Person"));

    }

    @Test
    public void page1Test() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.age")
		.from("Person p");
	Pageable pageable = PageRequest.of(1, 1);
	PagedTypedQuery<Long> query = jpa.createPagedQuery(entityManager, Long.class, pageable);
	Page<Long> ages = query.getResultList();
	assertThat(ages, is(not(nullValue())));
	assertThat(ages.getContent(), hasSize(1));
	assertThat(ages.getNumberOfElements(), is(1));
	assertThat(ages.getTotalElements(), is(2L));
	assertThat(ages.getContent().get(0), is(11L));
    }

    @Test
    public void page1DTOTest() {
	StrQLBuilder jpa = StrQLBuilder.createJPQL()
		.select("p.surname")
		.from("Person p")
		.order(by("id_person")); // Hibernate No se checkea si existe la propiedad
	Pageable pageable = PageRequest.of(1, 1);
	PagedTypedQuery<PersonDTO> query = jpa.createPagedQuery(entityManager, PersonDTO.class, pageable);
	Page<PersonDTO> persons = query.getResultList();
	assertThat(persons, is(not(nullValue())));
	assertThat(persons.getContent(), hasSize(1));
	assertThat(persons.getNumberOfElements(), is(1));
	assertThat(persons.getTotalElements(), is(2L));
	assertThat(persons.getContent().get(0).getSurname(), is("Labiano"));
    }

}
