package test;

public class Main {
    public static void main(String[] args) {

        String newRequestPort = "6499";
        String actualServerExecProps = "-loggingPath /usr/sap/BOD/sap_bobj/logging/ -requestport 6403";

        String pattern = "-requestport\\s+\\d{4}";
        String replacementString = "-requestport " + newRequestPort;
        System.out.println(replacementString);



        String modifiedServerExecProps = actualServerExecProps.replaceAll(pattern, replacementString);
        System.out.println(modifiedServerExecProps);

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
