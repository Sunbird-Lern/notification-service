import { DeliveryService } from "./delivery-service";
import { EmailAdapter } from "../adapters/email-adapter";
export class EmailDeliveryService extends DeliveryService {
    public emailAdapter: EmailAdapter;
    public emailData: Object;
    constructor(emailData: Object) {
        super()
        this.emailAdapter = new EmailAdapter()
        this.emailData = emailData;
    }
    public sendEmail() {
        let message = this.getComposedMessage(this.emailData);
        //super class method
        return this.deliverMessage(message, this.emailAdapter);
    }
    private getComposedMessage(emailData) {
        // use velocity js to substitute appropriate details and return composed message object
        return {};
    }

}