package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 截图工具类
 * 用于在测试失败或需要时捕获屏幕截图
 */
public class ScreenshotUtils {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtils.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final SimpleDateFormat FOLDER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 捕获屏幕截图
     * @param testName 测试名称
     * @return 截图文件路径
     */
    public static String capture(String testName) {
        WebDriver driver = getCurrentDriver();
        return capture(driver, testName);
    }

    public static String capture(WebDriver driver, String testName) {
        if (driver == null) {
            logger.warn("WebDriver 为空，无法截图");
            return null;
        }

        try {
            // 检查截图功能是否启用
            if (!ConfigManager.getInstance().isScreenshotOnFailure()) {
                logger.debug("截图功能已禁用");
                return null;
            }

            // 确保driver支持截图
            if (!(driver instanceof TakesScreenshot)) {
                logger.warn("WebDriver 不支持截图: {}", driver.getClass().getName());
                return null;
            }

            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;

            // 创建截图目录
            String screenshotDir = createScreenshotDirectory();

            // 生成文件名
            String timestamp = DATE_FORMAT.format(new Date());
            String safeTestName = sanitizeFileName(testName);
            String fileName = String.format("%s_%s.png", safeTestName, timestamp);
            String filePath = Paths.get(screenshotDir, fileName).toString();

            // 捕获截图
            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);

            // 保存截图
            saveScreenshot(screenshotBytes, filePath);

            // 创建缩略图
            createThumbnail(screenshotBytes, filePath);

            logger.info("📸 截图已保存: {}", filePath);
            return filePath;

        } catch (Exception e) {
            logger.error("❌ 截图失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 在测试失败时自动截图
     */
    public static String captureOnFailure(ITestResult result) {
        WebDriver driver = getCurrentDriver();
        if (driver == null) {
            logger.warn("无法获取 WebDriver，跳过失败截图");
            return null;
        }

        String testName = result.getName();
        logger.info("测试失败，正在截图: {}", testName);

        return capture(driver, "FAILED_" + testName);
    }

    /**
     * 捕获特定元素的截图
     */
    public static String captureElement(WebDriver driver, org.openqa.selenium.WebElement element, String elementName) {
        if (driver == null || element == null) {
            logger.warn("WebDriver 或元素为空，无法截图");
            return null;
        }

        try {
            // 创建截图目录
            String screenshotDir = createScreenshotDirectory();

            // 生成文件名
            String timestamp = DATE_FORMAT.format(new Date());
            String safeElementName = sanitizeFileName(elementName);
            String fileName = String.format("ELEMENT_%s_%s.png", safeElementName, timestamp);
            String filePath = Paths.get(screenshotDir, fileName).toString();

            // 捕获元素截图
            byte[] screenshotBytes = element.getScreenshotAs(OutputType.BYTES);

            // 保存截图
            saveScreenshot(screenshotBytes, filePath);

            logger.info("📸 元素截图已保存: {} - {}", elementName, filePath);
            return filePath;

        } catch (Exception e) {
            logger.error("❌ 元素截图失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 捕获完整页面截图（包括滚动部分）
     */
    public static String captureFullPage(WebDriver driver, String testName) {
        if (driver == null) {
            logger.warn("WebDriver 为空，无法截图");
            return null;
        }

        try {
            // 使用 JavaScript 获取页面完整高度
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
            Long totalHeight = (Long) js.executeScript("return Math.max(" +
                    "document.body.scrollHeight, " +
                    "document.body.offsetHeight, " +
                    "document.documentElement.clientHeight, " +
                    "document.documentElement.scrollHeight, " +
                    "document.documentElement.offsetHeight);");

            // 获取当前窗口高度
            Long windowHeight = (Long) js.executeScript("return window.innerHeight");

            // 创建截图目录
            String screenshotDir = createScreenshotDirectory();
            String timestamp = DATE_FORMAT.format(new Date());
            String safeTestName = sanitizeFileName(testName);

            // 如果页面高度大于窗口高度，需要分段截图并拼接
            if (totalHeight > windowHeight) {
                return captureScrollPage(driver, testName, totalHeight, windowHeight, screenshotDir, timestamp, safeTestName);
            } else {
                // 普通截图
                String fileName = String.format("FULL_%s_%s.png", safeTestName, timestamp);
                String filePath = Paths.get(screenshotDir, fileName).toString();

                byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                saveScreenshot(screenshotBytes, filePath);

                logger.info("📸 完整页面截图已保存: {}", filePath);
                return filePath;
            }

        } catch (Exception e) {
            logger.error("❌ 完整页面截图失败，使用普通截图", e);
            return capture(driver, "FULL_" + testName);
        }
    }

    /**
     * 滚动截图（长页面）
     */
    private static String captureScrollPage(WebDriver driver, String testName, Long totalHeight,
                                            Long windowHeight, String screenshotDir,
                                            String timestamp, String safeTestName) throws IOException {
        org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) driver;
        TakesScreenshot screenshotDriver = (TakesScreenshot) driver;

        // 计算需要截图的次数
        int screenshotsCount = (int) Math.ceil((double) totalHeight / windowHeight);

        // 保存各部分截图
        String[] partPaths = new String[screenshotsCount];

        for (int i = 0; i < screenshotsCount; i++) {
            // 滚动到相应位置
            js.executeScript(String.format("window.scrollTo(0, %d);", i * windowHeight));

            // 等待滚动完成
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // 截图
            String partFileName = String.format("PART_%s_%s_%d.png", safeTestName, timestamp, i);
            String partFilePath = Paths.get(screenshotDir, partFileName).toString();

            byte[] screenshotBytes = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            saveScreenshot(screenshotBytes, partFilePath);

            partPaths[i] = partFilePath;
        }

        // 合并截图（需要图像处理库）
        String finalFileName = String.format("FULL_SCROLL_%s_%s.png", safeTestName, timestamp);
        String finalFilePath = Paths.get(screenshotDir, finalFileName).toString();

        mergeScreenshots(partPaths, finalFilePath, windowHeight.intValue());

        // 清理临时文件
        for (String partPath : partPaths) {
            try {
                Files.deleteIfExists(Paths.get(partPath));
            } catch (IOException e) {
                logger.warn("无法删除临时截图文件: {}", partPath);
            }
        }

        logger.info("📸 滚动截图已保存: {}", finalFilePath);
        return finalFilePath;
    }

    /**
     * 合并多张截图
     */
    private static void mergeScreenshots(String[] screenshotPaths, String outputPath, int windowHeight) throws IOException {
        // 如果没有图像处理库，简单处理
        if (!hasImageProcessingLibrary()) {
            logger.warn("缺少图像处理库，无法合并截图，使用第一张截图");
            if (screenshotPaths.length > 0) {
                Files.copy(Paths.get(screenshotPaths[0]), Paths.get(outputPath));
            }
            return;
        }

        try {
            // 使用 Java 原生 ImageIO 合并
            BufferedImage[] images = new BufferedImage[screenshotPaths.length];
            int totalHeight = 0;
            int maxWidth = 0;

            // 加载所有图片
            for (int i = 0; i < screenshotPaths.length; i++) {
                BufferedImage img = ImageIO.read(new File(screenshotPaths[i]));
                images[i] = img;
                totalHeight += img.getHeight();
                maxWidth = Math.max(maxWidth, img.getWidth());
            }

            // 创建合并后的图片
            BufferedImage combined = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            java.awt.Graphics2D g = combined.createGraphics();

            int currentHeight = 0;
            for (BufferedImage img : images) {
                g.drawImage(img, 0, currentHeight, null);
                currentHeight += img.getHeight();
            }

            g.dispose();

            // 保存合并后的图片
            ImageIO.write(combined, "PNG", new File(outputPath));

        } catch (Exception e) {
            logger.error("合并截图失败", e);
            throw new IOException("Failed to merge screenshots", e);
        }
    }

    /**
     * 创建缩略图
     */
    private static void createThumbnail(byte[] screenshotBytes, String originalPath) {
        if (!ConfigManager.getInstance().getBooleanProperty("screenshot.thumbnail.enabled", true)) {
            return;
        }

        try {
            // 检查是否有图像处理库
            if (!hasImageProcessingLibrary()) {
                return;
            }

            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(screenshotBytes));

            // 创建缩略图尺寸
            int thumbnailWidth = 200;
            int thumbnailHeight = (int) ((double) originalImage.getHeight() / originalImage.getWidth() * thumbnailWidth);

            // 创建缩略图
            java.awt.Image thumbnail = originalImage.getScaledInstance(
                    thumbnailWidth, thumbnailHeight, java.awt.Image.SCALE_SMOOTH);

            BufferedImage bufferedThumbnail = new BufferedImage(
                    thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);

            bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);

            // 保存缩略图
            Path original = Paths.get(originalPath);
            String thumbnailName = original.getFileName().toString().replace(".png", "_thumb.png");
            Path thumbnailPath = original.getParent().resolve(thumbnailName);

            ImageIO.write(bufferedThumbnail, "PNG", thumbnailPath.toFile());

            logger.debug("缩略图已创建: {}", thumbnailPath);

        } catch (Exception e) {
            logger.warn("创建缩略图失败，跳过: {}", e.getMessage());
        }
    }

    /**
     * 保存截图到文件
     */
    private static void saveScreenshot(byte[] screenshotBytes, String filePath) throws IOException {
        Files.createDirectories(Paths.get(filePath).getParent());
        Files.write(Paths.get(filePath), screenshotBytes);

        // 记录文件信息
        File file = new File(filePath);
        logger.debug("截图保存: {} ({} bytes)", filePath, file.length());
    }

    /**
     * 创建截图目录
     */
    private static String createScreenshotDirectory() {
        String baseDir = ConfigManager.getInstance().getProperty("screenshot.base.dir", "test-results/screenshots");
        String dateFolder = FOLDER_DATE_FORMAT.format(new Date());

        Path screenshotDir = Paths.get(baseDir, dateFolder);

        try {
            Files.createDirectories(screenshotDir);
            logger.debug("截图目录: {}", screenshotDir);
        } catch (IOException e) {
            logger.error("创建截图目录失败: {}", screenshotDir, e);
            // 使用备选目录
            screenshotDir = Paths.get("screenshots", dateFolder);
            try {
                Files.createDirectories(screenshotDir);
            } catch (IOException ex) {
                logger.error("备选截图目录创建也失败", ex);
            }
        }

        return screenshotDir.toString();
    }

    /**
     * 清理旧的截图文件
     */
    public static void cleanupOldScreenshots(int daysToKeep) {
        try {
            String baseDir = ConfigManager.getInstance().getProperty("screenshot.base.dir", "test-results/screenshots");
            File screenshotsDir = new File(baseDir);

            if (!screenshotsDir.exists() || !screenshotsDir.isDirectory()) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
            int deletedCount = 0;

            for (File dateDir : screenshotsDir.listFiles()) {
                if (dateDir.isDirectory() && dateDir.lastModified() < cutoffTime) {
                    deleteDirectory(dateDir);
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                logger.info("清理了 {} 个旧的截图目录", deletedCount);
            }

        } catch (Exception e) {
            logger.error("清理旧截图失败", e);
        }
    }

    /**
     * 递归删除目录
     */
    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }

    /**
     * 获取当前线程的 WebDriver
     */
    private static WebDriver getCurrentDriver() {
        try {
            // 尝试从 WebDriverManager 获取
            return WebDriverManagerUtil.getDriver();
        } catch (Exception e) {
            logger.debug("无法从 WebDriverManager 获取 driver: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否有图像处理库
     */
    private static boolean hasImageProcessingLibrary() {
        try {
            Class.forName("javax.imageio.ImageIO");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 清理文件名中的非法字符
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed";
        }

        // 替换非法字符
        return fileName
                .replaceAll("[\\\\/:*?\"<>|]", "_")  // Windows 非法字符
                .replaceAll("\\s+", "_")              // 空格
                .replaceAll("[^a-zA-Z0-9_.-]", "_")   // 其他特殊字符
                .replaceAll("_+", "_")                // 多个下划线合并为一个
                .trim();
    }

    /**
     * 获取截图存储的基本信息
     */
    public static Map<String, Object> getScreenshotStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            String baseDir = ConfigManager.getInstance().getProperty("screenshot.base.dir", "test-results/screenshots");
            File dir = new File(baseDir);

            if (dir.exists() && dir.isDirectory()) {
                long totalSize = 0;
                int totalFiles = 0;

                // 递归计算大小和数量
                for (File dateDir : dir.listFiles()) {
                    if (dateDir.isDirectory()) {
                        totalFiles += countFiles(dateDir);
                        totalSize += getDirectorySize(dateDir);
                    }
                }

                stats.put("totalScreenshots", totalFiles);
                stats.put("totalSizeBytes", totalSize);
                stats.put("totalSizeMB", String.format("%.2f", totalSize / (1024.0 * 1024.0)));
                stats.put("screenshotDirectory", dir.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("获取截图统计信息失败", e);
        }

        return stats;
    }

    private static int countFiles(File dir) {
        int count = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        count++;
                    } else if (file.isDirectory()) {
                        count += countFiles(file);
                    }
                }
            }
        }
        return count;
    }

    private static long getDirectorySize(File dir) {
        long size = 0;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }
}