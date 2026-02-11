package com.company.ecommerce.utils;

import com.company.ecommerce.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理工具类
 * 提供数据库连接、查询、更新等操作
 */
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private Connection connection;
    private boolean isConnected = false;

    // 连接池配置
    private static final int MAX_POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT = 30;
    private static final String JDBC_URL_TEMPLATE = "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC";

    /**
     * 获取数据库连接
     */
    public void connect() {
        if (isConnected && connection != null) {
            logger.info("数据库连接已存在，复用现有连接");
            return;
        }

        try {
            String host = ConfigManager.getDbHost();
            String port = ConfigManager.getProperty("db.port", "3306");
            String database = ConfigManager.getDbName();
            String username = ConfigManager.getDbUsername();
            String password = ConfigManager.getDbPassword();

            String jdbcUrl = String.format(JDBC_URL_TEMPLATE, host, port, database);

            logger.info("连接数据库: {}", database);
            logger.debug("JDBC URL: {}", jdbcUrl);

            // 加载 MySQL 驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 建立连接
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            isConnected = true;

            logger.info("✅ 数据库连接成功");

        } catch (ClassNotFoundException e) {
            logger.error("❌ MySQL JDBC 驱动未找到", e);
            throw new RuntimeException("MySQL JDBC driver not found", e);
        } catch (SQLException e) {
            logger.error("❌ 数据库连接失败", e);
            throw new RuntimeException("Database connection failed", e);
        }
    }

    /**
     * 执行查询并返回结果列表
     */
    public List<Map<String, Object>> executeQuery(String sql, Object... params) {
        checkConnection();
        List<Map<String, Object>> resultList = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            logger.debug("执行查询: {}", sql);
            if (params.length > 0) {
                logger.debug("参数: {}", (Object) params);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    resultList.add(row);
                }
            }

            logger.info("查询返回 {} 条记录", resultList.size());
            return resultList;

        } catch (SQLException e) {
            logger.error("❌ 查询执行失败: {}", sql, e);
            throw new RuntimeException("Query execution failed", e);
        }
    }

    /**
     * 执行更新操作（INSERT, UPDATE, DELETE）
     */
    public int executeUpdate(String sql, Object... params) {
        checkConnection();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            logger.debug("执行更新: {}", sql);
            if (params.length > 0) {
                logger.debug("参数: {}", (Object) params);
            }

            int affectedRows = stmt.executeUpdate();
            logger.info("更新影响 {} 行", affectedRows);
            return affectedRows;

        } catch (SQLException e) {
            logger.error("❌ 更新执行失败: {}", sql, e);
            throw new RuntimeException("Update execution failed", e);
        }
    }

    /**
     * 查询单个值
     */
    public Object queryForObject(String sql, Object... params) {
        List<Map<String, Object>> results = executeQuery(sql, params);
        if (results.isEmpty() || results.get(0).isEmpty()) {
            return null;
        }
        return results.get(0).values().iterator().next();
    }

    /**
     * 查询单个值并转换为指定类型
     */
    public <T> T queryForObject(String sql, Class<T> clazz, Object... params) {
        Object result = queryForObject(sql, params);
        if (result == null) {
            return null;
        }
        return clazz.cast(result);
    }

    /**
     * 批量插入数据
     */
    public int[] batchInsert(String sql, List<Object[]> batchParams) {
        checkConnection();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Object[] params : batchParams) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.addBatch();
            }

            logger.info("批量插入 {} 条记录", batchParams.size());
            int[] result = stmt.executeBatch();
            logger.info("批量插入完成");
            return result;

        } catch (SQLException e) {
            logger.error("❌ 批量插入失败", e);
            throw new RuntimeException("Batch insert failed", e);
        }
    }

    /**
     * 开启事务
     */
    public void beginTransaction() {
        checkConnection();
        try {
            connection.setAutoCommit(false);
            logger.info("事务已开启");
        } catch (SQLException e) {
            logger.error("❌ 开启事务失败", e);
            throw new RuntimeException("Begin transaction failed", e);
        }
    }

    /**
     * 提交事务
     */
    public void commitTransaction() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.commit();
                connection.setAutoCommit(true);
                logger.info("事务已提交");
            }
        } catch (SQLException e) {
            logger.error("❌ 提交事务失败", e);
            throw new RuntimeException("Commit transaction failed", e);
        }
    }

    /**
     * 回滚事务
     */
    public void rollbackTransaction() {
        try {
            if (connection != null && !connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
                logger.info("事务已回滚");
            }
        } catch (SQLException e) {
            logger.error("❌ 回滚事务失败", e);
            throw new RuntimeException("Rollback transaction failed", e);
        }
    }

    /**
     * 创建测试数据
     */
    public void createTestData(String tableName, Map<String, Object> data) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder("VALUES (");

        List<Object> params = new ArrayList<>();
        int count = 0;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (count > 0) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(entry.getKey());
            values.append("?");
            params.add(entry.getValue());
            count++;
        }

        sql.append(") ").append(values).append(")");
        executeUpdate(sql.toString(), params.toArray());
    }

    /**
     * 清理测试数据
     */
    public void cleanupTestData(String tableName) {
        String sql = "DELETE FROM " + tableName + " WHERE created_at < NOW() - INTERVAL 1 DAY";
        int deleted = executeUpdate(sql);
        logger.info("清理 {} 表中的 {} 条旧测试数据", tableName, deleted);
    }

    /**
     * 检查表是否存在
     */
    public boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = ? AND table_name = ?";

        String dbName = ConfigManager.getDbName();
        Long count = queryForObject(sql, Long.class, dbName, tableName);
        return count != null && count > 0;
    }

    /**
     * 断开数据库连接
     */
    public void disconnect() {
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
                connection.close();
                isConnected = false;
                logger.info("✅ 数据库连接已关闭");
            } catch (SQLException e) {
                logger.error("❌ 关闭数据库连接失败", e);
            } finally {
                connection = null;
            }
        }
    }

    /**
     * 检查连接状态
     */
    private void checkConnection() {
        if (!isConnected || connection == null) {
            throw new IllegalStateException("数据库未连接，请先调用 connect() 方法");
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            if (connection == null || connection.isClosed()) {
                return false;
            }
            String sql = "SELECT 1";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() && rs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            logger.error("数据库健康检查失败", e);
            return false;
        }
    }

    /**
     * 获取数据库信息
     */
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            info.put("DatabaseProductName", connection.getMetaData().getDatabaseProductName());
            info.put("DatabaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            info.put("DriverName", connection.getMetaData().getDriverName());
            info.put("DriverVersion", connection.getMetaData().getDriverVersion());
            info.put("URL", connection.getMetaData().getURL());
            info.put("UserName", connection.getMetaData().getUserName());
        } catch (SQLException e) {
            logger.error("获取数据库信息失败", e);
        }
        return info;
    }

//    @Override
//    protected void finalize() throws Throwable {
//        try {
//            disconnect();
//        } finally {
//            super.finalize();
//        }
//    }
}