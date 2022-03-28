package Block;

import Exception.ErrorCode;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.ArrayList;

public class BlockManager {
    private int bmID;
    private ArrayList<Block> managedBlocks;

    @Override
    public String toString() {
        return "FileSystem/Block/bm-" + bmID;
    }

    public BlockManager(File file){
        bmID = Integer.parseInt(file.getName().substring(3));
        managedBlocks = new ArrayList<>();
        File[] blocks = file.listFiles();
        if (blocks == null) return;
        for(File b: blocks){
            // 只有meta文件可以转化
            try{
                if (b.getName().substring(b.getName().length() - 4).equals("meta")){
                    InputStream reader = new FileInputStream(b);
                    Block recoveredBlock = JSON.parseObject(new String(reader.readAllBytes()),Block.class);
                    recoveredBlock.setBM(this);
                    managedBlocks.add(recoveredBlock);
                }
            }catch (IOException e){
                throw new ErrorCode(ErrorCode.IO_EXCEPTION);
            }
        }
    }

    public BlockManager(int id){
        this.bmID = id;
        managedBlocks = new ArrayList<>();
        File dir = new File("FileSystem/Block/bm-" + bmID);
        if (!dir.exists()) dir.mkdirs();
    }
    public void listStructure(){
        System.out.println("BM-" + bmID + ":");
        for(Block b: managedBlocks){
            b.listStructure();
        }
        System.out.println();
    }

    public Block getBlock(int index) throws ErrorCode{
        Block block = null;
        for(Block b: managedBlocks){
            if (b.getId() == index)
                block = b;
        }
        if (block == null) throw new ErrorCode(ErrorCode.PHYSICAL_BLOCK_DAMAGED);
        return block;
    }
    //此时应该保证传入的参数一定是小于等于一个block的size大小的
    public Block newBlock(byte[] b){
        Block block = new Block(generateId(), b, this);
        managedBlocks.add(block);
        return block;
    }

    private int generateId(){
        int max = 0;
        for (Block b: managedBlocks){
            if (b.getId() > max)
                max = b.getId();
        }
        return max + 1;
    }

    public Block recoverBlock(int oldId, byte[] b){
        Block block = new Block(oldId, b, this);
        managedBlocks.add(block);
        return block;
    }

    public int getBmID(){return bmID;}
    public Block newEmptyBlock(int blockSize){
        Block emptyBlock = new Block(generateId(), blockSize, this);
        managedBlocks.add(emptyBlock);
        return emptyBlock;
    }

    public int[] getBlockIds() {
        int[] ids = new int[managedBlocks.size()];
        for (int i = 0;i < ids.length;i++){
           ids[i] = managedBlocks.get(i).getId();
        }
        return ids;
    }
}
