package com.nykaa.loyalty.util;

import com.nykaa.notification_base.constant.enums.MimeType;
import com.nykaa.notification_base.dto.AttachmentDto;
import com.nykaa.notification_base.dto.RecipientTemplateParamDto;
import com.nykaa.notification_client.service.MailClient;
import com.nykaa.loyalty.enums.EmailParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("loyaltyEmailUtil")
@Slf4j
public class EmailUtil {

    @Autowired
    private MailClient mailClient;

    @Value("${loyalty.notification.url}")
    private String notificationUrl;

    public void sendMailToAdmin(String email, String fileName, String statusStr, EmailParams emailParams, String template) {
        try {
            mailClient.setWebServiceBaseURL(notificationUrl);
            List<RecipientTemplateParamDto> recipientsTemplateParams = new ArrayList<>();
            List<String> recipientsMailIds = Arrays.asList(email.split(Constants.Symbols.COMMA));
            RecipientTemplateParamDto recipientTemplateParamDto = new RecipientTemplateParamDto();
            recipientTemplateParamDto.setRecipient(recipientsMailIds.get(0));
            recipientTemplateParamDto.setCcAddress(recipientsMailIds.subList(1, recipientsMailIds.size()));
            recipientsTemplateParams.add(recipientTemplateParamDto);

            List<AttachmentDto> attachmentDtoList = new ArrayList<>();
            AttachmentDto attachmentDto = new AttachmentDto();
            attachmentDto.setName(fileName);
            attachmentDto.setContent(statusStr);
            attachmentDto.setMimeType(MimeType.csv);
            attachmentDtoList.add(attachmentDto);
            mailClient.sendMailWithAttachment(template, recipientsTemplateParams,
                    emailParams.getName(), false, null, attachmentDtoList, null);
            log.info("Sending mail to emailIds: {} with attachement: {}", CommonUtils.maskEmailList(recipientsMailIds),
                    String.format("%s", attachmentDtoList));
        } catch (Exception e) {
            log.error("Exception occured while sending email: {}", e.getMessage());
        }
    }
}