package PartB;

import java.io.Serializable;

public class Log implements Serializable {
    public static final int BEGIN = 1;
    public static final int CHANGE = 2;
    public static final int OUTCOME = 3;

    private int type;
    // 如change的put，outcome对应的abort/commit，begin的retry或new_transaction
    private String operation;
    // work_id即watermark
    private int workId;
    // 主要是put的(<where>, <from>, <to>), 如果没有就"/"
    private String choices;

    public int getType() {
        return type;
    }

    public int getWorkId() {
        return workId;
    }

    public Log(int type, String operation, int workId, String choices){
        this.choices = choices;
        this.type = type;
        this.operation = operation;
        this.workId = workId;
    }

    @Override
    public String toString() {
        String answer = "";
        switch (type){
            case 1: answer += "BEGIN";break;
            case 2: answer += "CHANGE";break;
            case 3: answer += "OUTCOME";break;
        }
        answer += " " + operation + " " + workId + " ";
        if (!choices.equals("/"))
            answer += choices;
        return answer;
    }
}
