


package org.sunbird.notification.actor;

import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.util.*;

@ActorConfig(
        tasks = {JsonKey.READ_FEED},
        asyncTasks = {}
)
public class ReadNotificationFeedActor extends BaseActor {
    @Override
    public void onReceive(Request request) throws Throwable {
        Response response = new Response();
        List<Map<String,Object>> notifications = new ArrayList<>();
        Map<String, Object> addMemberNotification = getAddMemberNotification(request);
        notifications.add(addMemberNotification);
        notifications.add(getAddActivityNotification(request));
        notifications.add(getAddAdminActivityNotification(request));
        response.put("userFeed",notifications);
        sender().tell(response, getSelf());
    }

    private Map<String, Object> getAddMemberNotification(Request request) {
        Map<String,Object> addMemberNotification = new HashMap<>();
        addMemberNotification.put("id",UUID.randomUUID().toString());
        addMemberNotification.put("userId", request.getRequest().get("userId"));
        addMemberNotification.put("priority",1);
        addMemberNotification.put("createdBy",UUID.randomUUID().toString());
        addMemberNotification.put("status","unread");
        Map<String,Object> action = new HashMap<>();
        action.put("type","add-member");
        action.put("category","groups");
        Map<String,Object> template = new HashMap<>();
        template.put("type","JSON");
        template.put("ver","1.0");
        template.put("data","{\"title\": \"you have been added to Test by John\"}");
        action.put("template",template);
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put("id",UUID.randomUUID().toString());
        createdBy.put("type","User");
        createdBy.put("name","John");
        action.put("createdBy",createdBy);

        Map<String,Object> additionalInfo = new HashMap<>();
        Map<String,Object> group = new HashMap<>();
        group.put("id",UUID.randomUUID().toString());
        group.put("name","Test");
        additionalInfo.put("group",group);
        additionalInfo.put("groupRole","member");
        action.put("additionalInfo",additionalInfo);

        addMemberNotification.put("action",action);
        return addMemberNotification;
    }

    private Map<String, Object> getAddActivityNotification(Request request) {
        Map<String,Object> addMemberNotification = new HashMap<>();
        addMemberNotification.put("id",UUID.randomUUID().toString());
        addMemberNotification.put("userId", request.getRequest().get("userId"));
        addMemberNotification.put("priority",1);
        addMemberNotification.put("createdBy",UUID.randomUUID().toString());
        addMemberNotification.put("status","unread");
        Map<String,Object> action = new HashMap<>();
        action.put("type","activity-assigned");
        action.put("category","groups");
        Map<String,Object> template = new HashMap<>();
        template.put("type","JSON");
        template.put("ver","1.0");
        template.put("data","{\"title\": \"Math's has been assigned to the Test by John\"}");
        action.put("template",template);
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put("id",UUID.randomUUID().toString());
        createdBy.put("type","User");
        createdBy.put("name","John");
        action.put("createdBy",createdBy);

        Map<String,Object> additionalInfo = new HashMap<>();
        Map<String,Object> group = new HashMap<>();
        group.put("id",UUID.randomUUID().toString());
        group.put("name","Test");
        additionalInfo.put("group",group);
        additionalInfo.put("groupRole","member");

        Map<String,Object> activity= new HashMap<>();
        activity.put("id","do_113075936923082752112");
        activity.put("type","Course");
        activity.put("name","19991- Course with Self asses");
        additionalInfo.put("activity",activity);
        action.put("additionalInfo",additionalInfo);
        addMemberNotification.put("action",action);
        return addMemberNotification;
    }

    private Map<String, Object> getAddAdminActivityNotification(Request request) {
        Map<String,Object> addMemberNotification = new HashMap<>();
        addMemberNotification.put("id",UUID.randomUUID().toString());
        addMemberNotification.put("userId", request.getRequest().get("userId"));
        addMemberNotification.put("priority",1);
        addMemberNotification.put("createdBy",UUID.randomUUID().toString());
        addMemberNotification.put("status","unread");
        Map<String,Object> action = new HashMap<>();
        action.put("type","activity-assigned");
        action.put("category","groups");
        Map<String,Object> template = new HashMap<>();
        template.put("type","JSON");
        template.put("ver","1.0");
        template.put("data","{\"title\": \"Math's has been assigned to the Test by John\"}");
        action.put("template",template);
        Map<String,Object> createdBy = new HashMap<>();
        createdBy.put("id",UUID.randomUUID().toString());
        createdBy.put("type","User");
        createdBy.put("name","John");
        action.put("createdBy",createdBy);

        Map<String,Object> additionalInfo = new HashMap<>();
        Map<String,Object> group = new HashMap<>();
        group.put("id",UUID.randomUUID().toString());
        group.put("name","Test");
        additionalInfo.put("group",group);
        additionalInfo.put("groupRole","admin");

        Map<String,Object> activity= new HashMap<>();
        activity.put("id","do_113075936923082752112");
        activity.put("type","Course");
        activity.put("name","19991- Course with Self asses");
        additionalInfo.put("activity",activity);
        action.put("additionalInfo",additionalInfo);
        addMemberNotification.put("action",action);
        return addMemberNotification;
    }
}





