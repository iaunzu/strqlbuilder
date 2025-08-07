package io.github.iaunzu.strqlbuilder.apptest.repositories;

import io.github.iaunzu.strqlbuilder.apptest.domain.Person;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends PagingAndSortingRepository<Person, Long>, CrudRepository<Person, Long> {

    List<Person> findByName(@Param("name") String name);
}
