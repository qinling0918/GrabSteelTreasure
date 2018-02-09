package com.zgw.qgb.net.download1;

/**
 * Name:ErrorCode
 * Created by Tsinling on 2018/2/9 16:19.
 * description:
 */

public enum  Error {

    CODE_NO_ERROR(-1,"未知错误"),
    CODE_NO_FILE_OR_DIR(0,"该本地路径不存在"),
    CODE_SERVER_RESPONSE_IS_EMPTY(2,"服务器返回值为空"),
    CODE_FILE_WRITE_FAILED(3,"写入文件失败"),
    CODE_OKHTTP_EXECUTE_IOEXCEPTION(4,"okhttp's execute() IOEXCEPTION"),
    CODE_REMOTE_FILE_NOT_EXIST(1,"目标文件不存在"),;

    private final int code;
    private final String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    Error(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
