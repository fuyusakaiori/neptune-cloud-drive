package com.neptune.cloud.drive.storage.engine.fastdfs.config;

import com.github.tobato.fastdfs.conn.ConnectionPoolConfig;
import com.github.tobato.fastdfs.conn.FdfsConnectionPool;
import com.github.tobato.fastdfs.conn.PooledConnectionFactory;
import com.github.tobato.fastdfs.conn.TrackerConnectionManager;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;

import java.util.Collections;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("com.neptune.cloud.drive.storage.engine.fastdfs")
// 扫描 fastdfs 相关的类并注入到容器中
@ComponentScan(value = {"com.github.tobato.fastdfs.service", "com.github.tobato.fastdfs.domain"})
// 因为 fastdfs 存在相同的类, 所以需要声明忽略已存在的类
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class FastDFSStorageEngineConfig {

    /**
     * 连接超时时间
     */
    private int connectTimeout = 600;

    /**
     * Tracker 服务端的地址
     */
    private List<String> trackerServers = Collections.singletonList("127.0.0.1:22122");

    /**
     * 组名
     */
    private String group = "group1";

    /**
     * 连接池工厂
     */
    @Bean
    public PooledConnectionFactory pooledConnectionFactory() {
        // 1. 初始化连接工厂
        PooledConnectionFactory factory = new PooledConnectionFactory();
        // 2. 设置超时时间
        factory.setConnectTimeout(connectTimeout);

        return factory;
    }

    /**
     * 连接池配置
     */
    @Bean
    public ConnectionPoolConfig connectionPoolConfig() {
        return new ConnectionPoolConfig();
    }

    /**
     * fastdfs 连接池
     */
    @Bean
    public FdfsConnectionPool fdfsConnectionPool(ConnectionPoolConfig config, PooledConnectionFactory factory) {
        return new FdfsConnectionPool(factory, config);
    }

    /**
     *
     */
    @Bean
    public TrackerConnectionManager trackerConnectionManager(FdfsConnectionPool connectionPool) {
        // 1. 初始化 tracker 连接管理器
        TrackerConnectionManager manager = new TrackerConnectionManager(connectionPool);
        // 2. 设置 tracker 的连接
        manager.setTrackerList(trackerServers);

        return manager;
    }

}
