import * as express from "express";
import { SchedulerRouter } from './modules'
export class Routes {
    public static configure():express.Router {
        let router = express.Router();
        router.use("/notification", SchedulerRouter);
        return router;
    }
}