package test;

public class Person {

    private int age;
    private String homeTown;
    private String name;


    Person(int age, String homeTown, String name) {

        this.age = age;
        this.homeTown = homeTown;
        this.name = name;

    }

    public String getName() {

        return this.name;
    }

    public String setName(String name) {

        return this.name = name;

    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


}
