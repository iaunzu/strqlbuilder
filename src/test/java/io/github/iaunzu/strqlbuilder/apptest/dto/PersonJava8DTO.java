package io.github.iaunzu.strqlbuilder.apptest.dto;

import java.time.LocalDate;

public class PersonJava8DTO extends PersonDTO {

    public PersonJava8DTO() {
        super();
    }

    private LocalDate birthDate;

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
