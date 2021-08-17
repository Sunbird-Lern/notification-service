package utils.module;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.auth.verifier.AccessTokenValidator;
import org.sunbird.request.HeaderParam;
import play.mvc.Http;
import controllers.JsonKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RequestInterceptor {
    private static ConcurrentHashMap<String, Short> apiHeaderIgnoreMap = new ConcurrentHashMap<>();

    private RequestInterceptor() {}

    static {
        short var = 1;
        apiHeaderIgnoreMap.put("/v1/notification/otp/verify", var);
        apiHeaderIgnoreMap.put("/v1/notification/send/sync", var);
        apiHeaderIgnoreMap.put("/v2/notification/send", var);
        apiHeaderIgnoreMap.put("/v1/notification/send",var);
        apiHeaderIgnoreMap.put("/health", var);
        apiHeaderIgnoreMap.put("/service/health",var);
    }

    /**
     * Authenticates given HTTP request context
     *
     * @param request HTTP play request
     * @return User or Client ID for authenticated request. For unauthenticated requests, UNAUTHORIZED
     *     is returned release-3.0.0 on-wards validating managedBy token.
     */
    public static Map verifyRequestData(Http.Request request) {
        Map userAuthentication = new HashMap<String, String>();
        userAuthentication.put(JsonKey.USER_ID, null);
        userAuthentication.put(JsonKey.MANAGED_FOR, null);

        String clientId = JsonKey.UNAUTHORIZED;
        String managedForId = null;
        Optional<String> accessToken = request.header(HeaderParam.X_Authenticated_User_Token.getName());
        // The API must be invoked with either access token or client token.
        if (!isRequestInExcludeList(request.path()) && !isRequestPrivate(request.path())) {
            if (accessToken.isPresent()) {
                clientId = AccessTokenValidator.verifyUserToken(accessToken.get());
                if (!JsonKey.USER_UNAUTH_STATES.contains(clientId)) {
                    // Now we have some valid token, next verify managed user token.
                    // LUA - MUA user combo, check the 'for' token and its parent, child identifiers
                    Optional<String> forTokenHeader =
                            request.header(HeaderParam.X_Authenticated_For.getName());
                    String managedAccessToken = forTokenHeader.isPresent() ? forTokenHeader.get() : "";
                    if (StringUtils.isNotEmpty(managedAccessToken)) {
                        String managedFor =
                                AccessTokenValidator.verifyManagedUserToken(managedAccessToken, clientId);
                        if (!JsonKey.USER_UNAUTH_STATES.contains(managedFor)) {
                            managedForId = managedFor;
                        } else {
                            clientId = JsonKey.UNAUTHORIZED;
                        }
                    }
                }
            }
            userAuthentication.put(JsonKey.USER_ID, clientId);
            userAuthentication.put(JsonKey.MANAGED_FOR, managedForId);

        } else {
            userAuthentication.put(JsonKey.USER_ID, JsonKey.ANONYMOUS);
        }
        return userAuthentication;
    }

    /**
     * Checks if request URL is in excluded (i.e. public) URL list or not
     *
     * @param requestUrl Request URL
     * @return True if URL is in excluded (public) URLs. Otherwise, returns false
     */
    public static boolean isRequestInExcludeList(String requestUrl) {
        boolean resp = false;
        if (!StringUtils.isBlank(requestUrl)) {
            if (apiHeaderIgnoreMap.containsKey(requestUrl)) {
                resp = true;
            } else {
                String[] splitPath = requestUrl.split("[/]");
                String urlWithoutPathParam = removeLastValue(splitPath);
                if (apiHeaderIgnoreMap.containsKey(urlWithoutPathParam)) {
                    resp = true;
                }
            }
        }
        return resp;
    }

    private static boolean isRequestPrivate(String path) {
        return path.contains(JsonKey.PRIVATE);
    }

    /**
     * Returns URL without path and query parameters.
     *
     * @param splitPath URL path split on slash (i.e. /)
     * @return URL without path and query parameters
     */
    private static String removeLastValue(String splitPath[]) {

        StringBuilder builder = new StringBuilder();
        if (splitPath != null && splitPath.length > 0) {
            for (int i = 1; i < splitPath.length - 1; i++) {
                builder.append("/" + splitPath[i]);
            }
        }
        return builder.toString();
    }
}
