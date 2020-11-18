package com.gateway.connector.util;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
public class R {

    private int code;

    private String message;

    public static R error(String message){
        R r = new R(-1,message);
        return r;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
