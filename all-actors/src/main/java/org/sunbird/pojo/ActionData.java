package org.sunbird.pojo;

import java.util.Map;

public class ActionData {
    private String type;
    private String category;
    private Map<String,Object> template;
    private Map<String,String> createdBy;
    private Map<String,Object> additionalInfo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Map<String,Object> getTemplate() {
        return template;
    }

    public void setTemplate(Map<String,Object> template) {
        this.template = template;
    }

    public Map<String, String> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Map<String, String> createdBy) {
        this.createdBy = createdBy;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
