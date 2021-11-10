package org.sunbird.utils;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;

public class ExceptionHandler {

    public static void handleExceptions(Request request, Exception ex){
        if(ex instanceof BaseException){
            throw  new BaseException((BaseException) ex);
        }else{
            throw new BaseException(IResponseMessage.SERVER_ERROR,IResponseMessage.SERVER_ERROR,ResponseCode.SERVER_ERROR.getCode());
        }
    }
}
