package Block;

import Exception.ErrorCode;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import Sys.FileSystem;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

public class Block {
    public static final int MAX_SIZE = 2;
    @JSONField(name = "id")
    private int id;
    @JSONField(name = "size")
    private int size;
    @JSONField(name="BlockManager", serialize=false, deserialize = false)
    private BlockManager BM;
    @JSONField(name = "duplicatedBlocks")
    private List<int[]> duplicatedBlocks;
    @JSONField(name = "metaPath")
    private String metaPath;
    @JSONField(name = "dataPath")
    private String dataPath;
    @JSONField(name = "checkSum")
    private String checkSum;

    public void setId(int id) { this.id = id; }
    public int getId(){return id;}
    public void setSize(int size) { this.size = size; }
    public int getSize(){return size;}

    public void setBM(BlockManager BM) { this.BM = BM; }
    public BlockManager getBM(){return BM;}

    public List<int[]> getDuplicatedBlocks() { return duplicatedBlocks; }
    public void setDuplicatedBlocks(List<int[]> duplicatedBlocks) { this.duplicatedBlocks = duplicatedBlocks; }

    public String getMetaPath() { return metaPath; }
    public void setMetaPath(String metaPath) { this.metaPath = metaPath; }

    public String getDataPath() { return dataPath; }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    // default constructor
    public Block(){}

    // 用于创造空块
    Block(int id, int size, BlockManager bm) {
        if(size > MAX_SIZE)
            throw new ErrorCode(ErrorCode.OUT_OF_SIZE);
        this.id = id;
        this.metaPath = bm.toString() + "/" + this.id + ".meta";
        this.dataPath = bm.toString() + "/" + this.id + ".data";
        this.size = size;
        this.duplicatedBlocks = new ArrayList<>();
        this.BM = bm;
        byte[] data = new byte[size];
        this.setCheckSum(data);
        writeData(data);
    }
    //用于生成有内容的块
    Block(int id, byte[] data, BlockManager bm){
        if(data.length > MAX_SIZE)
            throw new ErrorCode(ErrorCode.OUT_OF_SIZE);
        this.id = id;
        this.metaPath = bm.toString() + "/" + this.id + ".meta";
        this.dataPath = bm.toString() + "/" + this.id + ".data";
        this.size = data.length;
        this.duplicatedBlocks = new ArrayList<>();
        this.BM = bm;
        this.setCheckSum(data);
        writeData(data);
    }
    public void addDuplicateBlocks(ArrayList<Block> blocks){
        for (Block block : blocks) {
            if (block != this) {
                this.duplicatedBlocks.add(new int[]{block.getBM().getBmID(),block.getId()});
            }
        }
    }
    public void resetDuplicateBlocks(ArrayList<int[]> blocks){
        for(int[] ints: blocks){
            if (ints[0] != BM.getBmID() && ints[1] != id)
                duplicatedBlocks.add(new int[]{ints[0], ints[1]});
        }
    }

    public byte[] read(){
        // 检查此块是否还在使用
        // 检查此块的内容有无收到损坏
        byte[] content;
        try{
            InputStream reader = new FileInputStream(dataPath);
            content = reader.readAllBytes();
        } catch (IOException e) {
            throw new ErrorCode(ErrorCode.BLOCK_DATA_NOT_EXIST);
        }
        if(this.checkSum.equals(calCheckSum(content)))
            return content;
        else throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
    }
    void listStructure(){
        System.out.print("Block-" + id + "  ");
    }

    // 用于计算checksum
    private static String calCheckSum(byte[] source){
        byte[] secretBytes;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(source);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
    }
    private void setCheckSum(byte[] source){
        this.checkSum = calCheckSum(source);
    }
    boolean isCorrect(){
        byte[] content;
        try{
            InputStream reader = new FileInputStream(dataPath);
            content = reader.readAllBytes();
        } catch (IOException e) {
            throw new ErrorCode(ErrorCode.BLOCK_DATA_NOT_EXIST);
        }
        return this.checkSum.equals(calCheckSum(content));
    }

    public void writeMeta(){
        // 先写meta
        File metaFile = new File(metaPath);
        try{
            if(!metaFile.exists()){
                metaFile.getParentFile().mkdirs();
                metaFile.createNewFile();
            }
            OutputStream metaOut = new FileOutputStream(metaFile);
            metaOut.write((JSON.toJSONString(this)).getBytes());
            metaOut.close();
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

    }


    public void writeData(byte[] read){
        File dataFile = new File(dataPath);
        try{
            if(!dataFile.exists()){
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }
            OutputStream dataOut = new FileOutputStream(dataFile);
            dataOut.write(read);
            dataOut.close();
        }catch (IOException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }

    }

}
