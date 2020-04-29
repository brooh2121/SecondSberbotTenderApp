package com.sberbot.Hugin.dao;

import com.sberbot.Hugin.model.AuctionModel;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public interface HuginDao {
    AuctionModel getMinimalTender();

    int setTenderStatusIfFailure(String tenderNumber);

    int setTenderStatusIfSuccess(String tenderNumber);

    int docSendJourInsert(String tenderNumber, String docStep, boolean bolResult, String comment);

    int setBotStartTimestamp (String botName, LocalDateTime localDateTime);

    int setBotEndTimestamp(LocalDateTime localDateTime);

}
