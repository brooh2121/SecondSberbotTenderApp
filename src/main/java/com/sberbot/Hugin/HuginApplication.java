package com.sberbot.Hugin;

import com.codeborne.selenide.WebDriverRunner;
import com.sberbot.Hugin.service.HuginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.codeborne.selenide.Selenide.switchTo;

@SpringBootApplication
public class HuginApplication  implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(HuginApplication.class.getSimpleName());

	String tenderNumber = "0372200054720000004";

	@Autowired
    HuginService huginService;

	public static void main(String[] args) {
		SpringApplication.run(HuginApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Пробуем залогинится");
		Boolean loginSuccesed = huginService.getLogin();
		if(loginSuccesed) {
			Boolean osagoTenderChecked = huginService.getCurrentTender(tenderNumber);
			if (osagoTenderChecked) {
				System.out.println("Это тендер по Осаго - можно начинать подавать документы");
			}else {
				System.out.println("Тендер не прошел одну из проверок");
			}
			WebDriverRunner.closeWindow();
			switchTo().window(0);
			System.out.println(WebDriverRunner.url());
		}else {
			System.out.println("Не удалось зайти под учетной записью с ЭЦП");
		}
		//huginService.getTenderUrlCheck();

		System.out.println("Закрываем страницу после получения заголовка");
		Thread.sleep(1000);
		huginService.closePage();
	}
}
