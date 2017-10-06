# StrQLBuilder
SQL and PSQL builder focused on complex paginated queries

## Description
StrQLBuilder provides you a way to build native and PSQL queries using a fluent API and simplifying paging, COUNT queries and mapping to Data Transfer Object (DTO).

Behind the scenes, StrQLBuilder uses [Hibernate](http://hibernate.org), so we can be sure a large number of databases are supported.

## Usage
Just include in your pom.xml file.
```xml
<dependency>
    <groupId>com.github.iaunzu</groupId>
    <artifactId>strqlbuilder</artifactId>
    <version>1.1.1</version>
</dependency>
```

### A brief example
Let's start with a simple native query:
```java
@Autowired
private EntityManager entityManager;

public Long findIdById(Long id) {
    TypedQuery<Long> query = StrQLBuilder.createNative()
		.select("t.id")
		.from("TestTable t")
        .where("t.id = :param1", id)
		.createQuery(entityManager, Long.class);
	return query.getSingleResult();
}
```
And that's it, now let's analyze it.
First, you instantiate an StrQLBuilder and start concatenating SQL clauses.
When you want to use a parameter, you simple declare it with a colon `:` prefix and add it to the
Then, you call createQuery method and provide the EntityManager and the resulting DTO class (a primitive type in this case).