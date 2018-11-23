import * as express from "express";
import * as bodyParser from "body-parser";
import { Routes } from "./routes";
import { CassandraAdapter } from "./modules/shared";
class App {

    public app: express.Application;
    public router = express.Router();
    public cassandraAdapter: CassandraAdapter;
    constructor() {
        this.app = express();
        this.config();
    }

    private config(): void {
        // support application/json type post data
        this.app.use(bodyParser.json());
        //support application/x-www-form-urlencoded post data
        this.app.use(bodyParser.urlencoded({ extended: false }));
        this.router = Routes.configure();
        this.app.use("/api/v1", this.router);
        this.cassandraAdapter = CassandraAdapter.connect();

    }

}

export default new App().app;