package test;

public class Player {

    IStrategy strategy;
    String type;

    public Player(String type) {
        this.type = type;
    }

    public void setStrategy(IStrategy strategy) {

        this.strategy = strategy;
    }

    public void actionCommand() {

        System.out.println("Player " + this.type);
        strategy.strategyAction();

    }
}
