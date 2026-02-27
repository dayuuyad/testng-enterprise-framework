package com.company.ecommerce.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器
 * 用于加载和管理环境配置
 */
public class ConfigManager {

    private static volatile ConfigManager instance;
    private final Properties properties = new Properties();
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final String DEFAULT_ENVIRONMENT = "qa";
    private static final String CONFIG_DIR = "config";
//    private static String currentEnvironment = "qa";
    // 环境变量前缀
    private static final String ENV_PREFIX = "ECOMMERCE_";

    // 配置加载状态
    private boolean isLoaded = false;

    private ConfigManager() {
        // 私有构造函数，单例模式
    }

    /**
     * 获取单例实例
     */
    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                    // 自动加载配置
                    instance.autoLoadConfig();
                }
            }
        }
        return instance;
    }

    private void autoLoadConfig() {
        if (isLoaded) {
            return;
        }

        // 1. 确定环境
        String environment = determineEnvironment();

        // 2. 加载配置文件
        loadConfig(environment);

        // 3. 加载环境变量（优先级高于配置文件）
        loadEnvironmentVariables();

        // 4. 加载系统属性（优先级最高）
        loadSystemProperties();

        isLoaded = true;
        System.out.println("配置加载完成，当前环境: " + environment);
    }

    /**
     * 确定运行环境
     */
    private String determineEnvironment() {
        // 优先级：命令行参数 > 环境变量 > 默认值
        String env = System.getProperty("env");
        if (env == null || env.trim().isEmpty()) {
            env = System.getenv("APP_ENV");
        }
        if (env == null || env.trim().isEmpty()) {
            env = System.getenv("ENVIRONMENT");
        }
        return env != null ? env : DEFAULT_ENVIRONMENT;
    }

    /**
     * 加载环境变量
     */
    private void loadEnvironmentVariables() {
        Map<String, String> env = System.getenv();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(ENV_PREFIX)) {
                // 转换环境变量格式：ECOMMERCE_NOTIFICATIONS_ENABLED -> notifications.enabled
                String normalizedKey = key.substring(ENV_PREFIX.length())
                        .toLowerCase()
                        .replace('_', '.');
                properties.setProperty(normalizedKey, entry.getValue());
            }
        }
    }


    /**
     * 加载系统属性
     */
    private void loadSystemProperties() {
        Properties sysProps = System.getProperties();
        for (String key : sysProps.stringPropertyNames()) {
            if (key.startsWith("ecommerce.") || key.startsWith("app.")) {
                properties.setProperty(key, sysProps.getProperty(key));
            }
        }
    }

    /**
     * 加载配置文件
     *
     * @param environment 环境名称 (dev, qa, staging, production)
     */
    private void loadConfig(String environment) {
        String configFile = environment + ".properties";

        // 获取当前工作目录（通常是项目根目录）
        String projectRoot = System.getProperty("user.dir");
        String configPath = projectRoot + File.separator + CONFIG_DIR + File.separator + configFile;

        try (InputStream input = new FileInputStream(configPath)) {
//        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream("config/" + configFile)) {

            if (input == null) {
                throw new RuntimeException("Configuration file not found: " + configFile);
            }

            properties.clear();
            properties.load(input);

            // 使用系统属性覆盖配置
            properties.putAll(System.getProperties());

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file: " + configFile, e);
        }
    }

    /**
     * 获取配置属性
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public  int getIntProperty(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public  int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    public  boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public  boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    // 常用属性便捷方法
    public String getWebBaseUrl() {
        return getProperty("app.web.url");
    }

    public String getApiBaseUrl() {
        return getProperty("app.api.url");
    }

    public String getBrowserName() {
        return getProperty("browser.name", "chrome");
    }

    public  boolean isHeadless() {
        return getBooleanProperty("browser.headless", false);
    }

    public String getTestDataPath() {
        return getProperty("test.data.path");
    }

    public String getTestUserEmail() {
        return getProperty("test.user.email");
    }

    public String getTestUserPassword() {
        return getProperty("test.user.password");
    }

    public String getDbHost() {
        return getProperty("db.host");
    }

    public String getDbName() {
        return getProperty("db.name");
    }

    public String getDbURL() {
        return getProperty("db.url");
    }

    public String getDbUsername() {
        return getProperty("db.username");
    }

    public String getDbPassword() {
        return getProperty("db.password");
    }

    public  int getTestTimeout() {
        return getIntProperty("test.timeout", 300);
    }

    public  int getTestRetryCount() {
        return getIntProperty("test.retry.count", 2);
    }

    public  boolean isScreenshotOnFailure() {
        return getBooleanProperty("test.screenshot.on.failure", true);
    }

//    public String getCurrentEnvironment() {
//        return currentEnvironment;
//    }

    public String getEnvironmentName() {
        return getProperty("env.name", "Unknown");
    }

    /**
     * 设置配置属性（用于测试）
     */
    public  void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getApiSecretKey() {
        return getProperty("app.api.secretKey");
    }

    public String getApiAppId() {
        return getProperty("app.api.appId");
    }

    public String getServiceCode() {
        return getProperty("app.api.serviceCode");

    }

    public String getCookieStr() {
        return getProperty("cookieStr");
    }
}
