package Exception;

import java.util.HashMap;
import java.util.Map;

public class ErrorCode extends RuntimeException{
    public static final int IO_EXCEPTION = 1;
    public static final int CHECKSUM_CHECK_FAILED = 2;
    public static final int BLOCK_NOT_INUSE = 3;
    public static final int OUT_OF_SIZE = 4;
    public static final int READ_OUT_OF_RANGE = 5;
    public static final int NO_SUCH_FILE = 6;
    public static final int LOGIC_BLOCK_DAMAGED = 7;
    public static final int PHYSICAL_BLOCK_DAMAGED = 8;
    public static final int NO_SUCH_RANGE_GLOBAL = 9;
    public static final int WRITE_TO_FILE_ERROR = 10;
    public static final int BLOCK_DATA_NOT_EXIST = 11;
    public static final int BLOCK_MANAGERS_DAMAGED = 12;
    public static final int BLOCK_MANAGER_DAMAGED = 13;
    public static final int MOVE_OUT_OF_RANGE = 14;
    public static final int NO_SUCH_FM = 15;
    // ... and more
    public static final int UNKNOWN = 1000;
    private static final Map<Integer, String> ErrorCodeMap = new HashMap<>();
    static {
        ErrorCodeMap.put(IO_EXCEPTION, "IO exception");
        ErrorCodeMap.put(CHECKSUM_CHECK_FAILED, "block checksum check failed");
        ErrorCodeMap.put(BLOCK_NOT_INUSE,"this block is no longer in use");
        ErrorCodeMap.put(UNKNOWN, "unknown");
        ErrorCodeMap.put(OUT_OF_SIZE, "out of the block's max size");
        ErrorCodeMap.put(READ_OUT_OF_RANGE, "read length out of the file's length");
        ErrorCodeMap.put(LOGIC_BLOCK_DAMAGED, "All blocks of this logic part are damaged!");
        ErrorCodeMap.put(PHYSICAL_BLOCK_DAMAGED, "This block is damaged!");
        ErrorCodeMap.put(NO_SUCH_RANGE_GLOBAL, "No such file in global range!");
        ErrorCodeMap.put(WRITE_TO_FILE_ERROR, "errors occurred when writing to file！");
        ErrorCodeMap.put(BLOCK_DATA_NOT_EXIST, "Can's find this block!");
        ErrorCodeMap.put(BLOCK_MANAGERS_DAMAGED, "All block managers are damaged!");
        ErrorCodeMap.put(BLOCK_MANAGER_DAMAGED, "this block manager is damaged!");
        ErrorCodeMap.put(MOVE_OUT_OF_RANGE, "attempt to move out of the file's length!");
        ErrorCodeMap.put(NO_SUCH_FM, "no this file manager!");

    }
    public static String getErrorText(int errorCode) {
        return ErrorCodeMap.getOrDefault(errorCode, "invalid");
    }
    private int errorCode;
    public ErrorCode(int errorCode) {
        super(String.format("error code '%d' \"%s\"", errorCode, getErrorText(errorCode)));
        this.errorCode = errorCode;
    }
    public int getErrorCode() {  return errorCode; }
}
