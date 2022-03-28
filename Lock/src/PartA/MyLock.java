package PartA;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyLock{
    private volatile int[] level;
    private volatile boolean[] choosing;
    private int threadNum;

    public MyLock(int threadNum){
        level = new int[threadNum * 10];
        choosing = new boolean[threadNum * 10];
        this.threadNum = threadNum;
    }
    private int find_max() {
        int max=0;
        for(int i=0;i<threadNum * 10;++i)
        {
            if(level[i]>max)
                max=level[i];
        }
        return max;
    }

    public void lock() {
//        String id = Thread.currentThread().getId() + "";
//        byte[] secretBytes;
//        try {
//            secretBytes = MessageDigest.getInstance("md5").digest(id.getBytes());
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("没有这个md5算法！");
//        }
//
//        int newId = Math.abs(new BigInteger(1, secretBytes).intValue() % level.length);
//        System.out.println(newId);
        long id = Thread.currentThread().getId();
//        System.out.println("Thread " + id + " releases the lock.");
        int newId = (int)(id % level.length);
        choosing[newId]=true;
        level[newId] = find_max() + 1;
        choosing[newId] = false;
        for(int i = 0;i < level.length;++i)
        {
            while(choosing[i]){
                // 等待, 在别人还在排队选号时不能抢先后
            }
            // 第一个条件表示有兴趣且它优先级比你更高
            // 第二个表示两人优先级一样，根据在队列中的id号确定先后
            while((level[i] != 0)&& ( (level[i] < level[newId]) || ((level[i] == level[newId]) && (i < newId)) )){
                //阻塞, 等待调度
//                System.out.println("Thread " + id + " waiting. Level: " + level[newId]);
            };
        }
//        System.out.println("Thread " + id + " : I get the lock");
    }

    public void unlock(){
        long id = Thread.currentThread().getId();
//        System.out.println("Thread " + id + " releases the lock.");
        int newId = (int)(id % level.length);
        level[newId] = 0;
    }

}
