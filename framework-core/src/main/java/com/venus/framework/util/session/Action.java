package com.venus.framework.util.session;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ywu on 15/1/21.
 */
public class Action extends SessionData {
    private static final String KEY_ID = "Action.id";
    private static final String KEY_ACTION = "Action.action";
    private String id;
    private String action;

    public Action() {
    }

    public Action(String action) {
        this.action = action;
    }

    public Action(String id, String action) {
        this.id = id;
        this.action = action;
    }

    public Action id(String id) {
        setId(id);
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Action putInt(String k, int v) {
        return (Action) super.putInt(k, v);
    }

    @Override
    public Action putLong(String k, long v) {
        return (Action) super.putLong(k, v);
    }

    @Override
    public Action putFloat(String k, float v) {
        return (Action) super.putFloat(k, v);
    }

    @Override
    public Action putDouble(String k, double v) {
        return (Action) super.putDouble(k, v);
    }

    @Override
    public Action putString(String k, String v) {
        return (Action) super.putString(k, v);
    }

    @Override
    public Action putBoolean(String k, boolean v) {
        return (Action) super.putBoolean(k, v);
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Action action(String action) {
        setAction(action);
        return this;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject jo = super.toJson();
        jo.putOpt(KEY_ID, id);
        jo.putOpt(KEY_ACTION, action);
        return jo;
    }

    public static Action fromJson(JSONObject jo) {
        Action action = new Action();
        action.setId(jo.optString(KEY_ID));
        action.setAction(jo.optString(KEY_ACTION));
        fromJson(action, jo);
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof Action) {
            Action that = (Action) o;
            return TextUtils.equals(id, that.id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
