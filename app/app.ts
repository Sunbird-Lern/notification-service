import * as express from "express";
import * as bodyParser from "body-parser";
import { AppConfig } from "./config"
import { Routes } from "./routes";
import { CassandraAdapter, KafkaProducerAdapter, KafkaConsumerAdapter } from "./modules/shared";
import { MessageProcessService } from "./modules/notification-delivery-manager";
class App {

    public app: express.Application;
    public router = express.Router();
    public cassandraAdapter: CassandraAdapter;
    public kafkaProducerAdapter: KafkaProducerAdapter;
    public kafkaConsumerAdapter: KafkaConsumerAdapter;
    public messageProcessService:MessageProcessService;
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
        this.kafkaProducerAdapter = KafkaProducerAdapter.connect();
        this.messageProcessService = new MessageProcessService();
        this.kafkaConsumerAdapter = new KafkaConsumerAdapter(AppConfig.KAFKA_TOPICS.split(','), (data) => {
            console.log("kafka consumer data",data);
            this.messageProcessService.processMessage(JSON.parse(data.value.toString()));
        })

    }

}

export default new App().app;