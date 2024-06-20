package tools.dynamia.domain;

import jakarta.validation.constraints.Min;
import tools.dynamia.domain.contraints.NotEmpty;

public class Person {

    private Long id;

    @NotEmpty(message = "Enter name")
    private String name;
    @Min(value = 18, message = "Enter valid age")
    private int age;

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Min(value = 18, message = "Enter valid age")
    public int getAge() {
        return age;
    }

    public void setAge(@Min(value = 18, message = "Enter valid age") int age) {
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
