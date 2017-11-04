package com.zgw.qgb.net.exception;

import java.io.IOException;

/**
 * 界面描述：异常类
 * <p>
 * Created by tianyang on 2017/9/27.
 */

public class ApiException extends IOException {
    public ApiException(String message) {
        super(message);
    }
}
