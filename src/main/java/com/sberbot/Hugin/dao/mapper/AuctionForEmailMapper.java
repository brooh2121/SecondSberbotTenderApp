package com.sberbot.Hugin.dao.mapper;

import com.sberbot.Hugin.model.AuctionModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AuctionForEmailMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuctionModel auctionModel = new AuctionModel();
        auctionModel.setAuctionNumber(rs.getString("tender_number"));
        auctionModel.setOrgName(rs.getString("org_name"));
        auctionModel.setTenderName(rs.getString("tender_name"));
        auctionModel.setPublicDate(rs.getString("publication_date"));
        auctionModel.setSum(rs.getString("tender_sum"));
        auctionModel.setTenderPlaceUrl(rs.getString("tender_place_url"));
        auctionModel.setTenderGovUrl(rs.getString("tender_gov_url"));
        return auctionModel;
    }
}
