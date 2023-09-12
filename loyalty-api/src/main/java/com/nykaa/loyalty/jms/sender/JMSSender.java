package com.nykaa.loyalty.jms.sender;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.nykaa.loyalty.enums.ErrorCodes;
import com.nykaa.loyalty.exception.LoyaltyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class JMSSender {
    private static final Logger logger = LogManager.getLogger(JMSSender.class);

    @Autowired(required = false)
    private AmazonSQSAsync sqs;

    @Autowired
    @Qualifier("loyaltyObjectMapper")
    private ObjectMapper objectMapper;

    @Autowired
    private Gson gson;

    public void sendMessageTOSQSQueue(String messageStr, String queueUrl) {
        if (messageStr == null) {
            throw new LoyaltyException(ErrorCodes.NULL_MESSAGE);
        }

        if (queueUrl == null) {
            throw new LoyaltyException(ErrorCodes.NULL_QUEUE);
        }

        if (sqs == null) {
            logger.info("SQS is not initialized, not sending message: " + messageStr);
            return;
        }

        logger.info("Sending message: " + messageStr + " to queue: " + queueUrl);

        final SendMessageRequest sendMessageRequest = new SendMessageRequest(queueUrl, messageStr);
        final SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
        final String sequenceNumber = sendMessageResult.getSequenceNumber();
        final String messageId = sendMessageResult.getMessageId();

        logger.info("Message sent successfully, message id: " + messageId + ", sequence number: " + sequenceNumber);
    }

    public String sendMessage(HashMap message, String queueUrl, boolean useGson) throws Exception {
        if (message == null) {
            throw new LoyaltyException(ErrorCodes.NULL_MESSAGE);
        }

        String messageStr;
        try {
            if (useGson) {
                messageStr = gson.toJson(message);
            } else {
                messageStr = objectMapper.writeValueAsString(message);
            }
            sendMessageTOSQSQueue(messageStr, queueUrl);
        } catch (Exception e) {
            logger.error("Exception occurred while sending message to queue: " + queueUrl + ", exception: ", e);
            throw e;
        }
        return messageStr;
    }
}
