package com.stylefeng.guns.alipay.modular.alipay.util;

import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.*;

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
    private String uploadPath;  // 上传路径

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
     *
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

    /**
     * 上传到ftp服务器
     *
     * @param fileName 文件名
     * @param file     文件
     * @return
     */
    public boolean uploadFile(String fileName, File file) {
        try {
            @Cleanup FileInputStream fileInputStream = new FileInputStream(file);

            // FTP相关内容
            initFTPClient();
            // 设置FTP的关键参数
            ftpClient.setControlEncoding("utf-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            // 将ftpClient的工作空间修改
            ftpClient.changeWorkingDirectory(this.getUploadPath());
            // 上传文件
            ftpClient.storeFile(fileName, fileInputStream);
            return true;
            
        } catch (Exception e) {
            log.error("上传失败", e);
            return false;
        } finally {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
