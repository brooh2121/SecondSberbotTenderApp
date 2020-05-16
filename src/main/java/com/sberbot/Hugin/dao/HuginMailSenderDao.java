package com.sberbot.Hugin.dao;

import com.sberbot.Hugin.model.AuctionModel;

public interface HuginMailSenderDao {

    Boolean checkFilinDocSuccessStatus (String tenderNumber);

    AuctionModel getModelFromOracle (String tenderNumber);

    String getFilingApplicationDate(Long tenderNumberId);
}
