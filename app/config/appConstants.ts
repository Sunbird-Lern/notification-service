export abstract class AppConstants {
    public static API_PREFIX = 'sunbird.notification.';
    public static API_VERSION = 'v1';
    public static API_IDS = {
        CREATE_NOTIFICATION: 'create'
    }
    public static RESPONSE_CODES = {
        SERVER_ERROR: 'SERVER_ERROR',
        CLIENT_ERROR: 'CLIENT_ERROR',
        BAD_REQUEST: 'BAD_REQUEST',
        OK:'OK'
    }
    public static ERROR_CODES = {
        REQUIRED_PARAMS_MISSING : 'REQUIRED_PARAMS_MISSING',
        SOMETHING_WENT_WRONG:'SOMETHING_WENT_WRONG'
    }
}