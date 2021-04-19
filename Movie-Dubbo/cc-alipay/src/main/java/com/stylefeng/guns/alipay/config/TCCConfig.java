package com.stylefeng.guns.alipay.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Sets;
import lombok.Data;
import org.apache.dubbo.remoting.TimeoutException;
import org.mengyun.tcctransaction.spring.recover.DefaultRecoverConfig;
import org.mengyun.tcctransaction.spring.repository.SpringJdbcTransactionRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * TCC-transaction 配置
 * 参考使用指南 https://github.com/changmingxie/tcc-transaction/wiki
 *  1. 下载tcc-transaction项目打包
 *  2. 引入依赖
 *  3. 引入jar包中的配置文件 tcc-transaction.xml, classpath:tcc-transaction-dubbo.xml
 *  4. 配置 事务数据源、事务存储器、事务恢复处理JOB
 *  5. 创建TCC用的数据库和表 TCC.TCC_TRANSACTION
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "tcc")
@ImportResource(locations = {"classpath:tcc-transaction.xml", "classpath:tcc-transaction-dubbo.xml"})
public class TCCConfig {

    private String driverClassName;
    private String url;
    private String username;
    private String password;

    // TCC事务存储器使用的数据源
    @Bean("tccDataSource")
    public DruidDataSource tccDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    // TCC事务存储器
    @Bean("transactionRepository")
    public SpringJdbcTransactionRepository tccRepository() {
        SpringJdbcTransactionRepository repository = new SpringJdbcTransactionRepository();
        repository.setDataSource(tccDataSource());
        // repository.setTbSuffix("_ALIPAY"); //设置事务表后缀，即 TCC.TCC_TRANSACTION_ALIPAY
        return repository;
    }

    // TCC事务恢复处理JOB
    @Bean
    public DefaultRecoverConfig recoverConfig() {
        DefaultRecoverConfig recoverConfig = new DefaultRecoverConfig();
        recoverConfig.setMaxRetryCount(3);
        recoverConfig.setRecoverDuration(120);
        recoverConfig.setCronExpression("0 */1 * * * ?");
        recoverConfig.setDelayCancelExceptions(Sets.newHashSet(TimeoutException.class));
        return recoverConfig;
    }

    @Bean("capabilityDataSourceTransactionManager")
    public PlatformTransactionManager createTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
