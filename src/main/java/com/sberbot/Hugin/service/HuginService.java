package com.sberbot.Hugin.service;

import com.codeborne.selenide.*;
import com.codeborne.selenide.ex.ElementNotFound;
import com.sberbot.Hugin.dao.HuginDao;
import com.sberbot.Hugin.dao.HuginOracleDao;
import com.sberbot.Hugin.model.AuctionModel;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.channels.SeekableByteChannel;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

@Service
public class HuginService {

    private static final Logger logger = LoggerFactory.getLogger(HuginService.class.getSimpleName());

    @Autowired
    HuginDao huginDao;

    @Autowired
    HuginOracleDao huginOracleDao;

    @Autowired
    Environment environment;

    public boolean getLogin() throws InterruptedException{

        File driverFile = new File (environment.getProperty("webdriver.chrome.path"));
        System.setProperty("webdriver.chrome.driver",driverFile.getAbsolutePath());
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-notifications");
        options.addExtensions(new File(environment.getProperty("criptopro.plugin.path")));
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        options.merge(capabilities);
        ChromeDriver driver = new ChromeDriver(options);
        WebDriverRunner.setWebDriver(driver);
        Configuration.reopenBrowserOnFail = true;

        logger.info("Переходим на страницу");
        open("https://www.sberbank-ast.ru/purchaseList.aspx");
        WebDriverRunner.getWebDriver().manage().window().maximize();
        try{
            SelenideElement loginButton = element(byXpath("//*[@id=\"ctl00_ctl00_loginctrl_anchSignOn\"]"));
            loginButton.click();
        }catch (ElementNotFound e) {
            logger.error(e.getMessage());
            logger.info("Закрываем страницу");
            closePage();
            logger.info("Не удалось перейти на страницу с регистрацией по кнопке, вызываем рекурсию метода для повторной попытки");
            getLogin();
        }

        element(byId("mainContent_DDL1")).waitUntil(Condition.visible,4000).selectOptionByValue("5EB4A43B643B922465BF95108F01BBA8F6C7C6E7");
        element(byId("btnEnter")).click();

        Thread.sleep(500);
        //minimalizeTenderTable();

        if (getTenderUrlCheck()) {
            return true;
        } else return false;


        //return true;
    }


    private boolean getTenderUrlCheck () {
        String url = WebDriverRunner.url();
        String user = element(byXpath("//*[@id=\"ctl00_ctl00_loginctrl_link\"]")).waitUntil(Condition.visible,5000).text();
        if(/*url.equals("https://www.sberbank-ast.ru/purchaseList.aspx")&*/user.equals("Мартьянова Надежда Васильевна ИНН: 7709031643 (головная организация)")) {
            logger.info("Мы успешно залогинились и находимся на странице поиска тендеров");
            return true;
        }else {
            logger.info("Ссылка " + url);
            logger.info("Пользователь " + user);
            return false;
        }

    }


    public AuctionModel getTenderFromDB () {
        AuctionModel auctionModelFromDb = huginDao.getMinimalTender();
        return auctionModelFromDb;
    }

    public Boolean getCurrentTender() {
        AuctionModel auctionModelFromDb = getTenderFromDB ();
        //if(auctionModelFromDb != null) {
            SelenideElement tableWithOneRow = seachOption(auctionModelFromDb.getAuctionNumber());
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
                if (checkTenderOptions(tableWithOneRow,auctionModelFromDb.getAuctionNumber())) {
                    logger.info("Тендер с номером " + auctionModelFromSite.getAuctionNumber() + " подходит для подачи документов");
                    huginDao.setTenderStatusIfSuccess(auctionModelFromSite.getAuctionNumber());
                    logger.info("Меняем статус тендера на значение: подходит для подачи документов");
                    return true;
                }else {
                    logger.info("Тендер с номером " + auctionModelFromSite.getAuctionNumber() + " не подходит для подачи документов");
                    huginDao.setTenderStatusIfFailure(auctionModelFromSite.getAuctionNumber());
                    logger.info("Меняем статус тендера на значение: не подходит для подачи документов");
                    WebDriverRunner.closeWindow();
                    switchTo().window(0);
                    return false;
                }
            }else {
                logger.info("Тендер из БД " + auctionModelFromDb.toString() + " не совпадает с тендером с сайта " + auctionModelFromSite);
                huginDao.setTenderStatusIfFailure(auctionModelFromSite.getAuctionNumber());
                logger.info("Меняем статус тендера на значение: не подходит для подачи документов по причине не совпадения данных");
                return false;
            }
        /*}else {
            logger.info("В базе данных нет тендеров, которые нужно проверить");
            return false;
        }*/

    }

        public void filinDoc(String tenderNumber, LocalDateTime botStartDateTime) {

        Long tenderNumberIdFromOracle = huginOracleDao.getTenderIdByNumber(tenderNumber);

        SelenideElement selenideElement = element(byId("resultTable"));
        selenideElement.shouldBe(Condition.visible);
        SelenideElement divonerow = selenideElement.find(byClassName("element-in-one-row"));
        ElementsCollection els = divonerow.findAll(byCssSelector("input"));
        els.get(0).click();
        logger.info("Осуществляем переход по ссылке " + WebDriverRunner.url());

        try {
            //скроллим до нажатия кнопки на выбор номера счета
            //executeJavaScript("window.scrollBy(0,400)", "");
            SelenideElement button = element(byXpath("//table[@id='bxAccount']/tbody/tr/td[2]/input[2]")).waitUntil(Condition.visible,1000);
            button.click();
            switchTo().frame("spravIframe");
            element(byXpath("//*[@id=\"XMLContainer\"]/table/tbody/tr[2]/td[1]/a/span")).waitUntil(Condition.visible,60000).click();
            switchTo().defaultContent();
            String inputSchetNumber = element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_phDocumentZone_nbtPurchaseRequest_bxAccount_account\"]")).getValue();

            if(StringUtils.hasText(inputSchetNumber)) {
                logger.info("Удалось выбрать номер счета");
                huginDao.docSendJourInsert(tenderNumber,"Выбрали номер счета",true,null);
                huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,1,botStartDateTime,LocalDateTime.now(),1,"Выбираем номер счета");
            }

            //согласие на поставку услуг
            //executeJavaScript("window.scrollBy(0,400)", "");
            element(byXpath("//*[@id=\"XMLContainer\"]/div/table[5]/tbody/tr[2]/td[2]/input[2]")).click();
            switchTo().frame("spravIframe");
            element(byXpath("//*[@id=\"ctl00_phDataZone_btnOK\"]")).waitUntil(Condition.visible,3000).click();
            switchTo().defaultContent();
            String deliveryConsention = element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_phDocumentZone_nbtPurchaseRequest_reqAgreementAnswer\"]")).getText();

            if(StringUtils.hasText(deliveryConsention)) {
                logger.info("Согласились на предоставление услуг");
                huginDao.docSendJourInsert(tenderNumber,"Согласились на предоставление услуг", true, null);
                huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,2,botStartDateTime,LocalDateTime.now(),1,"Соглашаемся на предоставление услуг");
            }

            //скроллим до кнопки формы согласия
            //executeJavaScript("window.scrollBy(0,400)", "");
            /*
            element(byXpath("//*[@id=\"ctl00$ctl00$phWorkZone$phDocumentZone$nbtPurchaseRequest$reqDocsPart1tblDoc\"]/tbody/tr/td[2]/input[1]")).click();
            switchTo().frame("spravIframe");
            element(byXpath("//*[@id=\"ctl00_phDataZone_FileStoreContainer\"]/div[1]/a")).click();
            switchTo().defaultContent();
            String formConsensionDoc = element(byXpath("//*[@id=\"txbFileName\"]")).getText();

            if (StringUtils.hasText(formConsensionDoc)) {
                logger.info("Приложили документ согласия");
                huginDao.docSendJourInsert(tenderNumber,"приложили документ на предоставление услуг", true, null);
                huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,3,botStartDateTime,LocalDateTime.now(),1,"Прикладываем документ согласия");
            }
            */



            //скроллим до кнопки подписать декларацию
            //executeJavaScript("window.scrollBy(0,1000)", "");
            element(byXpath("//*[@id=\"tblrequireddocs22\"]/tbody/tr[4]/td[2]/input[2]")).click();
            switchTo().frame("spravIframe");
            executeJavaScript("window.scrollBy(0,700)", "");
            element(byXpath("//*[@id=\"ctl00_phDataZone_btnOK\"]")).click();
            switchTo().defaultContent();
            String declarationConsent = element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_phDocumentZone_nbtPurchaseRequest_reqDeclarationRequirementsAnswer\"]")).getText();

            if(StringUtils.hasText(declarationConsent)) {
                logger.info("подписали декларацию");
                huginDao.docSendJourInsert(tenderNumber,"подписали декларацию", true, null);
                huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,4,botStartDateTime,LocalDateTime.now(),1,"Подписываем декларацию");
            }

            //скроллим чтобы приложить документы 2 часть
            //executeJavaScript("window.scrollBy(0,400)", "");
            element(byXpath("//*[@id=\"ctl00$ctl00$phWorkZone$phDocumentZone$nbtPurchaseRequest$FileAttach2tblDoc\"]/tbody/tr/td[2]/input[1]")).click();
            switchTo().frame("spravIframe");
            element(byXpath("//*[@id=\"ctl00_phDataZone_FileStoreContainer\"]/div[2]/a")).click();
            switchTo().defaultContent();
            String form2part = element(byXpath("/html/body/form/div[7]/div/div[1]/table/tbody/tr[1]/td/div/div/table[10]/tbody/tr/td[2]/div/div/table/tbody/tr/td[1]/a/span")).getText();

            if(StringUtils.hasText(form2part)) {
                logger.info("приложили документы, вторую часть");
                huginDao.docSendJourInsert(tenderNumber,"приложили вторую часть документов", true, null);
                huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,5,botStartDateTime,LocalDateTime.now(),1,"Прикладываем документы - вторую часть");
            }

            //Подачу самой заявки пока не делаем
            //element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_SignPanel_btnSignAllFilesAndDocument\"]")).click();
            if (
                    StringUtils.hasText(inputSchetNumber)
                            & StringUtils.hasText(deliveryConsention)
                            //& StringUtils.hasText(formConsensionDoc)
                            & StringUtils.hasText(declarationConsent)
                            & StringUtils.hasText(form2part)) {
                element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_SignPanel_btnSignAllFilesAndDocument\"]")).click();
                element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_SignPanel_btnSignAllFilesAndDocument\"]")).waitUntil(Condition.not(Condition.visible), 60000);

                String errorMessage;
                try {
                    errorMessage = element(byXpath("//*[@id=\"ctl00_ctl00_phWorkZone_errorMsg\"]")).text();
                    logger.info("Ошибка подачи:" + errorMessage);
                    //if(errorMessage.text().contains("Ваш документ зарегистрирован как отвергнутый.")) {
                        huginDao.docSendJourInsert(tenderNumber,"нажатие кнопки подписать и отправить", false,"подписываем и отправляем");
                        huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,6,botStartDateTime,LocalDateTime.now(),0,"нажимаем кнопку подписать и отправить");
                   //}
                }catch (ElementNotFound e) {
                    logger.info("Ошибка дубликата по тендеру с номером " + tenderNumber + " не найдена, подали документы первыми");
                    logger.error(e.getMessage());
                    huginDao.docSendJourInsert(tenderNumber,"нажатие кнопки подписать и отправить", true,"подписываем и отправляем");
                    huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,6,botStartDateTime,LocalDateTime.now(),1,"нажимаем кнопку подписать и отправить");
                }

                /*
                if(errorMessage.text().contains("Ваш документ зарегистрирован как отвергнутый.")) {
                    huginDao.docSendJourInsert(tenderNumber,"нажатие кнопки подписать и отправить", false,"подписываем и отправляем");
                    huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,6,botStartDateTime,LocalDateTime.now(),0,"нажимаем кнопку подписать и отправить");
                }else {
                    huginDao.docSendJourInsert(tenderNumber,"нажатие кнопки подписать и отправить", true,"подписываем и отправляем");
                    huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,6,botStartDateTime,LocalDateTime.now(),1,"нажимаем кнопку подписать и отправить");
                }*/

                //huginDao.docSendJourInsert(tenderNumber,"нажатие кнопки подписать и отправить", false,"пока что не отправляем");
                //huginOracleDao.tenderaRowsJourInsert(tenderNumberIdFromOracle,6,botStartDateTime,LocalDateTime.now(),"Пока что не нажимаем последнюю кнопку подписать и отправить");
            }
        }catch (Exception e) {
            logger.error(e.getMessage());
        }
        open("https://www.sberbank-ast.ru/purchaseList.aspx");

    }

    private boolean checkTenderOptions(SelenideElement selenideElement, String tenderNumber) {
        SelenideElement divonerow = selenideElement.find(byClassName("element-in-one-row"));
        ElementsCollection els = divonerow.findAll(byCssSelector("input"));
        els.get(1).click();
        switchTo().window(1);
        WebDriverRunner.getWebDriver().manage().timeouts().pageLoadTimeout(10,TimeUnit.SECONDS);
        //System.out.println(WebDriverRunner.url());
        if(!WebDriverRunner.url().equals("https://www.sberbank-ast.ru/purchaseList.aspx")) {
            logger.info("Переходим к просмотру данных по тендеру");
            SelenideElement el = element(byXpath("//*[@id=\"XMLContainer\"]/div[1]/table[1]/tbody/tr[2]/td[2]")).waitUntil(Condition.visible, 60000);
            if (String.valueOf(el).contains("44-ФЗ")) {
                logger.info("Тендер прошел проверку по значению поля равному 44-ФЗ, переходим к проверке ОКПД");
                String okpd = element(byCssSelector("span[content='leaf:code']")).text();
                if (okpd.contains("65")) {
                    logger.info("Тендер прошел проверку ОКПД в части первых двух символов, равных 65");
                    if (!okpd.contains("65.3.") || !okpd.contains("65.30") || !okpd.contains("65.12.49.000") || !okpd.contains("65.12.50.000") || !okpd.contains("65.12.29.000")) {
                        logger.info("Тендер не относится к 65.3. ,65.30, 65.12.49.000 (страхование имущества),65.12.50.000 (страхование общей ответственности),65.12.29.000 (КАСКО), следовательно подходит для подачи документов");
                        logger.info("Обновляем url тендера");
                        huginOracleDao.updateTenderPlaceUrl(WebDriverRunner.url(),tenderNumber);
                        String govUrlText = null;
                        String tenderEndPlanDate = null;
                        String tenderEndPlanTime = null;
                        try {
                            govUrlText  = element(byXpath("//*[@id=\"newinfolink\"]/td[2]/a/span")).text();
                            tenderEndPlanDate = element(byXpath("//*[@id=\"XMLContainer\"]/div[1]/table[14]/tbody/tr[2]/td[2]/span[1]")).text();
                            tenderEndPlanTime = element(byXpath("//*[@id=\"XMLContainer\"]/div[1]/table[14]/tbody/tr[2]/td[2]/span[2]")).text();
                        }catch (ElementNotFound e) {
                            logger.error(e.getMessage());
                        }
                        huginOracleDao.updateTenderGovUrl(govUrlText,tenderNumber);
                        huginOracleDao.updateTenderEndPlanDate(tenderEndPlanDate,tenderEndPlanTime,tenderNumber);
                        return true;
                    } else {
                        logger.info("так же не относится к осаго");
                        return false;
                    }
                } else {
                    logger.info("ОКДП не прошел проверку по значению 65 - значит тендер явно не по Осаго");
                    return false;
                }
            } else {
                logger.info("В ожидаемом поле не указано что тендер по 44-ФЗ");
                return false;
            }
        }else {
            logger.info("не удалось перейти к просмотру тендера");
            return false;
        }
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

    public void setBotStartTimestamp () {
        huginDao.setBotStartTimestamp("Hugin", LocalDateTime.now());
    }

    public void setBotEndTimestamp () {
        huginDao.setBotEndTimestamp(LocalDateTime.now());
    }
}
