package test;

public class Main {
    public static void main(String[] args) {

        Player terrorist = new Player("terrorist");
        Player peacemaker = new Player("peacemaker");

        terrorist.setStrategy(new IAgressiveStrategy());
        peacemaker.setStrategy(new IDefensiveStrategy());

        terrorist.actionCommand();
        peacemaker.actionCommand();

        peacemaker.setStrategy(new IAgressiveStrategy());
        terrorist.setStrategy(new IDefensiveStrategy());

        terrorist.actionCommand();
        peacemaker.actionCommand();


    }
}
