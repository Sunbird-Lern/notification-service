import * as express from "express";
import {SchedulerService} from "./service/schedule-service";
import {RequestMiddleware} from "./middleware/request-middleware";
let schedulerServiceInstance = new SchedulerService()
export let SchedulerRouter = express.Router({mergeParams: true});
SchedulerRouter.get('/hello',RequestMiddleware.validateRequest,schedulerServiceInstance.helloWorld);
SchedulerRouter.post('/create',RequestMiddleware.validateCreateNotificationRequest,schedulerServiceInstance.createNotification);
