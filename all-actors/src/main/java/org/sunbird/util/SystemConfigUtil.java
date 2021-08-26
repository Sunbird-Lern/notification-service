package org.sunbird.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sunbird.common.util.JsonKey;
import org.sunbird.common.response.Response;
import org.sunbird.request.LoggerUtil;
import org.sunbird.service.UserService;
import org.sunbird.service.UserServiceImpl;
import org.sunbird.common.util.LoggerEnum;

public class SystemConfigUtil {
    private static LoggerUtil logger = new LoggerUtil(SystemConfigUtil.class);
    private static UserService userService = UserServiceImpl.getInstance();
    private static Map<String, Object> custodianOrgDetails = new HashMap<>();

    public static void init() {
        cacheCustodianOrgDetails();
    }

    private static Map<String, String> getSystemSettingConfig() {
        Map<String, String> systemSettingConfig = new HashMap<>();
        Response response = userService.getSystemSettings();
        logger.info(
                "DataCacheHandler:cacheSystemConfig: Cache system setting fields" + response.getResult()+LoggerEnum.INFO.name());
        List<Map<String, Object>> responseList =
                (List<Map<String, Object>>) response.get(JsonKey.RESPONSE);
        if (null != responseList && !responseList.isEmpty()) {
            for (Map<String, Object> resultMap : responseList) {
                systemSettingConfig.put(
                        ((String) resultMap.get(JsonKey.FIELD)), (String) resultMap.get(JsonKey.VALUE));
            }
        }

        return systemSettingConfig;
    }

    private static void cacheCustodianOrgDetails() {
        Map<String, String> configSettings = getSystemSettingConfig();
        String custodianOrgId = configSettings.get(JsonKey.CUSTODIAN_ORG_ID);
        if (null != custodianOrgId) {
            Response response = userService.getOrganisationDetails(custodianOrgId);
            logger.info(
                    "DataCacheHandler:cacheSystemConfig: Cache system setting fields" + response.getResult()+LoggerEnum.INFO.name());
            if (null != response.getResult()
                    && null != response.getResult().get(JsonKey.RESPONSE)) {
                custodianOrgDetails = (Map<String, Object>) response.getResult().get(JsonKey.RESPONSE);
            }
        }
    }

    public static Map<String, Object> getCustodianOrgDetails() {
        return custodianOrgDetails;
    }

}
