package com.venus.framework.rest;

import androidx.annotation.Nullable;

import com.venus.framework.util.L;
import com.venus.framework.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jubin on 16/1/15.
 */
public class ErrorMessage {

    int errorCode;

    String message;

    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public ErrorMessage setErrorCode(int errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ErrorMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return message;
    }

    @Nullable
    public static ErrorMessage parseFromJson(String jsonStr) {
        try {
            JSONObject jo = new JSONObject(jsonStr);

            int code = 0;
            if (jo.has("err_code")) {
                code = jo.optInt("err_code");
            } else if (jo.has("code")) {
                code = jo.optInt("code");
            }

            String msg = "";
            if (jo.has("err_msg")) {
                msg = jo.optString("err_msg");
            } else if (jo.has("message")) {
                msg = jo.optString("message");
            }

            if (Utils.isNotEmpty(msg) && msg.startsWith("{")) {
                JSONObject joErr = new JSONObject(msg);
                if (joErr.has("text")) {
                    msg = joErr.optString("text");
                }
            }

            return new ErrorMessage().setErrorCode(code).setMessage(msg);
        } catch (JSONException e) {
            L.e(e);
            return null;
        }
    }
}
