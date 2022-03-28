package Sys;

import Block.*;
import File.*;
import Exception.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class FileSystem {
    private static ArrayList<BlockManager> bms = new ArrayList<>();
    private static ArrayList<FileManager> fms = new ArrayList<>();
    private static int fileCount = 0;
    public static ArrayList<FileManager> getFms(){return fms;}
    public static ArrayList<BlockManager> getBms(){return bms;}
    public static BlockManager addManager(int bmId){
        BlockManager bm = new BlockManager(bmId);
        bms.add(bm);
        return bm;
    }
    public static BlockManager getBlockManager(int bmId) throws ErrorCode{
        for(BlockManager bm: bms){
            if (bm.getBmID() == bmId)
                return bm;
        }
        throw new ErrorCode(ErrorCode.BLOCK_MANAGER_DAMAGED);
    }
    public static FileManager getFileManager(int fmId) throws ErrorCode{
        for(FileManager fm: fms){
            if (fm.getFmID() == fmId)
                return fm;
        }
        throw new ErrorCode(ErrorCode.NO_SUCH_FM);
    }
    public static My_File getFile(int fileId) throws ErrorCode{
        My_File f = null;
        for(FileManager fm: fms){
            f = fm.getFile(fileId);
            if (f != null) return f;
        }
        throw new ErrorCode(ErrorCode.NO_SUCH_FILE);
    }

    public static void initialize(int bmNum, int fmNum) {
        if(!recoverBlockStructure()){
            for(int i = 0;i < bmNum;i++){
                bms.add(new BlockManager(i+1));
            }
        }
        if(!recoverFileStructure()){
            for(int i = 0;i < fmNum;i++){
                fms.add(new FileManager(i+1));
            }
        }
        for (FileManager fm: fms){
            fileCount += fm.fileCount();
        }
    }
    public static int getNewIndex(){
        return ++fileCount;
    }

    public static ArrayList<BlockManager> partition(int num) {
        ArrayList<BlockManager> blockManagers = new ArrayList<>();
        boolean[] flags = new boolean[bms.size()];
        Random random = new Random();
        int j;
        for(int i = 0;i < num;i++){
            do {
                j = random.nextInt(bms.size()) + 1;
            }while (flags[j - 1]);
            flags[j - 1] = true;
            blockManagers.add(getBlockManager(j));
        }
        return blockManagers;
    }

    private static boolean recoverBlockStructure() {
        File blockLevelDir = new File("FileSystem/Block");
        if (blockLevelDir.exists()){
            File[] blockManagers = blockLevelDir.listFiles();
            // 全部损坏了
            if (blockManagers == null)
                // 全部的bm均损坏
                return false;
            for(File f: blockManagers){
                BlockManager bm = new BlockManager(f);
                bms.add(bm);
            }
        }else return false;
        return true;
    }
    private static boolean recoverFileStructure(){
        File fileLevelDir = new File("FileSystem/File");
        if (fileLevelDir.exists()){
            File[] fileManagers = fileLevelDir.listFiles();
            if (fileManagers == null)
                return false;
            for(File f: fileManagers){
                FileManager  fm = new FileManager(f);
                fms.add(fm);
            }
        }else return false;
        return true;
    }
    public static void listStructure(){
        for(FileManager fm: fms){
            fm.listStructure();
        }
        for (BlockManager bm: bms){
            bm.listStructure();
        }
    }

}