import * as Kafka from "node-rdkafka";
export class KafkaConsumerAdapter {


    constructor(topic, cb) {
        let consumer = new Kafka.KafkaConsumer({
            'group.id': 'grp' + topic.toString(),
            'metadata.broker.list': 'localhost:9092',
            'enable.auto.commit': false, // don't commit my offset
        }, {
                'auto.offset.reset': 'earliest' // consume from the start
            });

        consumer.connect();

        consumer
            .on('ready', (args) => {
                console.log('consumer connected ' + args);
                consumer.subscribe(topic);
                consumer.consume();
            })
            .on('data', (data) => {
                cb(data)
                consumer.commit(data);
            });
            return consumer;
    }
}