package PartB;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MyAtomicity {
    private String dbName;
    private String logLocation;
    public MyAtomicity(String db, String location){
        this.dbName = db;
        this.logLocation = location;
    }
    public void update(char ch){
        // try to modify
        new Thread(new Runnable() {
            @Override
            public void run() {
                int watermark = Controller.getWatermark();
                try {
                    log(Log.BEGIN, "NEW_TRANSACTION", watermark,"/");
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                // 获取当前文件中的值
                String[] originals = new String[10];
                try {
                    Scanner scanner = new Scanner(new File(dbName));
                    for (int i = 0;i < 10;i++)
                        originals[i] = scanner.nextLine();
                    scanner.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0;i < 10;i++){
                    try {
                        log(Log.CHANGE,"PUT",watermark,"(" + i + "," + originals[i] + "," + ch + ")");
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    sb.append(ch).append("\n");
                    // 强制休眠
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (watermark == Controller.checkWaterMark()){
                    try {
                        log(Log.OUTCOME, "COMMIT", watermark, "/");
                        System.out.println("Zsh: transaction commit");
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    // commit, 正式写进文件
                    File db = new File(dbName);
                    try {
                        BufferedWriter outBuff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(db)));
                        outBuff.write(sb.toString());
                        outBuff.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        log(Log.OUTCOME, "ABORT", watermark, "/");
                        System.out.println("Zsh: transaction abort");
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    // 由于这意味有人正在做，应该等待一段时间再重做，否则容易再次冲突重做导致两个人互相纠缠
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Retry");
                    run();
                }
            }
        }).start();
    }

    /**    
     *  Log some information  
     * */
    private synchronized void log(int type, String operation, int watermark, String parameters) throws IOException, ClassNotFoundException {
        // 将相关信息记录在log文件中
        ObjectInputStream file = new ObjectInputStream(new BufferedInputStream((new FileInputStream(logLocation))));
        LogFile logs = (LogFile) file.readObject();
        logs.addLog(new Log(type,operation,watermark,parameters));
    }
    /**    
     * Recover the system from your log.
     * This should be called at the start of each {@code update()} call.
     */
}
