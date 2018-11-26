import * as nodemailer from 'nodemailer';

import { AppConfig } from '../../../config';
export class EmailAdapter {

    constructor() {

    }

    public sendMessage(messageData) {
        return new Promise((resolve, reject) => {
            let transporter = nodemailer.createTransport({
                service: 'gmail',
                auth: {
                    user: AppConfig.SMTP_CONFIG.USERNAME,
                    pass: AppConfig.SMTP_CONFIG.PASSWORD
                }
            });
            const mailOptions = {
                from: AppConfig.SMTP_CONFIG.USERNAME, // sender address
                to: messageData.to, // list of receivers
                subject: messageData.subject, // Subject line
                html: messageData.message// plain text body
            };
            transporter.sendMail(mailOptions, function (err, info) {
                if (err)
                    console.log(err)
                else
                    console.log(info);
            });
            resolve(true)
        });
    }
}