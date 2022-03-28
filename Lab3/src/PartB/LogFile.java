package PartB;

import java.io.*;
import java.util.ArrayList;

public class LogFile implements Serializable {
    private String logLocation;
    private ArrayList<Log> logs;

    public LogFile(String logLocation){
        this.logLocation = logLocation;
        this.logs = new ArrayList<>();
    }

    public void addLog(Log log){
        logs.add(log);
        save();
    }

    public void save(){
        // 写入实际存储
        try{
            ObjectOutputStream writer = new ObjectOutputStream(new FileOutputStream(logLocation));
            writer.writeObject(this);
            writer.close();
        }catch (IOException ignored){
        }
    }

    public Log getLog(int id){
        return logs.get(id);
    }

    public int size(){return logs.size();}

    public void showInfo(){
        for (Log log: logs){
            System.out.println(log.toString());
        }
    }
}
