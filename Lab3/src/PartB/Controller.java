package PartB;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Controller {
    private static String logPlace = "logs.place";
    private static MyAtomicity atom1 = new MyAtomicity("db.txt", "logs.place");
    private static MyAtomicity atom2 = new MyAtomicity("db.txt", "logs.place");
    private static int watermark = 0;
    // 同时会有两个对数据库可以操作的线程竞争

    public static int getWatermark(){
        watermark ++;
        return watermark;
    }

    private static void recover() throws IOException, ClassNotFoundException {
        ArrayList<Integer> finishWorks = new ArrayList<>();
        ArrayList<Integer> losers = new ArrayList<>();

        ObjectInputStream file = new ObjectInputStream(new BufferedInputStream((new FileInputStream(logPlace))));
        LogFile logs = (LogFile) file.readObject();
        file.close();
        for (int i = logs.size() - 1;i >= 0;i --) {
            Log log = logs.getLog(i);
            if (log.getType() == Log.OUTCOME)
                finishWorks.add(log.getWorkId());
            if (!isExist(log.getWorkId(), finishWorks)) {
                if (!isExist(log.getWorkId(), losers))
                    losers.add(log.getWorkId());
                if (log.getType() == Log.CHANGE) {
                    // undo, 但我们这里不需要实际进行undo操作(具体理由见md)
                }
            }
        }
        for (int loserId: losers){
            logs.addLog(new Log(Log.OUTCOME, "ABORT", loserId, "/"));
        }
    }

    private static boolean isExist(int watermark, ArrayList<Integer> owners){
        for (int id: owners){
            if (id == watermark) return true;
        }
        return false;
    }

    public static int checkWaterMark(){return watermark;}

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // 最开始的初始化
        File log = new File("logs.place");
        if (!log.exists()){
            LogFile init = new LogFile("logs.place");
            init.save();
        }
        // 进行测试
        File db = new File("db.txt");
        // 如果没建过的话初始化
        if (!db.exists()){
            try{
                BufferedWriter outBuff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(db)));
                for(int i = 0;i < 10;i++){
                    outBuff.write("0\n");
                }
                outBuff.close();
            }catch (IOException ignored){
            }
        }
        // 命令行控制
        Scanner scanner = new Scanner(System.in);
        String command;
        System.out.println("Zsh: please input command(<command> <atom_id> <to_write_ch>)");
        command = scanner.nextLine();
        while(!command.equals("quit")){
            String[] parameters = command.split(" ");
            switch (parameters[0]) {
                case "put":
                    if (parameters[1].equals("1")) {
                        atom1.update(parameters[2].charAt(0));
                    } else {
                        atom2.update(parameters[2].charAt(0));
                    }
                    break;
                case "check":
                    // 展示log信息
                    showLogInfo();
                    break;
                case "recover":
                    recover();
                    break;
            }
            System.out.println("Zsh: please input command(<command> <atom_id> <to_write_ch>)");
            command = scanner.nextLine();
        }
    }

    private static void showLogInfo() throws IOException, ClassNotFoundException {
        ObjectInputStream file = new ObjectInputStream(new BufferedInputStream((new FileInputStream(logPlace))));
        LogFile logs = (LogFile) file.readObject();
        logs.showInfo();
    }
}
