package com.stylefeng.guns.order.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * FTP工具类，封装Apache的FTPClient操作
 * 参看测试方法 com.stylefeng.guns.order.CcOrderApplicationTests#testFTPUtil()
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "ftp")
public class FTPUtil {
    // 地址 端口 用户名 密码
    private String hostname;
    private Integer port;
    private String username;
    private String password;

    // Apache的ftp操作类
    private FTPClient ftpClient = null;

    /**
     * 初始化FTPClient，连接FTP服务器
     */
    private void initFTPClient() {
        try {
            ftpClient = new FTPClient();
            ftpClient.setControlEncoding("utf-8");
            ftpClient.connect(hostname, port);
            ftpClient.login(username, password);
        } catch (Exception e) {
            log.error("初始化FTP失败", e);
        }
    }

    /**
     * 输入一个路径，然后将路径里的文件转换成字符串返回
     * @param fileAddress
     * @return
     */
    public String getFileStrByAddress(String fileAddress) {
        // 初始化 FTPClient
        initFTPClient();

        try (
                // 通过 FTPClient 获取文件输入流，转换成BufferedReader
                InputStream fileStream = ftpClient.retrieveFileStream(fileAddress);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileStream));
        ) {
            // 循环读取文件内容，直到结束
            StringBuilder stringBuffer = new StringBuilder();
            while (true) {
                String lineStr = bufferedReader.readLine();
                if (lineStr == null) {
                    break;
                }
                stringBuffer.append(lineStr);
            }
            // 退出jFTP客户端，返回数据
            ftpClient.logout();
            return stringBuffer.toString();
        } catch (Exception e) {
            log.error("获取文件信息失败", e);
            e.printStackTrace();
        }
        return null;
    }
    
}
