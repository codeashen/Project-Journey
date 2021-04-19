package com.ashen.xunwu.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * es监控定时任务
 */
@Component
@Slf4j
public class ESMonitor {
    private static final String HEALTH_CHECK_API = "http://127.0.0.1:9200/_cluster/health";

    private static final String GREEN = "green";
    private static final String YELLOW = "yellow";
    private static final String RED = "red";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender mailSender;

   @Scheduled(fixedDelay = 10 * 1000)  // 10秒一次
    public void healthCheck() {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(HEALTH_CHECK_API);

        try {
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                log.error("无法正常访问ES服务！请检查服务器。");
            } else {
                String body = EntityUtils.toString(response.getEntity(), "UTF-8");
                JsonNode result = objectMapper.readTree(body);
                String status = result.get("status").asText();

                String message = "";
                boolean isNormal = false;
                switch (status) {
                    case GREEN:
                        message = "ES服务器正常运行";
                        isNormal = true;
                        break;
                    case YELLOW:
                        message = "ES服务器的状态为YELLOW！请检查ES服务器！";
                        break;
                    case RED:
                        message = "ES服务器的状态为RED！请立即排查！";
                        break;
                    default:
                        message = "位置的ES服务器状态: " + status + ", 请检查";
                        break;
                }

                if (!isNormal) {
                    sendAlertMessage(message);
                    log.error(message);

                } else {
                    log.info(message);
                }

                // 获取集群节点
                int totalNodes = result.get("number_of_nodes").asInt();
                if (totalNodes < 5) {
                    sendAlertMessage("节点少于5个了, 请检查节点状态！");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendAlertMessage(String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("1103940541@qq.com");
        mailMessage.setTo("1103940541@qq.com");
        mailMessage.setSubject("【警告】ES服务监控");
        mailMessage.setText(message);
        mailSender.send(mailMessage);
    }
}
