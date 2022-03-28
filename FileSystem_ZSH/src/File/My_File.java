package File;

import Block.*;

import Exception.ErrorCode;
import Sys.FileSystem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class My_File {
    @JSONField(deserialize = false, serialize = false)
    public static final int DUPLICATE_BLOCK_NUM = 3;
    private int MOVE_CURR = 0; //只是光标的三个枚举值，具体数值⽆实际意义
    private int MOVE_HEAD = 0;
    private int MOVE_TAIL = 0;
    private int indexInBlock = 0;
    private int curLevelIndex = 0;
    @JSONField(deserialize = false, serialize = false)
    private FileManager FM;
    private int id;
    private String metaPath;
    private ArrayList<ArrayList<int[]>> logicBlocks;

    public static int getDuplicateBlockNum() {
        return DUPLICATE_BLOCK_NUM;
    }

    public int getMOVE_CURR() {
        return MOVE_CURR;
    }

    public void setMOVE_CURR(int MOVE_CURR) {
        this.MOVE_CURR = MOVE_CURR;
    }

    public int getMOVE_HEAD() {
        return MOVE_HEAD;
    }

    public void setMOVE_HEAD(int MOVE_HEAD) {
        this.MOVE_HEAD = MOVE_HEAD;
    }

    public int getMOVE_TAIL() {
        return MOVE_TAIL;
    }

    public void setMOVE_TAIL(int MOVE_TAIL) {
        this.MOVE_TAIL = MOVE_TAIL;
    }

    public int getIndexInBlock() {
        return indexInBlock;
    }

    public void setIndexInBlock(int indexInBlock) {
        this.indexInBlock = indexInBlock;
    }

    public int getCurLevelIndex() {
        return curLevelIndex;
    }

    public void setCurLevelIndex(int curLevelIndex) {
        this.curLevelIndex = curLevelIndex;
    }

    public FileManager getFM() {
        return FM;
    }

    public void setFM(FileManager FM) {
        this.FM = FM;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMetaPath() {
        return metaPath;
    }

    public void setMetaPath(String metaPath) {
        this.metaPath = metaPath;
    }

    public ArrayList<ArrayList<int[]>> getLogicBlocks() {
        return logicBlocks;
    }

    public void setLogicBlocks( ArrayList<ArrayList<int[]>> logicBlocks) {
        this.logicBlocks = logicBlocks;
    }

    // 为恢复提供的默认constructor
    public My_File(){}

    public void writeIntoFiles(){
        // 先写meta
        File metaFile = new File(metaPath);
        try{
            if(!metaFile.exists()){
                metaFile.getParentFile().mkdirs();
                metaFile.createNewFile();
            }
            OutputStream metaOut = new FileOutputStream(metaFile);
            // test
            String json = JSON.toJSONString(this);
            metaOut.write(json.getBytes());
            metaOut.close();
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

    }
    public My_File(FileManager FM, int id, String fatherPath){
        this.id = id;
        System.out.println("id: "+ this.id);
        this.FM = FM;
        this.logicBlocks = new ArrayList<>();
        this.metaPath= fatherPath + "/" + id + ".meta";
        writeIntoFiles();
    }
    public FileManager getFileManager(){
        return this.FM;
    }
    public void listStructure() {
        System.out.print("File-" + id + "  ");
    }

    public byte[] read(int length){
        int toMove = length + MOVE_CURR;
        if (toMove > getSize())
            throw new ErrorCode(ErrorCode.READ_OUT_OF_RANGE);
        byte[] content = new byte[length];
        int hasRead = 0;

        while(length > 0){
            for(;curLevelIndex < logicBlocks.size();curLevelIndex++){
                Block block = getCorrectBlockOfLevel(curLevelIndex);
                if(length < block.getSize()){
                    System.arraycopy(block.read(),indexInBlock,content,hasRead,length);
                    indexInBlock += length;
                    hasRead += length;
                    length = 0;
                }else{
                    System.arraycopy(block.read(),indexInBlock,content,hasRead,block.getSize());
                    indexInBlock = 0;
                    hasRead += block.getSize();
                    length -= block.getSize();
                }
            }
        }
        move(toMove, 0);
        return content;
    }
    public void write(byte[] b){
        //首先要把块分割
        boolean writeAtEnd = true;
        boolean doSplit = false;
        // 由于保证原子性，所以block可以先产完，但是写入必须是最后
        ArrayList<ArrayList<Block>> toAppendList = new ArrayList<>();
        try{
            if(MOVE_CURR < MOVE_TAIL){
                // 属于中间插入，但是也要考虑是不是正好在某个块的0和尾，不是的话才需要分割。
                writeAtEnd = false;
                Block originalBlock = getCorrectBlockOfLevel(curLevelIndex);
                if (indexInBlock == originalBlock.getSize()) curLevelIndex++;
                if(indexInBlock != 0 && indexInBlock != originalBlock.getSize()){
                    doSplit = true;
                    byte[] firstPart = new byte[indexInBlock];
                    byte[] lastPart = new byte[originalBlock.getSize() - indexInBlock];

                    System.arraycopy(originalBlock.read(),0,firstPart,0,firstPart.length);
                    System.arraycopy(originalBlock.read(),indexInBlock,lastPart,0,lastPart.length);

                    toAppendList.add(generateLogicLevel(firstPart));
                    toAppendList.add(generateLogicLevel(lastPart));
                }
            }
            int toNewNum = b.length / Block.MAX_SIZE + 1;
            int pointerInB = 0;
            for(int i = 0;i < toNewNum;i++){
                if (i != toNewNum - 1) {
                    byte[] content = new byte[Block.MAX_SIZE];
                    System.arraycopy(b, pointerInB,content,0,content.length);
                    toAppendList.add(generateLogicLevel(content));
                    pointerInB += Block.MAX_SIZE;
                }
                else {
                    byte[] content = new byte[b.length % Block.MAX_SIZE];
                    System.arraycopy(b, pointerInB,content,0,content.length);
                    toAppendList.add(generateLogicLevel(content));
                }
            }
        }catch (ErrorCode e){
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
            System.out.println("writing to this file fails!");
            return;
        }
        // 确定没问题了才能写入
        if (doSplit){
            appendLogicBlocks(toAppendList.get(0), curLevelIndex++);
            appendLogicBlocks(toAppendList.get(1), curLevelIndex++);
            removeLevel(curLevelIndex);
            // 移除前两个的
            toAppendList.remove(0);
            toAppendList.remove(0);
            curLevelIndex -= 1;
        }
        for(ArrayList<Block> blocks: toAppendList){
            if (writeAtEnd){
                appendLogicBlocks(blocks);
            }else{
                appendLogicBlocks(blocks, curLevelIndex++);
            }
        }
        MOVE_TAIL = getSize();
        move(b.length, MOVE_CURR);
        writeIntoFiles();
    }


    public int pos(){
        return move(0, MOVE_CURR);
    }
    public int move(int offset, int where){
        if (offset + where > MOVE_TAIL) {
            throw new ErrorCode(ErrorCode.MOVE_OUT_OF_RANGE);
        }
        MOVE_CURR = offset + where;
        int pointer = 0;
        curLevelIndex = 0;
        indexInBlock = 0;
        int i = 0;
        while(pointer < MOVE_CURR){
            int blockSize = getCorrectBlockOfLevel(i).getSize();
            if (pointer + blockSize >= MOVE_CURR){
                indexInBlock = MOVE_CURR - pointer;
                break;
            }else{
                curLevelIndex++;
                pointer += blockSize;
                i++;
            }
        }
        return MOVE_CURR;
    }//把⽂件光标移到距离where offset个byte的位置，并返回⽂件光标所在位置

    // 移除某位置的引用
    private void removeLevel(int level){
        logicBlocks.remove(level);
    }

    // 制定插入层数
    private void appendLogicBlocks(ArrayList<Block> blocks, int level){
        ArrayList<int[]> toInsert = new ArrayList<>();
        for(Block b: blocks){
            toInsert.add(new int[]{b.getBM().getBmID(), b.getId()});
        }
        logicBlocks.add(level, toInsert);
    }
    private void appendLogicBlocks(ArrayList<Block> blocks){
        ArrayList<int[]> toInsert = new ArrayList<>();
        for(Block b: blocks){
            toInsert.add(new int[]{b.getBM().getBmID(), b.getId()});
        }
        logicBlocks.add(toInsert);
    }

    private ArrayList<Block> generateLogicLevel(int size){
        ArrayList<BlockManager> bms = FileSystem.partition(DUPLICATE_BLOCK_NUM);
        ArrayList<Block> bs = new ArrayList<>();
        for (BlockManager bm: bms){
            Block b = bm.newEmptyBlock(size);
            bs.add(b);
        }
        for (Block b: bs){
            b.addDuplicateBlocks(bs);
        }
        for (Block b: bs){
            b.writeMeta();
        }
        return bs;
    }
    private ArrayList<Block> generateLogicLevel(byte[] content) {
        ArrayList<BlockManager> bms = FileSystem.partition(DUPLICATE_BLOCK_NUM);
        ArrayList<Block> bs = new ArrayList<>();
        for (BlockManager bm: bms){
            Block b = bm.newBlock(content);
            bs.add(b);
        }
        for (Block b: bs){
            b.addDuplicateBlocks(bs);
        }
        for (Block b: bs){
            b.writeMeta();
        }
        return bs;
    }

    public void setNewSize(int newSize) {
        int originalSize = getSize();
        if (newSize > originalSize){
            int furtherSize = newSize - originalSize;
            int toNewNum = furtherSize / Block.MAX_SIZE + 1;
            ArrayList<ArrayList<Block>> toAppendList = new ArrayList<>();
            try{
                for(int i = 0;i < toNewNum;i++){
                    if (i != toNewNum - 1) toAppendList.add(generateLogicLevel(Block.MAX_SIZE));
                    else toAppendList.add(generateLogicLevel((furtherSize % Block.MAX_SIZE)));
                }
            }catch (ErrorCode e){
                System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
                System.out.println("setting size fails!");
                return;
            }
            // 确定没问题了 改
            for(ArrayList<Block> blocks: toAppendList){
                appendLogicBlocks(blocks);
            }
        }else if (newSize < originalSize){
            ArrayList<Integer> toRemoveLevel = new ArrayList<>();
            ArrayList<Block> toAppendBlock = null;
            try{
                for(int i = logicBlocks.size() - 1;i >= 0;i--){
                    int toRemove = getCorrectBlockOfLevel(i).getSize();
//                System.out.println("toremoveSize: " + toRemove);
                    if(originalSize - toRemove < newSize){
                        // 对其中部分内容的
//                    System.out.println("yes want to split");
                        Block b = getCorrectBlockOfLevel(i);
                        byte[] content = new byte[toRemove - (originalSize - newSize)];
                        System.arraycopy(b.read(),0,content,0,content.length);
                        toRemoveLevel.add(i);
                        toAppendBlock = generateLogicLevel(content);
                        break;
                    }else{
                        originalSize -= toRemove;
                        toRemoveLevel.add(i);
                    }
                }
            }catch (ErrorCode e){
                System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
                System.out.println("writing to this file fails!");
                return;
            }
            // 确定没问题了
            for (Integer i: toRemoveLevel){
                removeLevel(i);
            }
            if (toAppendBlock != null)
                appendLogicBlocks(toAppendBlock);
        }
        MOVE_TAIL = getSize();
        writeIntoFiles();
    }

    private Block getCorrectBlockOfLevel(int level){
        Block b = null;
        byte[] content = null;
        boolean needToRepair = false;
        ArrayList<int[]> needRepairData = new ArrayList<>();
        ArrayList<int[]> needRepairBMAndBlock = new ArrayList<>();
        ArrayList<int[]> needRepairBlock = new ArrayList<>();
        for (int[] ints: logicBlocks.get(level)){
            try{
                // 一共有3种坏的可能：bm被删了，block被删了，block checksum出问题
                b = FileSystem.getBlockManager(ints[0]).getBlock(ints[1]);
                content = b.read();
                break;
            }catch (ErrorCode e){
                // try to repair
                needToRepair = true;
                switch (e.getErrorCode()){
                    case ErrorCode.BLOCK_MANAGER_DAMAGED: needRepairBMAndBlock.add(ints); break;
                    case ErrorCode.CHECKSUM_CHECK_FAILED: case ErrorCode.BLOCK_DATA_NOT_EXIST: needRepairData.add(ints); break;
                    case ErrorCode.PHYSICAL_BLOCK_DAMAGED: needRepairBlock.add(ints); break;
                }
            }
        }
        // 全部的冗余块都损坏，回天乏力
        if (content == null) throw new ErrorCode(ErrorCode.LOGIC_BLOCK_DAMAGED);
        if (needToRepair){
            for(int[] ints: needRepairData){
                Block toRepair = FileSystem.getBlockManager(ints[0]).getBlock(ints[1]);
                toRepair.writeData(content);
            }
            for (int[] ints: needRepairBlock){
                Block newBlock = FileSystem.getBlockManager(ints[0]).recoverBlock(ints[1], content);
                newBlock.resetDuplicateBlocks(logicBlocks.get(level));
                newBlock.writeMeta();
            }
            for (int[] ints: needRepairBMAndBlock) {
                BlockManager bm = FileSystem.addManager(ints[0]);
                Block newBlock = bm.recoverBlock(ints[1], content);
                newBlock.resetDuplicateBlocks(logicBlocks.get(level));
                newBlock.writeMeta();
            }
        }
        return b;
    }

    public int getSize() throws ErrorCode{
        int totalSize = 0;
        for(int i = 0;i < logicBlocks.size();i++){
            Block b = getCorrectBlockOfLevel(i);
//            try{
//
//            }catch (ErrorCode e){
//                System.out.println("All of the duplicated blocks are damaged!");
//            }
            totalSize += b.getSize();
        }
        return totalSize;
    }

    public void copy(My_File src){
        for (ArrayList<int[]> level: src.getLogicBlocks()){
            ArrayList<int[]> tmp = new ArrayList<>();
            for (int[] ints: level){
                tmp.add(new int[]{ints[0],ints[1]});
            }
            logicBlocks.add(tmp);
        }
        MOVE_TAIL = getSize();
    }
}
