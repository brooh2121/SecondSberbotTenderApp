package com.sberbot.Hugin.dao.impl;

import com.sberbot.Hugin.dao.HuginMailSenderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HuginMailSenderDaoImpl implements HuginMailSenderDao {

    @Autowired
    @Qualifier("jdbcTemplatePostgreTend")
    JdbcTemplate jdbcTemplatePostgreTend;

    @Override
    public Boolean checkFilinDocSuccessStatus(String tenderNumber) {
        String query = "select sucessful_step from send_doc_jur where auction_number = ? and doc_step = 'нажатие кнопки подписать и отправить'";
        return jdbcTemplatePostgreTend.queryForObject(query,new Object[] {tenderNumber},Boolean.class);
    }
}
