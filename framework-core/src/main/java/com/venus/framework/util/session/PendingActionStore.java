package com.venus.framework.util.session;

import com.venus.framework.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

/**
 * Created by ywu on 15/1/21.
 */
public class PendingActionStore {
    private final Map<String, Queue<Action>> pendingActions = new HashMap<>();

    public Queue<Action> getPendingActions(String key) {
        Queue<Action> actions = pendingActions.get(key);
        return actions == null ? new ArrayDeque<>() : actions;
    }

    public void queuePendingAction(String key, Action action) {
        Queue<Action> actions = pendingActions.get(key);
        if (actions == null) {
            actions = new ArrayDeque<>();
            pendingActions.put(key, actions);
        }

        if (!actions.contains(action)) {
            actions.offer(action);
        }
    }

    public void removePendingActions(String key) {
        pendingActions.remove(key);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jo = new JSONObject();
        for (Map.Entry<String, Queue<Action>> e : pendingActions.entrySet()) {
            Queue<Action> actions = e.getValue();
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            JSONArray arr = new JSONArray();
            jo.put(e.getKey(), actions);

            for (Action a : actions) {
                arr.put(a.toJson());
            }
        }
        return jo;
    }

    public static PendingActionStore fromJson(JSONObject jo) {
        PendingActionStore store = new PendingActionStore();

        Iterator<String> keys = jo.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            JSONArray arr = jo.optJSONArray(k);
            if (arr == null) {
                continue;
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject joAction = arr.optJSONObject(i);
                Action action = Action.fromJson(joAction);
                store.queuePendingAction(k, action);
            }
        }

        return store;
    }

    public String toJsonString() {
        JSONObject jo = new JSONObject();
        for (Map.Entry<String, Queue<Action>> e : pendingActions.entrySet()) {
            Queue<Action> actions = e.getValue();
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            try {
                JSONArray arr = new JSONArray();
                jo.put(e.getKey(), arr);

                for (Action a : actions) {
                    arr.put(a.toJson());
                }
            } catch (JSONException ex) {
                L.e("serializing pending action failed", ex);
            }
        }
        return jo.toString();
    }

    public static PendingActionStore fromJsonString(String jo) {
        try {
            return fromJson(new JSONObject(jo));
        } catch (JSONException e) {
            L.e("parse pending actions failed", e);
        }

        return new PendingActionStore();
    }

}
