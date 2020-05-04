package com.sberbot.Hugin.dao;


import java.time.LocalDateTime;

public interface HuginOracleDao {

    Long getTenderIdByNumber(String tenderNumber);

    int tenderaRowsJourInsert(Long tenderNumberId, int operationTypeId, LocalDateTime botStartDateTime, LocalDateTime operationDateTime,int success, String remark);

}
