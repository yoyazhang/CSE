package PartA;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class Test {
    public static void main(String[] args){
        MyLock lock = new MyLock(30);
        //TODO: initialize the lock
        testA1(lock);
    }
    private static int cnt = 0;
    private static void testA1(MyLock lock){
        System.out.println("Test A start");
        int threadNumber = 30;
        final CountDownLatch cdl = new CountDownLatch(threadNumber);//参数为线程个数
        Thread[] threads = new Thread[threadNumber];

        for (int i = 0; i < threadNumber; i++){
            threads[i] = new Thread(() -> {

                lock.lock();
                int tmp = cnt;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                cnt = tmp + 1;
                lock.unlock();
                cdl.countDown();

            });
        }
        for (int i = 0; i < threadNumber; i++){
            threads[i].start();
        }
        //线程启动后调用countDownLatch方法
        try{
            cdl.await();//需要捕获异常，当其中线程数为0时这里才会继续运行
            System.out.println("cnt is " + cnt);
            String res = cnt == threadNumber ? "Test A passed" : "Test A failed,cnt should be 5";
            System.out.println(res);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

}
