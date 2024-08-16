package com.github.iaunzu.strqlbuilder;

import static com.github.iaunzu.strqlbuilder.chunks.OrderBy.by;
import static com.github.iaunzu.strqlbuilder.chunks.OrderBy.Direction.DESC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.iaunzu.strqlbuilder.apptest.TestApplication;
import com.github.iaunzu.strqlbuilder.apptest.domain.Person;
import com.github.iaunzu.strqlbuilder.apptest.dto.Enabled;
import com.github.iaunzu.strqlbuilder.apptest.dto.PersonDTO;
import com.github.iaunzu.strqlbuilder.apptest.repositories.PersonRepository;
import com.github.iaunzu.strqlbuilder.pagination.PagedTypedQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

//@ActiveProfiles("h2")
//@ActiveProfiles("mysql")
public class StrQLBuilderNativeTest extends TestApplication {

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	private PersonRepository personRepository;

	@BeforeEach
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
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.id_person as idPerson")
			.select("p.name AS name")
			.select("p.surname as \"surname\"")
			.select("p.enabled as enabled,p.alive as alive")
			.select("p.creation_date as creationDate")
			.select("p.age as age, p.height as height")
			.select("p.name as reallyLongPropertyToTestLongAliases")
			.select("p.name as \"reallyLongPropertyToTestLongAliases\"")
			.select("p.birthday")
			.select("p.name as parent.name")
			.from("Person p")
			.leftjoin("Job j").on("j.id_job = p.id_job").and("p.name = :name", "Luis")
			.where("p.name = :pname", "Luis")
			.andlike("p.name", ":name_pattern", "uis")
			.and("p.name IN :pnamearray", (Object) new String[] { "Luis" })
			.and("p.name IN :pnamelist", Arrays.asList("Luis"))
			.and("p.name IN (:pnamelist2)", Arrays.asList("Luis"))
			.order(by("surname", DESC).and("name"));

		TypedQuery<PersonDTO> query = sql.createQuery(entityManager, PersonDTO.class);
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
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.name as reallyLongPropertyToTestLongAliases")
			.from("Person p")
			.order(by("reallyLongPropertyToTestLongAliases", DESC));

		TypedQuery<PersonDTO> query = sql.createQuery(entityManager, PersonDTO.class);
		List<PersonDTO> persons = query.getResultList();

		assertThat(persons, is(not(empty())));
	}

	@Test
	public void orderAliasQuotedTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.name as \"reallyLongPropertyToTestLongAliases\"")
			.from("Person p")
			.order(by("\"reallyLongPropertyToTestLongAliases\"", DESC));

		TypedQuery<PersonDTO> query = sql.createQuery(entityManager, PersonDTO.class);
		List<PersonDTO> persons = query.getResultList();

		assertThat(persons, is(not(empty())));
	}

	@Test
	public void groupHavingTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.name")
			.from("Person p")
			.leftjoin("Job j").on("j.id_job = p.id_job")
			.where("p.id_person > :min", 0)
			.groupBy("p.name")
			.having("count(p.id_job) >= :counting", 0);
		TypedQuery<String> query = sql.createQuery(entityManager, String.class);
		List<String> persons = query.getResultList();
		assertThat(persons, is(not(empty())));
	}

	@Test
	public void stringResultTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.name")
			.from("Person p")
			.leftjoin("Job j").on("j.id_job = p.id_job")
			.where("p.id_person > :min", 0);
		TypedQuery<String> query = sql.createQuery(entityManager, String.class);
		List<String> persons = query.getResultList();
		assertThat(persons, is(not(empty())));
	}

	@Test
	public void longResultTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.id_person")
			.from("Person p");
		TypedQuery<Long> query = sql.createQuery(entityManager, Long.class);
		List<Long> persons = query.getResultList();
		assertThat(persons, is(not(empty())));
	}

	@Test
	public void subSelectTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.id_person")
			.from("Person p")
			.where("not exists (:sql)", StrQLBuilder.createNative()
				.select("1")
				.from("Job j")
				.where("j.id_job = p.id_job")
				.and("1 = :val", 1)
				.and("exists (:sql2)", StrQLBuilder.createNative()
					.select("1")
					.from("Person p")
					.where("1 = :val2", 1)));
		TypedQuery<Long> query = sql.createQuery(entityManager, Long.class);
		List<Long> persons = query.getResultList();
		assertThat(persons, is(not(empty())));
	}

	@Test
	public void unionTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.surname")
			.from("Person p")
			.where("p.surname = :surname", "Labiano")
			.union()
			.select("p.name")
			.from("Person p")
			.where("p.surname = :surname2", "Labiano")
			.order(by("surname"))
			.endUnion();
		TypedQuery<String> query = sql.createQuery(entityManager, String.class);
		List<String> persons = query.getResultList();
		assertThat(persons, hasSize(2));
		assertThat(persons.get(0), is("Labiano"));
		assertThat(persons.get(1), is("Luis"));
	}

	@Test
	public void unionAllTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.name")
			.from("Person p")
			.where("p.surname = :surname", "Labiano")
			.unionAll(StrQLBuilder.createNative()
				.select("p.surname")
				.from("Person p")
				.where("p.surname = :surname2", "Labiano"));
		TypedQuery<String> query = sql.createQuery(entityManager, String.class);
		List<String> persons = query.getResultList();
		assertThat(persons, hasSize(2));
		assertThat(persons.get(0), is("Luis"));
		assertThat(persons.get(1), is("Labiano"));
	}

	@Test
	public void unionNotEndTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.surname")
			.from("Person p")
			.where("p.surname = :surname", "Labiano")
			.union()
			.select("p.name")
			.from("Person p")
			.where("p.surname = :surname2", "Labiano")
			.order(by("surname"));
		TypedQuery<String> query = sql.createQuery(entityManager, String.class);
		List<String> persons = query.getResultList();
		assertThat(persons, hasSize(2));
		assertThat(persons.get(0), is("Labiano"));
		assertThat(persons.get(1), is("Luis"));
	}

	@Test
	public void countTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.count("*")
			.from("Person p");
		TypedQuery<Long> query = sql.createQuery(entityManager, Long.class);
		Long persons = query.getSingleResult();
		assertThat(persons, is(2L));
	}

	@Test
	public void pagedTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.name")
			.from("Person p");
		Pageable pageable = PageRequest.of(0, 10);
		PagedTypedQuery<String> query = sql.createPagedQuery(entityManager, String.class, pageable);
		Page<String> persons = query.getResultList();
		assertThat(persons, is(not(nullValue())));
		assertThat(persons.getContent(), hasSize(2));
		assertThat(persons.getNumberOfElements(), is(2));
		assertThat(persons.getTotalElements(), is(2L));
	}

	@Test
	public void page0Test() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.surname")
			.from("Person p");
		Pageable pageable = PageRequest.of(0, 1);
		PagedTypedQuery<String> query = sql.createPagedQuery(entityManager, String.class, pageable);
		Page<String> persons = query.getResultList();
		assertThat(persons, is(not(nullValue())));
		assertThat(persons.getContent(), hasSize(1));
		assertThat(persons.getNumberOfElements(), is(1));
		assertThat(persons.getTotalElements(), is(2L));
		assertThat(persons.getContent().get(0), is("Fake Person"));

	}

	@Test
	public void page1Test() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.age")
			.from("Person p");
		Pageable pageable = PageRequest.of(1, 1);
		PagedTypedQuery<Long> query = sql.createPagedQuery(entityManager, Long.class, pageable);
		Page<Long> ages = query.getResultList();
		assertThat(ages, is(not(nullValue())));
		assertThat(ages.getContent(), hasSize(1));
		assertThat(ages.getNumberOfElements(), is(1));
		assertThat(ages.getTotalElements(), is(2L));
		assertThat(ages.getContent().get(0), is(11L));
	}

	@Test
	public void page1DTOTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("p.surname")
			.from("Person p")
			.order(by("id_person"));
		Pageable pageable = PageRequest.of(1, 1);
		PagedTypedQuery<PersonDTO> query = sql.createPagedQuery(entityManager, PersonDTO.class, pageable);
		Page<PersonDTO> persons = query.getResultList();
		assertThat(persons, is(not(nullValue())));
		assertThat(persons.getContent(), hasSize(1));
		assertThat(persons.getNumberOfElements(), is(1));
		assertThat(persons.getTotalElements(), is(2L));
		assertThat(persons.getContent().get(0).getSurname(), is("Labiano"));
	}

	@Test
	public void caseTest() {
		StrQLBuilder sql = StrQLBuilder.createNative()
			.select("CASE WHEN p.age = :age THEN 1 ELSE :cero END", 11, 0L)
			.from("Person p");
		TypedQuery<Long> query = sql.createQuery(entityManager, Long.class);
		List<Long> value = query.getResultList();
		assertThat(value, hasSize(2));
		assertThat(value.get(0), is(0L));
		assertThat(value.get(1), is(1L));
	}

}
