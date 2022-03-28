package PartB;

import PartA.MyLock;

public class Philosopher extends Thread{
    private final Object leftFork;
    private final Object rightFork;
    private final String name;
    Philosopher(Object left, Object right, String name){
        this.leftFork = left;
        this.rightFork = right;
        this.name = name;
    }
    private void doAction(String action) throws InterruptedException{
        System.out.println(this.name + " " +
                action);
//        System.out.println(Thread.currentThread().getName() + " " +
//                action);
        Thread.sleep(((int) (Math.random() * 100)));
    }
    @Override
    public void run(){
        try {
            while(true){
                doAction(System.nanoTime() + ": Thinking"); // thinking
                // your code
                ((MyLock)leftFork).lock();
                ((MyLock)rightFork).lock();
                // 随机吃一段时间
                doAction(System.nanoTime() + ": Eating");
                // 解锁
                ((MyLock)rightFork).unlock();
                ((MyLock)rightFork).unlock();
            }
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
}
