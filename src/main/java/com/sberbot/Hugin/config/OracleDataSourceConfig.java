package com.sberbot.Hugin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OracleDataSourceConfig {

    @Autowired
    Environment environment;

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    @Bean(name="oracleTend")
    public HikariDataSource getDataSourceOracle() {
        config.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name1"));
        config.setJdbcUrl(environment.getProperty("spring.datasource.url1"));
        config.setUsername(environment.getProperty("spring.datasource.username1"));
        config.setPassword(environment.getProperty("spring.datasource.password1"));
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("select 1 from dual");
        ds = new HikariDataSource(config);
        return ds;
    }

    @Bean(name = "jdbcTemplateOracleTend")
    public JdbcTemplate getJdbcTemplateOracleTend() {return new JdbcTemplate(getDataSourceOracle());}
}
