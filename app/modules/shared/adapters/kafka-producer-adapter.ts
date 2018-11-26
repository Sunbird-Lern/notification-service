import * as Kafka from "node-rdkafka"
export class KafkaProducerAdapter {
    private static kafkaProducerAdapter:any;
    constructor(){
        let producer = new Kafka.Producer({
            'client.id': 'kafka',
            'metadata.broker.list': 'localhost:9092',
            'compression.codec': 'gzip',
            'retry.backoff.ms': 200,
            'message.send.max.retries': 10,
            'socket.keepalive.enable': true,
            'queue.buffering.max.messages': 100000,
            'queue.buffering.max.ms': 1000,
            'batch.num.messages': 1000000,
            'dr_cb': true
        },null);
        // Connect to the broker manually
        producer.connect();
        producer.on('ready',  () => {
            console.log('Producer ready')
        })
        producer.on('event.error', (err) => {
            console.error('Error from producer');
            console.error(err);
        })
        return producer
    }

    public static connect(){
        // always return singleton instance of KafkaProducerAdapter
        if(!KafkaProducerAdapter.kafkaProducerAdapter){
            KafkaProducerAdapter.kafkaProducerAdapter = new KafkaProducerAdapter();
        }
        return KafkaProducerAdapter.kafkaProducerAdapter;
    }

    public static pushMessageToBroker(producer,data, cb) {
        try {
            var ret = producer.produce(
                // Topic to send the message to
                data.topic,
                // optionally we can manually specify a partition for the message
                // this defaults to -1 - which will use librdkafka's default partitioner (consistent random for keyed messages, random for unkeyed messages)
                null,
                // Message to send. Must be a buffer
                new Buffer(JSON.stringify(data.message)),
                // for keyed messages, we also specify the key - note that this field is optional
                data.messageKey,
                // you can send a timestamp here. If your broker version supports it,
                // it will get added. Otherwise, we default to 0
                Date.now()
                // you can send an opaque token here, which gets passed along
                // to your delivery reports
            );
            cb(null, ret)
    
        } catch (err) {
            console.error('A problem occurred when pushing your message');
            console.error(err);
            cb(err, null)
        }
    }
}