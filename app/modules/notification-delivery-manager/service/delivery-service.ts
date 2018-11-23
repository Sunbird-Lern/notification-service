export class DeliveryService {
    constructor() {

    }

    // sends the composed message through the provided broadcast adapter(email or sms) 
    public deliverMessage(message, adapter) {
     return adapter.sendMesaage(message);
    }
}