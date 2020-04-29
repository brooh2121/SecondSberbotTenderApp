package com.sberbot.Hugin.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PostgreDataSourceConfig {

    @Autowired
    Environment environment;

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    @Bean(name = "postgreTend")
    public HikariDataSource getDataSourceForPostgre() {
        config.setDriverClassName(environment.getProperty("spring.pg.datasource.driver-class-name"));
        config.setJdbcUrl(environment.getProperty("spring.datasource.url"));
        config.setUsername(environment.getProperty("spring.datasource.username"));
        config.setPassword(environment.getProperty("spring.datasource.password"));
        config.setMaximumPoolSize(10);
        config.setConnectionTestQuery("select 1");
        ds = new HikariDataSource(config);
        return ds;
    }

    @Bean(name = "jdbcTemplatePostgreTend")
    public JdbcTemplate getJdbcTemplatePostgreTend(){return new JdbcTemplate(getDataSourceForPostgre());}
}
