package com.zgw.qgb.ui.moudle.main;

/**
 * Name:BaseBean
 * Created by Tsinling on 2018/3/15 13:22.
 * description:
 */

public class BaseBean {

    /**
     * result : 0
     * msg : 请输入有效的手机号码
     */

    private int result;
    private String msg;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
