package com.sberbot.Hugin;

import com.codeborne.selenide.WebDriverRunner;
import com.sberbot.Hugin.model.AuctionModel;
import com.sberbot.Hugin.service.HuginService;
import com.sberbot.Hugin.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.switchTo;

@SpringBootApplication
public class HuginApplication  implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(HuginApplication.class.getSimpleName());

	private int counter = 0;

	@Autowired
    HuginService huginService;

	@Autowired
	MailService mailService;

	@Autowired
	Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(HuginApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Портим статусы тендеров, по которым бот не успел отработать ранее, при первом старте бота");
		huginService.seTenderStatusOnBotStarting();
		logger.info("Пробуем залогинится");
		//mailService.sendHelloBotStartingEmail();
		Boolean loginSuccesed = huginService.getLogin();
		for(;;) {
				if(counter == Integer.parseInt(environment.getProperty("bot.browser.reloadingInterval"))) {
					logger.info("Перезапускаем браузер");
					counter = callBrowserReload(counter);
				}
				counter++;
				logger.info("Запускаем бота " + LocalDateTime.now());
				LocalDateTime botStartDateTime = LocalDateTime.now();
				huginService.setBotStartTimestamp();
				if(loginSuccesed) {
					AuctionModel auctionModelFromDb = huginService.getTenderFromDB();
					if(auctionModelFromDb!=null) {
						Boolean osagoTenderChecked = huginService.getCurrentTender();
						if (osagoTenderChecked) {
							System.out.println("Это тендер по Осаго - можно начинать подавать документы");
							logger.info("Это тендер по Осаго - можно начинать подавать документы");
							WebDriverRunner.closeWindow();
							switchTo().window(0);
							logger.info("Переходим на страницу с таблицей тендеров и готовимся подавать документы" + WebDriverRunner.url());
							huginService.filinDoc(auctionModelFromDb.getAuctionNumber(),botStartDateTime);
							logger.info("передаем номер тендера" + auctionModelFromDb.getAuctionNumber() + " для отправки рассылки");
							mailService.sendEmailNotification(auctionModelFromDb.getAuctionNumber());
						}else {
							System.out.println("Тендер не прошел одну из проверок");
							logger.info("Тендер не прошел одну из проверок");
						}
					}else {
						logger.info("В Базе данных не нашлось тендеров для проверки");
					}
				}else {
					System.out.println("Не удалось зайти под учетной записью с ЭЦП");
					logger.info("Не удалось зайти под учетной записью с ЭЦП");
				}
				logger.info("Бот закончил работу в " + LocalDateTime.now());
				huginService.setBotEndTimestamp();
				//huginService.getTenderUrlCheck();

				Thread.sleep(1000);
			//huginService.closePage();
		}
	}

	public int callBrowserReload(int counter) throws InterruptedException{
		closeWebDriver();
		huginService.getLogin();
		return counter = 0;
	}
}
