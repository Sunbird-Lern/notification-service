import * as express from "express";
import {SchedulerService} from "./service/schedule-service";
import {RequestMiddleware} from "./middleware/request-middleware";
export let SchedulerRouter = express.Router({mergeParams: true});
SchedulerRouter.get('/hello',RequestMiddleware.validateRequest,SchedulerService.helloWorld);
