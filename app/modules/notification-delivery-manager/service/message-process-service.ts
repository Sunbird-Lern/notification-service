import { EmailDeliveryService } from "./email-delivery-service";
import { CassandraAdapter } from "../../shared";
export class MessageProcessService {
    public emailDeliveryService: EmailDeliveryService;
    constructor() {

    }
    public processMessage(msgData) {

        let models = CassandraAdapter.connect()
        if (models.instance.Messages) {
            models.instance.Messages.findOne({ id: models.uuidFromString(msgData.messageId) }, { raw: true, allow_filtering: true }, (err, message) => {
                if (err) {
                }
                switch (message.broadcast_type) {
                    case "email":
                        let reciepients = this.getReceipients(message.recipient_refid, message.recipientRefType, "email")
                        models.instance.Templates.findOne({ name: message.template_name }, { raw: true, allow_filtering: true }, (err, templateData) => {
                            if (err) {

                            }
                            let emailData = {
                                subject: "Batch notification update",
                                templateSubData: message.message_data,
                                templateText: templateData.template,
                                to: reciepients
                            }
                            this.emailDeliveryService = new EmailDeliveryService(emailData)
                            this.emailDeliveryService.sendEmail().then((result)=>{
                             console.log("result",result)   
                            })
                        })
                        break
                }
            })
        }
        else {
            console.log("Cassandra models not initialised.Try again !!!")
        }

    }
    private getReceipients(refIds: String, refType: String, fieldToFetch: String) {
        return "rajeev.sathish@tarento.com";
    }
}