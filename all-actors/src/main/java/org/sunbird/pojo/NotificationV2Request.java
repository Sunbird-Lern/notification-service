package org.sunbird.pojo;

import java.util.List;

public class NotificationV2Request {

    private List<String> ids;
    private int priority;
    private String type;
    private ActionData action;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ActionData getAction() {
        return action;
    }

    public void setAction(ActionData action) {
        this.action = action;
    }
}
