package com.sberbot.Hugin.service;

import com.codeborne.selenide.*;
import com.sberbot.Hugin.model.AuctionModel;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.channels.SeekableByteChannel;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

@Service
public class HuginService {

    private static final Logger logger = LoggerFactory.getLogger(HuginService.class.getSimpleName());

    @Autowired
    Environment environment;

    public boolean getLogin() throws InterruptedException {
        File driverFile = new File(environment.getProperty("webdriver.path"));
        System.setProperty("webdriver.ie.driver",driverFile.getAbsolutePath());
        Configuration.browserCapabilities.setCapability("ie.forceCreateProcessApi",true);
        Configuration.browserCapabilities.setCapability("nativeEvents", true);
        Configuration.browserCapabilities.setCapability("unexpectedAlertBehaviour", "accept");
        Configuration.browserCapabilities.setCapability("ignoreProtectedModeSettings", true);
        Configuration.browserCapabilities.setCapability("disable-popup-blocking", true);
        Configuration.browserCapabilities.setCapability("enablePersistentHover", true);
        Configuration.browserCapabilities.setCapability("ignoreZoomSetting", true);
        Configuration.browserCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
        Configuration.browserCapabilities.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
        Configuration.browser="Internet Explorer";
        //Configuration.browserCapabilities.setCapability("acceptSslCerts",true);
        //Configuration.browserCapabilities.setCapability("acceptInsecureCerts",true);
        logger.info("Переходим на страницу");
        open("https://www.sberbank-ast.ru/purchaseList.aspx");
        /*
        element(byId("ctl00_ctl00_loginctrl_anchSignOn")).click();
        confirm();
        element(byId("mainContent_DDL1")).selectOptionByValue("5EB4A43B643B922465BF95108F01BBA8F6C7C6E7");
        element(byId("btnEnter")).click();
        minimalizeTenderTable();
        getTenderUrlCheck();
        */
        return true;
    }

    /*
    private boolean getTenderUrlCheck () {
        String url = WebDriverRunner.url();
        String user = element(byCssSelector("#ctl00_ctl00_loginctrl_link")).text();
        if(url.equals("https://www.sberbank-ast.ru/purchaseList.aspx")&user.equals("Мартьянова Надежда Васильевна ИНН: 7709031643 (головная организация)")) {
            logger.info("Мы успешно залогинились и находимся на странице поиска тендеров");
            return true;
        }else {
            logger.info("Ссылка " + url);
            logger.info("Пользователь " + user);
            return false;
        }

    }

     */

    public Boolean getCurrentTender(String tenderNumber) {
        //String tenderNumber = "0725100000220000012";
        AuctionModel auctionModelFromDb = new AuctionModel();
        auctionModelFromDb.setAuctionNumber("0725100000220000012");
        auctionModelFromDb.setOrgName("ФЕДЕРАЛЬНОЕ БЮДЖЕТНОЕ УЧРЕЖДЕНИЕ ЦЕНТР РЕАБИЛИТАЦИИ ФОНДА СОЦИАЛЬНОГО СТРАХОВАНИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ \"ТИНАКИ\"");
        auctionModelFromDb.setTenderName("Поставка плёнки и клеёнки");
        auctionModelFromDb.setPublicDate("24.04.2020 12:48");
        auctionModelFromDb.setSum("149 723.76");

        SelenideElement tableWithOneRow = seachOption(tenderNumber);
        String tenderNumberFromSite = tableWithOneRow.find(byClassName("es-el-code-term")).text();
        String orgName = tableWithOneRow.find(byClassName("es-el-org-name")).text();
        String tenderName = tableWithOneRow.find(byClassName("es-el-name")).text();
        String publicDate = tableWithOneRow.find(byCssSelector("span[content='leaf:PublicDate']")).text();
        String tenderSum = tableWithOneRow.find(byClassName("es-el-amount")).text();
        String tenderStatus = tableWithOneRow.find(byCssSelector("div.es-el-state-name.PurchStateName")).text();

        AuctionModel auctionModelFromSite = new AuctionModel ();
        auctionModelFromSite.setAuctionNumber(tenderNumberFromSite);
        auctionModelFromSite.setOrgName(orgName);
        auctionModelFromSite.setTenderName(tenderName);
        auctionModelFromSite.setPublicDate(publicDate);
        auctionModelFromSite.setSum(tenderSum);
        auctionModelFromSite.setTenderStatus(tenderStatus);

        Boolean tenderMatch = auctionModelFromDb.equals(auctionModelFromSite);
        if (tenderMatch) {
            if (checkTenderOptions(tableWithOneRow)) {
                return true;
            }else return false;
        }else {
         logger.info("Тендер из БД " + auctionModelFromDb.toString() + " не совпадает с тендером с сайта " + auctionModelFromSite);
         return false;
        }
    }

    private boolean checkTenderOptions(SelenideElement selenideElement) {
        SelenideElement divonerow = selenideElement.find(byClassName("element-in-one-row"));
        ElementsCollection els = divonerow.findAll(byCssSelector("input"));
        els.get(1).click();
        switchTo().window(1);
        if(!WebDriverRunner.url().equals("https://www.sberbank-ast.ru/purchaseList.aspx")) {
            SelenideElement el = element(byXpath("//*/tbody/tr[2]/td[2]"));
            if (String.valueOf(el).contains("44-ФЗ")) {
                String okpd = element(byCssSelector("span[content='leaf:code']")).text();
                if(okpd.contains("65")) {
                    if (!okpd.contains("65.3.")||!okpd.contains("65.30")) {
                        System.out.println("Это осаго");
                        return true;
                    }else {
                        System.out.println("так же не относится к осаго");
                        return false;
                    }
                }else {
                    System.out.println("это не осаго");
                    return false;
                }
            }else return false;
        }else return false;
    }

    private void minimalizeTenderTable() {
        executeJavaScript("select = document.getElementById('headerPagerSelect');\n" +
                "var opt = document.createElement('option');\n" +
                "opt.value = 1;\n" +
                "opt.innerHTML = 1;\n" +
                "select.appendChild(opt);");
        element(byId("headerPagerSelect")).selectOptionByValue("1");
    }

    private SelenideElement seachOption(String tenderNumber) {
        element(byId("searchInput")).setValue(tenderNumber).pressEnter();
        SelenideElement selenideElement = element(byId("resultTable"));
        selenideElement.shouldBe(Condition.visible);
        return selenideElement;
    }

    public void closePage() {
        closeWebDriver();
    }
}
