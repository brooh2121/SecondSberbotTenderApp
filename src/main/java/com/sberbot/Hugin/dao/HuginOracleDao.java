package com.sberbot.Hugin.dao;


import com.sberbot.Hugin.model.AuctionModel;

import java.time.LocalDateTime;

public interface HuginOracleDao {

    Long getTenderIdByNumber(String tenderNumber);

    int tenderaRowsJourInsert(Long tenderNumberId, int operationTypeId, LocalDateTime botStartDateTime, LocalDateTime operationDateTime,int success, String remark);

    void updateTenderPlaceUrl(String tenderPlaceUrl, String tenderNumber);

    void updateTenderGovUrl(String tenderGovUrl, String tenderNumber);

    void updateTenderEndPlanDate(String tenderEndPlanDate,String tenderEndPlanTime,String tenderNumber);

}
