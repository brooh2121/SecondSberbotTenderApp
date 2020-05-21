package com.sberbot.Hugin.dao.impl;

import com.sberbot.Hugin.dao.HuginDao;
import com.sberbot.Hugin.dao.mapper.AuctionsMapper;
import com.sberbot.Hugin.model.AuctionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class HuginDaoImpl implements HuginDao {

    @Autowired
    @Qualifier("jdbcTemplatePostgreTend")
    JdbcTemplate jdbcTemplatePostgreTend;

    @Override
    public AuctionModel getMinimalTender() {
        AuctionModel auctionModelFromDb;
        String query = "select\n" +
                "*\n" +
                "from auctions a\n " +
                "where tender_status = 'Подача заявок'\n " +
                //"and date_insert >= current_date\n " +
                "order by\n" +
                "to_timestamp(publication_date,'dd.mm.yyyy hh24:mi:ss') limit 1";

        try {
            auctionModelFromDb = (AuctionModel) jdbcTemplatePostgreTend.queryForObject(query, new Object [] {} ,new AuctionsMapper());
        }catch (EmptyResultDataAccessException e) {
            return null;
        }

        return auctionModelFromDb;
    }

    @Override
    public int setTenderStatusIfFailure(String tenderNumber) {
        String query = "update public.auctions set tender_status = 'не подходит для подачи документов' where auction_number = ?";
        return jdbcTemplatePostgreTend.update(query,tenderNumber);
    }

    @Override
    public int setTenderStatusIfSuccess(String tenderNumber) {
        String query = "update public.auctions set tender_status = 'подходит для подачи документов' where auction_number = ?";
        return jdbcTemplatePostgreTend.update(query,tenderNumber);
    }

    public int docSendJourInsert(String tenderNumber, String docStep, boolean bolResult, String comment) {
        String query = "insert into send_doc_jur (\n" +
                "send_doc_id,\n" +
                "auction_number,\n" +
                "doc_step,\n" +
                "sucessful_step,\n" +
                "doc_send_comment\n" +
                ")\n" +
                "values(\n" +
                "(select nextval('send_doc_id_seq')),\n" +
                "'"+tenderNumber+"',\n" +
                "'"+docStep+"',\n" +
                ""+bolResult+",\n" +
                "'"+comment+"'\n" +
                ");";

        return jdbcTemplatePostgreTend.update(query);
    }

    @Override
    public int setBotStartTimestamp(String botName, LocalDateTime localDateTime) {

        Timestamp timestamp = Timestamp.valueOf(localDateTime);

        String query = "insert into public.bot_log_jour (\n" +
                "bot_log_id,\n" +
                "bot_name,\n" +
                "bot_beg_date\n" +
                ")\n" +
                "values (\n" +
                "(select nextval('bot_log_id_seq')),\n" +
                "'"+botName+"',\n" +
                "'"+timestamp+"'\n" +
                ");";

        return jdbcTemplatePostgreTend.update(query);
    }

    @Override
    public int setBotEndTimestamp(LocalDateTime localDateTime) {

        Timestamp timestamp = Timestamp.valueOf(localDateTime);

        String query = "with tt1 as (\n" +
                "select max(bot_log_id) from public.bot_log_jour\n" +
                ")\n" +
                "update public.bot_log_jour set bot_end_date = ? where bot_log_id = (select * from tt1)";

        return jdbcTemplatePostgreTend.update(query,timestamp);
    }

    @Override
    public void setTenderStatusOnBotStarting() {
        String query = "update public.auctions set tender_status = 'опоздали с подачей' where tender_status = 'Подача заявок'";
        jdbcTemplatePostgreTend.update(query);
    }

}
