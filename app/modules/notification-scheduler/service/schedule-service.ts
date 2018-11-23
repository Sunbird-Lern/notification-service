import * as express from "express";
export class SchedulerService {
    public static helloWorld(req:express.Request,res:express.Response):void{
      res.send({status:"success",message:"hello world"});
    }

    public static createNotification(req:express.Request,res:express.Response):void{
      // first save the notification details to cassandra and then push to notification queue and return response
    }


}
