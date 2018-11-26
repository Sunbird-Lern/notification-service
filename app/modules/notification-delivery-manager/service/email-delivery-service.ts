import { DeliveryService } from "./delivery-service";
import { EmailAdapter } from "../adapters/email-adapter";
import { parse, Compile } from 'velocityjs';
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
        let email = {
            to: this.emailData['to'],
            subject: this.emailData['subject'],
            message: message
        }
        return this.emailAdapter.sendMessage(email);
    }
    private getComposedMessage(emailData) {
        const asts = parse(emailData.templateText);
        const data = JSON.parse(emailData.templateSubData);
        const msg = (new Compile(asts)).render(data);
        // use velocity js to substitute appropriate details and return composed message object
        return msg;
    }

}