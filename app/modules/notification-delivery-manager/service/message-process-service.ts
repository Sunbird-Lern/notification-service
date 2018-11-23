import { EmailDeliveryService } from "./email-delivery-service";
export class MessageProcessService {
    public emailDeliveryService: EmailDeliveryService;
    constructor() {

    }
    public processMessage(msgData) {

        let receipients = this.getReceipients(msgData.receipientRefIds,msgData.receipientRefType, "email");
        // if the receipient is single send
        if (receipients.length == 1) {
            this.emailDeliveryService = new EmailDeliveryService({});
            this.emailDeliveryService.sendEmail().then((result) => {
                // based on success status upadate message delivery status to db
            }, (error) => {
                // if error add the message back to queue with retries count
            });
        } else {
            this.processGroupMessages();
        }

    }
    private getReceipients(refIds:Array<String>,refType: String, fieldToFetch: String): Array<String> {
        let receipients = [];
        // based on type get associated users from the provided reference ids and return list of user emails or mobile numbers
        switch (refType) {
            case 'BATCH': {

            }
                break;
            case 'USER': {

            }
                break;
        }
        return receipients;
    }
    private processGroupMessages() {
        // fanout group messages as individual and push back to queue and database
    }

    private saveMessageToDB(msg) {
        // use 'CassandraAdapter' to insert message details into DB
    }

    private addMessageToQueue(msg) {
        // use 'KafkaProducerAdapter' to insert message details into DB
    }

    private updateDeliveryStatus(msg) {
        // use 'CassandraAdapter' to update message delivery status in DB
    }

}