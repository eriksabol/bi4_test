package test;

import java.util.*;

public class MainStreams {

    public static void main(String[] args) {

        System.out.println("Hello World!");

        Person erik = new Person(38, "Kosice", "Erik");
        Person rody = new Person(6, "Licartovce", "Rody");
        Person leonie = new Person(9, "Cadca", "Leonie");
        Person lori = new Person(4, "Kosice", "");

        List<Person> personList = Arrays.asList(erik, rody, leonie, lori);

        List<String> myList = Arrays.asList("erik", "Leonie", "loren", "renata", "rody");

        myList.stream()
                .filter(s -> s.endsWith("n"))
                .map(String::toUpperCase)
                .sorted()
                .forEach(System.out::println);

        personList.stream()
                    .filter(e -> e.getAge() < 10)
                    .map(Person::getName)
                    .filter(e -> e.startsWith("L"))
                    .forEach(System.out::println);

        personList.stream()
                .filter(e -> e.getName().isEmpty())
                .map(Person::getAge)
                .forEach(System.out::println);

        personList.stream()
                .map(e -> e.getName().equals("Erik"))
                .forEach(System.out::println);

        Map<String, Integer> formatterMap = new LinkedHashMap<>();
        formatterMap.put("serverTitle", 50);
        formatterMap.put("serverState", 25);
        formatterMap.put("runningPort", 15);
        formatterMap.put("setMethod", 15);
        formatterMap.put("nowSetTo", 15);
        formatterMap.put("rqrsRestart", 15);

        printHeader(formatterMap);
        printDashedSpacer(formatterMap);

    }

    private static void printHeader(Map<String, Integer> formatterMap) {

        formatterMap.forEach((key, value) -> System.out.print(String.format("%-" + value + "s", key)));
        System.out.println();

    }

    private static void printDashedSpacer(Map<String, Integer> formatterMap) {

        Integer sumOfFormattedSpace = formatterMap.values().stream().reduce(0, Integer::sum);
        System.out.println(String.join("", Collections.nCopies(sumOfFormattedSpace, "-")));

    }

}