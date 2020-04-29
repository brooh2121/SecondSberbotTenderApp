package com.sberbot.Hugin.dao.mapper;

import com.sberbot.Hugin.model.AuctionModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionsMapper implements RowMapper {

    @Override
    public AuctionModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuctionModel auctionModel = new AuctionModel();
        auctionModel.setAuctionNumber(rs.getString(1));
        auctionModel.setOrgName(rs.getString(2));
        auctionModel.setTenderName(rs.getString(3));
        auctionModel.setPublicDate(rs.getString(4));
        auctionModel.setSum(rs.getString(5));
        return auctionModel;
    }
}
