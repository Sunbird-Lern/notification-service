import * as express from "express";
import * as Joi from "joi";
import * as _ from "lodash";
import * as HttpStatus from "http-status-codes";
import { ResponseUtil } from '../../shared';
import { AppConstants } from '../../../config';
export class RequestMiddleware {
    constructor() {

    }
    public static validateRequest(req: express.Request, res: express.Response, next: express.NextFunction) {
        // based on diferent request write validation rules here
        next();

    }

    public static validateCreateNotificationRequest(req: express.Request, res: express.Response, next: express.NextFunction) {        
        const requestSchema = Joi.object().keys({
            request: Joi.object().keys({
                broadcastType: Joi.string().valid('email').required(),
                messageData:Joi.object().required(),
                messageType: Joi.string().valid('immediate','normal').required(),
                recipientRefId: Joi.string().required(),
                recipientRefType: Joi.string().valid('individual','batch').required(),
                templateName: Joi.string().required()
            }).required()
        })
        const result = Joi.validate(req.body, requestSchema)
        if (result.error && !_.isEmpty(result.error.message)) {
            let err = result.error.message
            let responseUtil = new ResponseUtil(AppConstants.API_IDS.CREATE_NOTIFICATION);
            res.status(HttpStatus.BAD_REQUEST);
            res.send(responseUtil.prepareErrorResponse(AppConstants.RESPONSE_CODES.CLIENT_ERROR,
                AppConstants.ERROR_CODES.REQUIRED_PARAMS_MISSING, err))
        } else {
            next();
        }
    }
}