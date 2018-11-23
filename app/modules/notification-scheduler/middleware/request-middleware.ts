import * as express from "express";
export class RequestMiddleware {
    constructor(){

    }
    public static validateRequest(req:express.Request,res:express.Response,next:express.NextFunction){
        // based on diferent request write validation rules here
        next();
        
    }
}