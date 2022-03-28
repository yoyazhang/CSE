package File;

import Block.Block;
import Exception.ErrorCode;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FileManager {
    private int fmID;
    private ArrayList<My_File> managedFiles;

    @Override
    public String toString() {
        return "FileSystem/File/fm-" + fmID;
    }
    public My_File newFile(int fileId){
        My_File f = new My_File(this, fileId, this.toString());
        managedFiles.add(f);
        return f;
    }
    public int[] getFileIds() {
        int[] ids = new int[managedFiles.size()];
        for (int i = 0;i < ids.length;i++){
            ids[i] = managedFiles.get(i).getId();
        }
        return ids;
    }
    public int getFmID(){return fmID;}

    public My_File getFile(int fileId){
        for (My_File file : managedFiles) {
            if (file.getId() == fileId)
                return file;
        }
        return null;
    }
    public int fileCount(){return managedFiles.size();}
    // 恢复文件结构
    public FileManager(File file) {
        fmID = Integer.parseInt(file.getName().substring(3));
        managedFiles = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null) return;
        for(File b: files){
            // 只有meta文件可以转化
            if (b.getName().substring(b.getName().length() - 4).equals("meta")){
                try{
                    InputStream reader = new FileInputStream(b);
                    My_File recoveredFile = JSON.parseObject(new String(reader.readAllBytes()), My_File.class);
                    recoveredFile.setFM(this);
                    managedFiles.add(recoveredFile);
                }catch (IOException e){
                    throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                }
            }
        }
    }
    // 从零开始建设
    public FileManager(int id){
        this.fmID = id;
        managedFiles = new ArrayList<>();
        File dir = new File("FileSystem/File/fm-" + fmID);
        if (!dir.exists()) dir.mkdirs();
    }
    public void listStructure(){
        System.out.println("FM-" + fmID + ":");
        for(My_File f: managedFiles){
            f.listStructure();
        }
        System.out.println();
    }
}
