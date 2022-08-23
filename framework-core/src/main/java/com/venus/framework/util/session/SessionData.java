package com.venus.framework.util.session;

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Key-value data store
 * Created by ywu on 15/1/21.
 */
public class SessionData implements Serializable {

    private Bundle data;

    protected Bundle getData() {
        if (data == null) {
            data = new Bundle();
        }

        return data;
    }

    public SessionData putInt(String k, int v) {
        getData().putInt(k, v);
        return this;
    }

    public int getInt(String k) {
        return getData().getInt(k);
    }

    public SessionData putLong(String k, long v) {
        getData().putLong(k, v);
        return this;
    }

    public long getLong(String k) {
        return getData().getLong(k);
    }

    public SessionData putFloat(String k, float v) {
        getData().putFloat(k, v);
        return this;
    }

    public float getFloat(String k) {
        return getData().getFloat(k);
    }

    public SessionData putDouble(String k, double v) {
        getData().putDouble(k, v);
        return this;
    }

    public double getDouble(String k) {
        return getData().getDouble(k);
    }

    public SessionData putString(String k, String v) {
        getData().putString(k, v);
        return this;
    }

    public String getString(String k) {
        return getData().getString(k);
    }

    public SessionData putBoolean(String k, boolean v) {
        getData().putBoolean(k, v);
        return this;
    }

    public boolean getBoolean(String k) {
        return getData().getBoolean(k);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jo = new JSONObject();

        if (data != null) {
            for (String k : data.keySet()) {
                jo.putOpt(k, data.get(k));
            }
        }

        return jo;
    }

    public static SessionData fromJson(JSONObject jo) {
        SessionData store = new SessionData();
        fromJson(store, jo);
        return store;
    }

    protected static void fromJson(SessionData store, JSONObject jo) {
        Iterator<String> keys = jo.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            Object o = jo.opt(k);

            if (o instanceof String) {
                store.putString(k, (String) o);
            } else if (o instanceof Integer || int.class.isInstance(o)) {
                store.putInt(k, (int) o);
            } else if (o instanceof Float || float.class.isInstance(o)) {
                store.putFloat(k, (float) o);
            } else if (o instanceof Double || double.class.isInstance(o)) {
                store.putDouble(k, (double) o);
            } else if (o instanceof Long || long.class.isInstance(o)) {
                store.putLong(k, (long) o);
            } else if (o instanceof Boolean || boolean.class.isInstance(o)) {
                store.putBoolean(k, (boolean) o);
            }
        }
    }
}
