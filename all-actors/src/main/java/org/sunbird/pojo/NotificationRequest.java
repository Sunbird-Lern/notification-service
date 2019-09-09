package org.sunbird.pojo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class NotificationRequest {

    /**
     * required , Mode in which the notification is sent
     */
    private String mode;
    /**
     * required
     */
    private String deliveryType;
    /**
     * configuration data needed for the notification
     */
    private Config config;

    /**
     * required
     */
    private String[] ids;

    private Template template;

    private JsonNode rawData;

    private ObjectMapper mapper = new ObjectMapper();

    public NotificationRequest() {
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public JsonNode getRawData() {
        return rawData;
    }

    public void setRawData(JsonNode rawData) {
        this.rawData = rawData;
    }

    @Override
    public String toString() {
        String stringRep = null;
        try {
            stringRep = mapper.writeValueAsString(this);
        } catch (JsonProcessingException jpe) {
            jpe.printStackTrace();
        }
        return stringRep;
    }
}
