package com.sberbot.Hugin;

import com.codeborne.selenide.*;
import com.sberbot.Hugin.model.AuctionModel;
import com.sberbot.Hugin.service.HuginService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

public class currentTenderTest {

    @BeforeAll
    static void setBrowserDriver() {
        File driverFile = new File("C:\\IEdriver\\IEDriverServer.exe");
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
    }

    @AfterAll
    static void closeDriver() {
        WebDriverRunner.closeWebDriver();
    }

    @Test
    void TestCurrentTender () {
        String tenderNumber = "0725100000220000012";
        AuctionModel auctionModelFromDb = new AuctionModel();
        auctionModelFromDb.setAuctionNumber("0725100000220000012");
        auctionModelFromDb.setOrgName("ФЕДЕРАЛЬНОЕ БЮДЖЕТНОЕ УЧРЕЖДЕНИЕ ЦЕНТР РЕАБИЛИТАЦИИ ФОНДА СОЦИАЛЬНОГО СТРАХОВАНИЯ РОССИЙСКОЙ ФЕДЕРАЦИИ \"ТИНАКИ\"");
        auctionModelFromDb.setTenderName("Поставка плёнки и клеёнки");
        auctionModelFromDb.setPublicDate("24.04.2020 12:48");
        auctionModelFromDb.setSum("149 723.76");
        auctionModelFromDb.setTenderStatus("Подача заявок");
        open("https://www.sberbank-ast.ru/purchaseList.aspx");
        element(byId("searchInput")).setValue(tenderNumber).pressEnter();
        SelenideElement selenideElement = element(byId("resultTable"));
        selenideElement.shouldBe(Condition.visible);

        String tenderNumberFromSite = selenideElement.find(byClassName("es-el-code-term")).text();
        String orgName = selenideElement.find(byClassName("es-el-org-name")).text();
        String tenderName = selenideElement.find(byClassName("es-el-name")).text();
        String publicDate = selenideElement.find(byCssSelector("span[content='leaf:PublicDate']")).text();
        String tenderSum = selenideElement.find(byClassName("es-el-amount")).text();
        String tenderStatus = selenideElement.find(byCssSelector("div.es-el-state-name.PurchStateName")).text();

        AuctionModel auctionModelFromSite = new AuctionModel ();
        auctionModelFromSite.setAuctionNumber(tenderNumberFromSite);
        auctionModelFromSite.setOrgName(orgName);
        auctionModelFromSite.setTenderName(tenderName);
        auctionModelFromSite.setPublicDate(publicDate);
        auctionModelFromSite.setSum(tenderSum);
        auctionModelFromSite.setTenderStatus(tenderStatus);

        Assertions.assertTrue(tenderNumberFromSite.equals(auctionModelFromDb.getAuctionNumber()));
        Assertions.assertTrue(orgName.equals(auctionModelFromDb.getOrgName()));
        Assertions.assertTrue(tenderName.equals(auctionModelFromDb.getTenderName()));
        Assertions.assertTrue(publicDate.equals(auctionModelFromDb.getPublicDate()));
        Assertions.assertTrue(tenderSum.equals(auctionModelFromDb.getSum()));
        Assertions.assertTrue(tenderStatus.equals(auctionModelFromDb.getTenderStatus()));
        Assertions.assertEquals(auctionModelFromDb,auctionModelFromSite);
    }

    @Test
    void elemFromProsmotr() throws InterruptedException {
        open("https://www.sberbank-ast.ru/purchaseList.aspx");
        String tenderNumber = "0725100000220000012";
        element(byId("searchInput")).setValue(tenderNumber).pressEnter();
        SelenideElement selenideElement = element(byId("resultTable"));
        selenideElement.shouldBe(Condition.visible);
        SelenideElement divonerow = selenideElement.find(byClassName("element-in-one-row"));
        ElementsCollection els = divonerow.findAll(byCssSelector("input"));
        els.get(1).click();
        switchTo().window(1);
        System.out.println(!WebDriverRunner.url().equals("https://www.sberbank-ast.ru/purchaseList.aspx"));
        SelenideElement el = element(byXpath("//*/tbody/tr[2]/td[2]"));
        System.out.println(String.valueOf(el).contains("44-ФЗ"));
        String okpd = element(byCssSelector("span[content='leaf:code']")).text();
        System.out.println(okpd);
        System.out.println(okpd.toCharArray().length);
        if(okpd.contains("65")) {
            if (!okpd.contains("65.3.")||!okpd.contains("65.30")) {
                System.out.println("Это осаго");
            }
        }else {
            System.out.println("это не осаго");
        }
        WebDriverRunner.closeWindow();
    }


    @Test
    void elemFromProsmotrOsago() {
        open("https://www.sberbank-ast.ru/purchaseList.aspx");
        String tenderNumber = "0372200054720000004";
        element(byId("searchInput")).setValue(tenderNumber).pressEnter();
        SelenideElement selenideElement = element(byId("resultTable"));
        selenideElement.shouldBe(Condition.visible);
        SelenideElement divonerow = selenideElement.find(byClassName("element-in-one-row"));
        ElementsCollection els = divonerow.findAll(byCssSelector("input"));
        els.get(1).click();
        switchTo().window(1);
        System.out.println(!WebDriverRunner.url().equals("https://www.sberbank-ast.ru/purchaseList.aspx"));
        SelenideElement el = element(byXpath("//*/tbody/tr[2]/td[2]"));
        System.out.println(String.valueOf(el).contains("44-ФЗ"));
        String okpd = element(byCssSelector("span[content='leaf:code']")).text();
        System.out.println(okpd);
        System.out.println(okpd.toCharArray().length);
        if(okpd.contains("65")) {
            if (!okpd.contains("65.3.")||!okpd.contains("65.30")) {
                System.out.println("Это осаго");
            }
        }else {
            System.out.println("это не осаго");
        }
        WebDriverRunner.closeWindow();
        switchTo().window(0);
        System.out.println(WebDriverRunner.url());
    }

    @Test
    void fillinDoc() throws InterruptedException{
        open("https://www.sberbank-ast.ru/purchaseList.aspx");
        String tenderNumber = "0338200006520000003";
        element(byId("searchInput")).setValue(tenderNumber).pressEnter();
        SelenideElement selenideElement = element(byId("resultTable"));
        selenideElement.shouldBe(Condition.visible);
        SelenideElement divonerow = selenideElement.find(byClassName("element-in-one-row"));
        ElementsCollection els = divonerow.findAll(byCssSelector("input"));
        System.out.println(els.get(0));
        //els.get(0).click();
        Thread.sleep(1000);
    }

    @Test
    void checkGovUrlText() throws InterruptedException {
        open("https://www.sberbank-ast.ru/purchaseview.aspx?id=7568034");
        String govUrlText  = element(byXpath("//*[@id=\"newinfolink\"]/td[2]/a/span")).text();
        System.out.println(govUrlText);
        //*[@id="newinfolink"]/td[2]/a/span
    }

    @Test
    void checkTenderEndPlanDate() {
        open("https://www.sberbank-ast.ru/purchaseview.aspx?id=7568943");
        String tenderEndPlanDate = element(byXpath("//*[@id=\"XMLContainer\"]/div[1]/table[14]/tbody/tr[2]/td[2]/span[1]")).text();
        String tenderEndPlanTime = element(byXpath("//*[@id=\"XMLContainer\"]/div[1]/table[14]/tbody/tr[2]/td[2]/span[2]")).text();
        System.out.println(tenderEndPlanDate + " " + tenderEndPlanTime);
    }
}
