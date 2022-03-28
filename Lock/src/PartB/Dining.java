package PartB;

import PartA.MyLock;

public class Dining {
    public static void main(String[] args) throws Exception {
        Philosopher[] philosophers = new Philosopher[5];
        Object[] forks = new Object[philosophers.length];
        for (int i = 0; i < forks.length; i++) {
            // initialize fork object
            forks[i] = new MyLock(philosophers.length);
        }
        for (int i = 0; i < philosophers.length; i++) {
            // initialize Philosopher object
            int leftId = i % forks.length;
            int rightId = (i + 1) % forks.length;
            if (leftId < rightId) philosophers[i] = new Philosopher(forks[leftId], forks[rightId], "Philosopher" + i);
            else philosophers[i] = new Philosopher(forks[rightId], forks[leftId], "Philosopher" + i);
        }
        for (Philosopher e: philosophers){
            e.start();
        }
    }
}
