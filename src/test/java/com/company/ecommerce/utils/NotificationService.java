package com.company.ecommerce.utils;

//import com.company.ecommerce.utils.ConfigManager;
import com.company.ecommerce.config.ConfigManager;
import org.testng.ITestResult;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试失败通知服务
 * 支持邮件、Slack、钉钉、企业微信等多种通知方式
 */
public class NotificationService {

    private static final ConfigManager config = new ConfigManager();
    private static final boolean ENABLED = config.getBooleanProperty("notifications.enabled", false);
    private static final String NOTIFICATION_TYPE = config.getProperty("notifications.type", "email");

    // 通知模板
    private static final String FAILURE_TEMPLATE =
            "测试执行失败通知\n" +
                    "================\n" +
                    "测试名称: %s\n" +
                    "执行时间: %s\n" +
                    "失败原因: %s\n" +
                    "堆栈跟踪: %s\n" +
                    "环境: %s\n" +
                    "项目: %s";

    private static final String HTML_FAILURE_TEMPLATE =
            "<html><body>" +
                    "<h2 style='color: #d9534f;'>🚨 测试执行失败通知</h2>" +
                    "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px;'>" +
                    "<p><strong>测试名称:</strong> %s</p>" +
                    "<p><strong>执行时间:</strong> %s</p>" +
                    "<p><strong>失败原因:</strong> <span style='color: #d9534f;'>%s</span></p>" +
                    "<p><strong>环境:</strong> %s</p>" +
                    "<p><strong>项目:</strong> %s</p>" +
                    "</div>" +
                    "<pre style='background-color: #2c3e50; color: #ecf0f1; padding: 10px; border-radius: 5px; overflow-x: auto;'>%s</pre>" +
                    "</body></html>";

    /**
     * 发送测试失败通知
     */
    public static void sendFailureNotification(ITestResult result) {
        if (!ENABLED) {
            System.out.println("通知服务未启用");
            return;
        }

        try {
            Map<String, String> notificationData = buildNotificationData(result);

            switch (NOTIFICATION_TYPE.toLowerCase()) {
                case "email":
                    sendEmailNotification(notificationData);
                    break;
                case "slack":
                    sendSlackNotification(notificationData);
                    break;
                case "dingtalk":
                    sendDingTalkNotification(notificationData);
                    break;
                case "wechat":
                    sendWeChatNotification(notificationData);
                    break;
                default:
                    System.err.println("不支持的 notification.type: " + NOTIFICATION_TYPE);
            }

            System.out.println("失败通知已发送");
        } catch (Exception e) {
            System.err.println("发送失败通知时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 构建通知数据
     */
    private static Map<String, String> buildNotificationData(ITestResult result) {
        Map<String, String> data = new HashMap<>();

        String testName = result.getName();
        String className = result.getTestClass().getName();
        Throwable throwable = result.getThrowable();
        String errorMessage = throwable != null ? throwable.getMessage() : "Unknown error";
        String stackTrace = getStackTrace(throwable);

        data.put("testName", testName);
        data.put("className", className);
        data.put("errorMessage", errorMessage);
        data.put("stackTrace", stackTrace);
        data.put("timestamp", new java.util.Date().toString());
        data.put("environment", config.getProperty("environment", "dev"));
        data.put("project", config.getProperty("project.name", "E-commerce Automation"));

        return data;
    }

    /**
     * 获取堆栈跟踪信息
     */
    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) return "";

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 发送邮件通知
     */
    private static void sendEmailNotification(Map<String, String> data) {
        String smtpHost = config.getProperty("email.smtp.host", "smtp.gmail.com");
        String smtpPort = config.getProperty("email.smtp.port", "587");
        String username = config.getProperty("email.username");
        String password = config.getProperty("email.password");
        String from = config.getProperty("email.from", username);
        String to = config.getProperty("email.to");
        boolean useSSL = config.getBooleanProperty("email.ssl", true);
        boolean useTLS = config.getBooleanProperty("email.tls", true);

        if (to == null || to.isEmpty()) {
            System.err.println("未配置收件人邮箱 (email.to)");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", String.valueOf(useTLS));
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        if (useSSL) {
            props.put("mail.smtp.socketFactory.port", smtpPort);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("🚨 测试失败: " + data.get("testName"));

            // 创建多部分消息
//            MimeMultipart multipart = new MimeMultipart("alternative");
//
//            // 纯文本部分
//            MimeBodyPart textPart = new MimeBodyPart();
//            String textContent = String.format(FAILURE_TEMPLATE,
//                    data.get("testName"),
//                    data.get("timestamp"),
//                    data.get("errorMessage"),
//                    data.get("stackTrace"),
//                    data.get("environment"),
//                    data.get("project"));
//            textPart.setText(textContent);
//
//            // HTML 部分
//            MimeBodyPart htmlPart = new MimeBodyPart();
//            String htmlContent = String.format(HTML_FAILURE_TEMPLATE,
//                    data.get("testName"),
//                    data.get("timestamp"),
//                    data.get("errorMessage"),
//                    data.get("environment"),
//                    data.get("project"),
//                    data.get("stackTrace"));
//            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
//
//            multipart.addBodyPart(textPart);
//            multipart.addBodyPart(htmlPart);
//
//            // 附加截图（如果存在）
//            String screenshotPath = "screenshots/" + data.get("testName") + ".png";
//            File screenshot = new File(screenshotPath);
//            if (screenshot.exists()) {
//                MimeBodyPart attachmentPart = new MimeBodyPart();
//                attachmentPart.attachFile(screenshot);
//                attachmentPart.setFileName("failure-screenshot.png");
//                multipart.addBodyPart(attachmentPart);
//            }
//
//            message.setContent(multipart);
            Transport.send(message);

            System.out.println("邮件通知已发送至: " + to);
        } catch (Exception e) {
            throw new RuntimeException("发送邮件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送 Slack 通知
     */
    private static void sendSlackNotification(Map<String, String> data) {
        String webhookUrl = config.getProperty("slack.webhook.url");

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.err.println("未配置 Slack Webhook URL (slack.webhook.url)");
            return;
        }

        String channel = config.getProperty("slack.channel", "#test-notifications");
        String username = config.getProperty("slack.username", "Test Bot");

        String payload = String.format(
                "{" +
                        "\"channel\": \"%s\"," +
                        "\"username\": \"%s\"," +
                        "\"text\": \"🚨 测试失败通知\"," +
                        "\"attachments\": [{" +
                        "\"color\": \"#FF0000\"," +
                        "\"fields\": [" +
                        "{\"title\": \"测试名称\", \"value\": \"%s\", \"short\": true}," +
                        "{\"title\": \"执行时间\", \"value\": \"%s\", \"short\": true}," +
                        "{\"title\": \"失败原因\", \"value\": \"%s\", \"short\": false}," +
                        "{\"title\": \"环境\", \"value\": \"%s\", \"short\": true}," +
                        "{\"title\": \"项目\", \"value\": \"%s\", \"short\": true}" +
                        "]" +
                        "}]" +
                        "}",
                channel,
                username,
                data.get("testName"),
                data.get("timestamp"),
                data.get("errorMessage"),
                data.get("environment"),
                data.get("project")
        );

        sendHttpPostRequest(webhookUrl, payload, "application/json");
    }

    /**
     * 发送钉钉通知
     */
    private static void sendDingTalkNotification(Map<String, String> data) {
        String webhookUrl = config.getProperty("dingtalk.webhook.url");

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.err.println("未配置钉钉 Webhook URL (dingtalk.webhook.url)");
            return;
        }

        String content = String.format(
                "🚨 测试失败通知\n\n" +
                        "**测试名称**: %s\n" +
                        "**执行时间**: %s\n" +
                        "**失败原因**: %s\n" +
                        "**环境**: %s\n" +
                        "**项目**: %s\n\n" +
                        "```\n%s\n```",
                data.get("testName"),
                data.get("timestamp"),
                data.get("errorMessage"),
                data.get("environment"),
                data.get("project"),
                data.get("stackTrace").substring(0, Math.min(data.get("stackTrace").length(), 500))
        );

        String payload = String.format(
                "{" +
                        "\"msgtype\": \"markdown\"," +
                        "\"markdown\": {" +
                        "\"title\": \"测试失败通知\"," +
                        "\"text\": \"%s\"" +
                        "}," +
                        "\"at\": {" +
                        "\"isAtAll\": false" +
                        "}" +
                        "}",
                content.replace("\"", "\\\"")
        );

        sendHttpPostRequest(webhookUrl, payload, "application/json");
    }

    /**
     * 发送企业微信通知
     */
    private static void sendWeChatNotification(Map<String, String> data) {
        String webhookUrl = config.getProperty("wechat.webhook.url");

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            System.err.println("未配置企业微信 Webhook URL (wechat.webhook.url)");
            return;
        }

        String content = String.format(
                "测试失败通知\n\n" +
                        "测试名称: %s\n" +
                        "执行时间: %s\n" +
                        "失败原因: %s\n" +
                        "环境: %s\n" +
                        "项目: %s",
                data.get("testName"),
                data.get("timestamp"),
                data.get("errorMessage"),
                data.get("environment"),
                data.get("project")
        );

        String payload = String.format(
                "{" +
                        "\"msgtype\": \"text\"," +
                        "\"text\": {" +
                        "\"content\": \"%s\"," +
                        "\"mentioned_list\": [\"@all\"]" +
                        "}" +
                        "}",
                content.replace("\"", "\\\"")
        );

        sendHttpPostRequest(webhookUrl, payload, "application/json");
    }

    /**
     * 发送 HTTP POST 请求
     */
    private static void sendHttpPostRequest(String url, String payload, String contentType) {
        try {
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", contentType);
            conn.setDoOutput(true);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("HTTP 通知发送成功");
            } else {
                System.err.println("HTTP 通知发送失败，响应码: " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("发送 HTTP 请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送自定义通知（供其他模块调用）
     */
    public static void sendCustomNotification(String title, String message, NotificationType type) {
        if (!ENABLED) return;

        Map<String, String> data = new HashMap<>();
        data.put("testName", title);
        data.put("errorMessage", message);
        data.put("timestamp", new java.util.Date().toString());
        data.put("environment", config.getProperty("environment", "dev"));
        data.put("project", config.getProperty("project.name", "E-commerce Automation"));

        switch (type) {
            case EMAIL:
                sendEmailNotification(data);
                break;
            case SLACK:
                sendSlackNotification(data);
                break;
            case DINGTALK:
                sendDingTalkNotification(data);
                break;
            case WECHAT:
                sendWeChatNotification(data);
                break;
        }
    }

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        EMAIL, SLACK, DINGTALK, WECHAT
    }
}
