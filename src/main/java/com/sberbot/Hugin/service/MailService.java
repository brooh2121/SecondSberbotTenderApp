package com.sberbot.Hugin.service;

import com.sberbot.Hugin.dao.HuginMailSenderDao;

import com.sberbot.Hugin.dao.HuginOracleDao;
import com.sberbot.Hugin.model.AuctionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;


@Service
public class MailService {

    @Autowired
    Environment environment;

    @Autowired
    HuginMailSenderDao huginMailSenderDao;

    @Autowired
    @Qualifier("mainMailSender")
    JavaMailSender javaMailSender;

    @Autowired
    HuginOracleDao oracleDao;

    private static final Logger logger = LoggerFactory.getLogger(MailService.class.getSimpleName());

    private boolean checkFilinDocSuccessStatus(String tenderNumber) {
        return huginMailSenderDao.checkFilinDocSuccessStatus(tenderNumber);
    }


    public void sendEmailNotification (String tenderNumber) {
        AuctionModel auctionModelFromOracle = null;
        String tenderFilingApplicationDate = null;
        try {
            auctionModelFromOracle = huginMailSenderDao.getModelFromOracle(tenderNumber);
            Long tenderNumberId = oracleDao.getTenderIdByNumber(tenderNumber);
            tenderFilingApplicationDate = huginMailSenderDao.getFilingApplicationDate(tenderNumberId);
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
        String defaultError = "Ошибка - Заявка на участие в электронном аукционе возвращена на основании п.2 ч.11 ст. 66 Федерального закона от 05.04.2013 г. № 44-ФЗ: заявка на участие в аукционе уже подана! Для повторной подачи необходимо отозвать ранее поданную заявку. \n";
        String textEmailOrganizationName = "Организатор торгов - наименование организации: " + auctionModelFromOracle.getOrgName() + "\n";
        String servicePlace = "Место оказания услуги: \n";
        String textEmailTenderName = "Наименование объекта закупки: " + auctionModelFromOracle.getTenderName() + "\n";
        String textEmailTenderNumber = "Номер извещения: " + auctionModelFromOracle.getAuctionNumber() + "\n";
        String textEmailTenderSum = "НМЦК: " + auctionModelFromOracle.getSum() + "\n";
        String textEmailTenderPlaceUrl = "Ссылка на электронный аукцион на площадке: " + auctionModelFromOracle.getTenderPlaceUrl() + "\n";
        String textEmailTenderGovUrl = "Ссылка на извещение, опубликованное в ЕИС: " + auctionModelFromOracle.getTenderGovUrl() + "\n";
        String textEmailTenderPublicationDate = "Дата публикации: " + auctionModelFromOracle.getPublicDate() + "\n";
        String textEmailTenderDateFilingDoc = "Дата подачи: " + tenderFilingApplicationDate;
        String textEmailResult = textEmailOrganizationName + servicePlace + textEmailTenderName + textEmailTenderNumber + textEmailTenderSum + textEmailTenderPlaceUrl + textEmailTenderGovUrl + textEmailTenderPublicationDate + textEmailTenderDateFilingDoc;
        String textEmailResultFail = defaultError + textEmailOrganizationName + servicePlace + textEmailTenderName + textEmailTenderNumber + textEmailTenderSum + textEmailTenderPlaceUrl + textEmailTenderGovUrl + textEmailTenderPublicationDate + textEmailTenderDateFilingDoc;
        if(checkFilinDocSuccessStatus(tenderNumber)) {
            logger.info("Пробуем отправить email после успешно поданной заявки на тендер с номером" + tenderNumber);
            emailSend(textEmailResult);
        }else {
            logger.info("Отправляем другое письмо,если не первые подали заявку на тендер с номером " + tenderNumber);
            emailSend(textEmailResultFail);
        }
    }

    private void emailSend(String emailText) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setSubject("Информация о подаче документов по электронному аукциону на площадке Sber");
            helper.setFrom(environment.getProperty("spring.mail.username"));
            helper.setTo(new String [] {"dimich14@gmail.com","algor@makc.ru","achistov@makc.ru","gagavrilova@makc.ru","past@makc.ru"});
            helper.setText(emailText);
            javaMailSender.send(mimeMessage);
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void sendHelloBotStartingEmail() {
        String emailText = "Бот Hugin запущен в" + LocalDateTime.now();
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setSubject("Старт бота");
            helper.setFrom(environment.getProperty("spring.mail.username"));
            helper.setTo(new String [] {"dimich14@gmail.com","koks-gops@ya.ru"});
            helper.setText(emailText);
            javaMailSender.send(mimeMessage);
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
