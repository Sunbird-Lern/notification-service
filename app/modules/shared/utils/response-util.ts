import { AppConstants } from '../../../config';
import * as moment from 'moment';
export class ResponseUtil {
    public apiId: String;
    constructor(apiId) {
        this.apiId = AppConstants.API_PREFIX + apiId;
    }
    public prepareSuccessResponse(responseCode, responseData) {
        let successResponse = {
            'id': this.apiId,
            'ver': AppConstants.API_VERSION,
            'ts': moment().format('YYYY-MM-DD HH:mm:ss'),
            "params": {
                "resmsgid": null,
                "msgid": "",
                "err": "",
                "status": "success",
                "errmsg": null
            },
            responseCode: responseCode,
            result: responseData
        }
        return successResponse;
    }
    public prepareErrorResponse(responseCode, errorCode, error) {
        let errorResponse = {
            'id': this.apiId,
            'ver': AppConstants.API_VERSION,
            'ts': moment().format('YYYY-MM-DD HH:mm:ss'),
            "params": {
                "resmsgid": null,
                "msgid": "",
                "err": errorCode,
                "status": errorCode,
                "errmsg": error
            },
            responseCode: responseCode
        }
        return errorResponse;
    }

}