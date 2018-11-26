import * as express from "express";
import * as moment from "moment";
import { CassandraAdapter, ResponseUtil, KafkaProducerAdapter } from "../../shared";
import * as HttpStatus from "http-status-codes";
import { AppConstants } from '../../../config';
export class SchedulerService {

  constructor() {

  }

  public saveNotificationToDB(reqObj) {


  }


  public helloWorld(req: express.Request, res: express.Response): void {
    res.send({ status: "success", message: "hello world" });
  }

  public createNotification(req: express.Request, res: express.Response): void {

    let reqObj = req.body.request;

    // validate the template data here

    // store to cassandra
    let models = CassandraAdapter.connect()
    let currentTimestamp = moment().valueOf()
    let message = new models.instance.Messages({
      broadcast_type: reqObj.broadcastType,
      created_on: currentTimestamp,
      message_data: JSON.stringify(reqObj.messageData),
      message_type: reqObj.messageType,
      recipient_refid: reqObj.recipientRefId,
      recipient_reftype: reqObj.recipientRefType,
      status: 'pending',
      template_name: reqObj.templateName,
      updated_on: currentTimestamp
    });
    message.save((err) => {
      if (err) {
        let responseUtil = new ResponseUtil(AppConstants.API_IDS.CREATE_NOTIFICATION);
        res.status(HttpStatus.INTERNAL_SERVER_ERROR);
        res.send(responseUtil.prepareErrorResponse(AppConstants.RESPONSE_CODES.SERVER_ERROR,
          AppConstants.RESPONSE_CODES.SERVER_ERROR, err))
      } else {

        models.instance.Messages.findOne({
          recipient_refid: message.recipient_refid,
          recipient_reftype: message.recipient_reftype, status: message.status, created_on: currentTimestamp
        }, { raw: true, allow_filtering: true },  (err, messageInfo) => {
          if (err) {
            let responseUtil = new ResponseUtil(AppConstants.API_IDS.CREATE_NOTIFICATION);
            res.status(HttpStatus.INTERNAL_SERVER_ERROR);
            res.send(responseUtil.prepareErrorResponse(AppConstants.RESPONSE_CODES.SERVER_ERROR,
              AppConstants.RESPONSE_CODES.SERVER_ERROR, err))
          }
          let kafkaProducerAdapter = KafkaProducerAdapter.connect()
          KafkaProducerAdapter.pushMessageToBroker(kafkaProducerAdapter, {
            topic: messageInfo.message_type,
            message: {
              messageId: messageInfo.id
            }
          }, (err, status) => {
            if (err) {
              let responseUtil = new ResponseUtil(AppConstants.API_IDS.CREATE_NOTIFICATION);
              res.status(HttpStatus.INTERNAL_SERVER_ERROR);
              res.send(responseUtil.prepareErrorResponse(AppConstants.RESPONSE_CODES.SERVER_ERROR,
                AppConstants.RESPONSE_CODES.SERVER_ERROR, err))
            } else {
              let responseUtil = new ResponseUtil(AppConstants.API_IDS.CREATE_NOTIFICATION);
              res.status(HttpStatus.OK);
              res.send(responseUtil.prepareSuccessResponse(AppConstants.RESPONSE_CODES.OK, { id: messageInfo.id }))
            }

          })


        });
      }
    })
  }



}
