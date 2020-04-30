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
    public int tenderaRowsJourInsert(Long tenderNumberId, int operationTypeId, LocalDateTime botStartDateTime, LocalDateTime operationDateTime,String remark) {

        Timestamp botStartTimeStamp = Timestamp.valueOf(botStartDateTime);
        Timestamp operationTimeStamp = Timestamp.valueOf(operationDateTime);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.YYYY HH:mm:ss");


        String query = "INSERT INTO TENDERA_ROWS VALUES (\n" +
                "tend.SQ_TENDERA_ROWS_PK.NEXTVAL,\n" +
                ""+tenderNumberId+",\n" +
                ""+operationTypeId+",\n" +
                "'"+format.format(botStartTimeStamp)+"',\n" +
                "'"+format.format(operationTimeStamp)+"',\n" +
                "1,\n" +
                "SYSDATE,\n" +
                "'"+remark+"'\n" +
                ")";
        return jdbcTemplateOracleTend.update(query);
    }

    @Override
    public Long getTenderIdByNumber(String tenderNumber) {
       String query = "SELECT id FROM TENDERA  WHERE TENDER_NUMBER =  ?";
       return jdbcTemplateOracleTend.queryForObject(query,new Object [] {tenderNumber},Long.class);
    }
}
