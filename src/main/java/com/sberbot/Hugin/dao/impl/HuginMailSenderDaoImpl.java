package com.sberbot.Hugin.dao.impl;

import com.sberbot.Hugin.dao.HuginMailSenderDao;
import com.sberbot.Hugin.model.AuctionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HuginMailSenderDaoImpl implements HuginMailSenderDao {

    @Autowired
    @Qualifier("jdbcTemplatePostgreTend")
    JdbcTemplate jdbcTemplatePostgreTend;

    @Autowired
    @Qualifier("jdbcTemplateOracleTend")
    JdbcTemplate jdbcTemplateOracleTend;

    @Override
    public Boolean checkFilinDocSuccessStatus(String tenderNumber) {
        String query = "select sucessful_step from send_doc_jur where auction_number = ? and doc_step = 'нажатие кнопки подписать и отправить'";
        return jdbcTemplatePostgreTend.queryForObject(query,new Object[] {tenderNumber},Boolean.class);
    }

    @Override
    public AuctionModel getModelFromOracle(String tenderNumber) {
        String query = "select org_name,tender_name,tender_number,tender_sum,tender_place_url,tender_gov_url,publication_date from tend.tendera where tender_number = ?";
        return jdbcTemplateOracleTend.queryForObject(query,new Object[] {tenderNumber},AuctionModel.class);
    }

    @Override
    public String getFilingApplicationDate(Long tenderNumberId) {
        String query = "SELECT OPER_END_DATE FROM TENDERA_ROWS WHERE tender_ID = ? AND operation_type_id = 6";
        return jdbcTemplateOracleTend.queryForObject(query,new Object [] {tenderNumberId},String.class);
    }

}
