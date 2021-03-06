package com.sberbot.Hugin.dao.impl;

import com.sberbot.Hugin.dao.HuginDao;
import com.sberbot.Hugin.dao.HuginOracleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Repository
public class HiginOracleDaoImpl implements HuginOracleDao {

    @Autowired
    @Qualifier("jdbcTemplateOracleTend")
    JdbcTemplate jdbcTemplateOracleTend;



    @Override
    public int tenderaRowsJourInsert(Long tenderNumberId, int operationTypeId, LocalDateTime botStartDateTime, LocalDateTime operationDateTime,int success,String remark) {

        Timestamp botStartTimeStamp = Timestamp.valueOf(botStartDateTime);
        Timestamp operationTimeStamp = Timestamp.valueOf(operationDateTime);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");


        String query = "INSERT INTO TENDERA_ROWS VALUES (\n" +
                "tend.SQ_TENDERA_ROWS_PK.NEXTVAL,\n" +
                ""+tenderNumberId+",\n" +
                ""+operationTypeId+",\n" +
                "'"+format.format(botStartTimeStamp)+"',\n" +
                "'"+format.format(operationTimeStamp)+"',\n" +
                ""+success+",\n" +
                "SYSDATE,\n" +
                "'"+remark+"',\n" +
                "TO_TIMESTAMP('01.01.9000 01:00:00','dd.mm.yyyy hh24:mi:ss') + (to_timestamp('"+format.format(operationTimeStamp)+"') - to_timestamp('"+format.format(botStartTimeStamp)+"'))\n" +
                ")";
        return jdbcTemplateOracleTend.update(query);
    }

    @Override
    public Long getTenderIdByNumber(String tenderNumber) {
       String query = "SELECT id FROM TENDERA  WHERE TENDER_NUMBER =  ?";
       return jdbcTemplateOracleTend.queryForObject(query,new Object [] {tenderNumber},Long.class);
    }

    @Override
    public void updateTenderPlaceUrl(String tenderPlaceUrl,String tenderNumber) {
        String query = "update tend.tendera set tender_place_url = ? where tender_number = ?";
        jdbcTemplateOracleTend.update(query,tenderPlaceUrl,tenderNumber);
    }

    @Override
    public void updateTenderGovUrl(String tenderGovUrl, String tenderNumber) {
        String query = "update tend.tendera set tender_gov_url = ? where tender_number = ?";
        jdbcTemplateOracleTend.update(query,tenderGovUrl,tenderNumber);
    }

    @Override
    public void updateTenderEndPlanDate(String tenderEndPlanDate, String tenderEndPlanTime, String tenderNumber) {
        String tenderEndPlanDateTime = tenderEndPlanDate + " " + tenderEndPlanTime;
        String query = "update tend.tendera set tender_end_date_plan = to_date('"+tenderEndPlanDateTime+"','dd.mm.yyyy hh24:mi') where tender_number = ?";
        jdbcTemplateOracleTend.update(query,tenderNumber);
    }
}
