package test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import File.*;
import Block.*;
import Exception.ErrorCode;
import Sys.FileSystem;

class Test {
    public static void main(String[] args) throws IOException {
        /* * initialize your file system here * for example, initialize FileManagers and BlockManagers * and offer all the required interfaces * */ // test code
        //initialize:
        FileSystem.initialize(5,3);

        // write to file test:
        My_File file = FileSystem.getFileManager(1).newFile(FileSystem.getNewIndex()); // id为1的⼀个file
        file.write("FileSystem".getBytes(StandardCharsets.UTF_8));

        // read test:
        file.move(0,0);
        System.out.println(new String(file.read(file.getSize())));
        // array.toString only gets byte number, but you can try if you want
//        file.move(0,0);
//        System.out.println(Arrays.toString(file.read(file.getSize())));

        //write at head test
        file.move(0, file.getMOVE_HEAD());
        file.write("Smart".getBytes(StandardCharsets.UTF_8));
        file.move(0, file.getMOVE_HEAD());
        System.out.println(new String(file.read(file.getSize())));
        // array.toString only gets byte number, but you can try if you want
//        file.move(0,0);
//        System.out.println(Arrays.toString(file.read(file.getSize())));

        // write in middle test:
        file.move(5, file.getMOVE_HEAD());
        file.write("blahblah".getBytes(StandardCharsets.UTF_8));
        file.move(0, file.getMOVE_HEAD());
        System.out.println(new String(file.read(file.getSize())));

        // setSize test
        // larger
        file.setNewSize(100);
        file.move(0, file.getMOVE_HEAD());
        file.move(40,file.getMOVE_HEAD());
        file.write("aa".getBytes());
        file.move(0, file.getMOVE_HEAD());
        System.out.println("current size: " + file.getSize());
        System.out.println(new String(file.read(file.getSize())));
        // array.toString only gets byte number, but you can try if you want
//        file.move(0,0);
//        System.out.println(Arrays.toString(file.read(file.getSize())));

        // smaller
        file.setNewSize(16);
        file.move(0, file.getMOVE_HEAD());
        System.out.println("current size: " + file.getSize());
        System.out.println(new String(file.read(file.getSize())));
        // array.toString only gets byte number, but you can try if you want
//        file.move(0,0);
//        System.out.println(Arrays.toString(file.read(file.getSize())));

        test.Tools.smartLs();
//      here we will destroy a block, and you should handler this exception
//
//         destroy test
//         before test, structure:
        Tools.smartLs();

        My_File file1 = FileSystem.getFileManager(1).getFile(1);
        file1.move(0, file1.getMOVE_HEAD());
        System.out.println(new String(file1.read(file1.getSize())));
        // array.toString only gets byte number, but you can try if you want
//        file1.move(0,0);
//        System.out.println(Arrays.toString(file1.read(file1.getSize())));
        // after test, structure:
        Tools.smartLs();

        // copy test
        My_File file2 = Tools.smartCopy(1);
        Tools.smartLs();
        file2.move(0, file2.getMOVE_HEAD());
        System.out.println(new String(file2.read(file2.getSize())));

        // cat test:
        System.out.println(new String(Tools.smartCat(file2.getId())));

        // hex test:
        Tools.smartHex(1,3);

        // smart write test:
        Tools.smartWrite(0,file2.getMOVE_HEAD(), file2.getId());
        file2.move(0,file2.getMOVE_HEAD());
        System.out.println(new String(file2.read(file2.getSize())));
        file1.move(0,file1.getMOVE_HEAD());
        System.out.println(new String(file1.read(file1.getSize())));

        Tools.smartLs();
    }
}
public class Tools{
    // implements 4 smart-function
    public static String getData(int fileId) throws ErrorCode{
        My_File f = FileSystem.getFile(fileId);
        f.move(0,0);
        return new String(f.read(f.getSize()));
    }
    public static byte[] smartCat(int fileId){
        try{
            My_File f = FileSystem.getFile(fileId);
            f.move(0,0);
            return f.read(f.getSize());
        }catch (ErrorCode e){
            System.out.println("Oops! It seems you are messed up with some problems!");
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
            return null;
        }
    }
    public static void smartHex(int bmId, int blockId){
        try{
            Block b = FileSystem.getBlockManager(bmId).getBlock(blockId);
            System.out.println(bytesToHex(b.read()));
        }catch (ErrorCode e){
            System.out.println("Oops! It seems you are messed up with some problems!");
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }
    }

    public static void smartWrite(int offset, int where, int fileId){
        try{
            My_File file = FileSystem.getFile(fileId);
            file.move(offset,where);
            InputStreamReader reader = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(reader);
            try{
                file.write(br.readLine().getBytes());
            }catch (IOException e){
                throw new ErrorCode(ErrorCode.IO_EXCEPTION);
            }
        }catch (ErrorCode e){
            System.out.println("Oops! It seems you are messed up with some problems!");
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }
    }
    public static My_File smartCopy(int fileId){
        try{
            My_File src = FileSystem.getFile(fileId);
            My_File newFile = src.getFileManager().newFile(FileSystem.getNewIndex());
            newFile.copy(src);
            newFile.writeIntoFiles();
            return newFile;
        }catch (ErrorCode e){
            System.out.println("Oops! It seems you are messed up with some problems!");
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
            return null;
        }
    }
    public static void smartLs(){
        FileSystem.listStructure();
    }

    // 工具函数
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}