package com.sberbot.Hugin.service;

import com.sberbot.Hugin.dao.HuginMailSenderDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;


@Service
public class MailService {

    @Autowired
    Environment environment;

    @Autowired
    HuginMailSenderDao huginMailSenderDao;

    @Autowired
    @Qualifier("mainMailSender")
    JavaMailSender javaMailSender;

    private static final Logger logger = LoggerFactory.getLogger(MailService.class.getSimpleName());

    private boolean checkFilinDocSuccessStatus(String tenderNumber) {
        return huginMailSenderDao.checkFilinDocSuccessStatus(tenderNumber);
    }


    public void sendEmailNotification (String tenderNumber) {
        String emailText = "Добрый день, была успешно подана заявка на тендер с номером " + tenderNumber + ".";
        if(checkFilinDocSuccessStatus(tenderNumber)) {
            try {
                logger.info("Пробуем отправить email после успешно поданной заявки");
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
                helper.setSubject("Новый тендер");
                helper.setFrom(environment.getProperty("spring.mail.username"));
                helper.setTo(new String [] {"dimich14@gmail.com","algor@makc.ru","achistov@makc.ru","gagavrilova@makc.ru","past@makc.ru"});
                helper.setText(emailText);
            }catch (Exception e) {
                logger.error(e.getMessage());
            }
        }else {
            logger.info("Не отправляем письмо, так как не первые подали заявку на тендер с номером " + tenderNumber);
        }

    }
}
