package com.quietchatter.batch.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.talk")
    fun talkDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    fun talkJdbcTemplate(talkDataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(talkDataSource)
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.book")
    fun bookDataSource(): DataSource {
        return DataSourceBuilder.create().build()
    }

    @Bean
    fun bookJdbcTemplate(bookDataSource: DataSource): JdbcTemplate {
        return JdbcTemplate(bookDataSource)
    }
}
