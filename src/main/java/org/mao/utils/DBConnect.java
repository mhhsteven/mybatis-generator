package org.mao.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class DBConnect {

    private String driver;
    private String url;
    private String username;
    private String password;
    private Connection conn;
    private Statement stmt;
    private PreparedStatement ps;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DBConnect(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 打开数据库连接
     *
     * @throws Exception
     */
    public void opendb() throws Exception {
        try {
            closedb();//不管开着还是关着先关一次
            // 初始化
            Class.forName(driver);// 加载驱动
            conn = DriverManager.getConnection(url, username, password);
            stmt = conn.createStatement();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 开始事务
     *
     * @throws Exception
     */
    public void beginTransaction() throws Exception {
        try {
            conn.setAutoCommit(false);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 提交事务
     *
     * @throws Exception
     */
    public void commit() throws Exception {
        try {
            conn.commit();
            conn.setAutoCommit(true);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 回滚事务
     *
     * @throws Exception
     */
    public void rollback() {
        try {
            conn.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用executeQuerySql()
     *
     * @param sql 需要执行的sql语句
     * @return 查询到的记录集合
     * @throws Exception
     */
    public ResultSet executeQuerySql(String sql) throws Exception {
        ResultSet rs = stmt.executeQuery(sql);
        return rs;
    }

    /**
     * @param sql
     * @return
     * @throws Exception
     */
    public ResultSet executePrepareQuerySql(String sql, List params) throws Exception {
        ps = conn.prepareStatement(sql);
        for (int i = 1; i < params.size() + 1; i++) {
            Object value = params.get(i - 1);
            if (value == null) {
                ps.setObject(i, value);
            } else if (value instanceof String) {
                ps.setString(i, value.toString());
            } else if (value instanceof Date) {
                ps.setTimestamp(i, new Timestamp(((Date) value).getTime()));
            } else if (value instanceof Integer) {
                ps.setInt(i, Integer.parseInt(value.toString()));
            } else if (value instanceof Long) {
                ps.setLong(i, Long.parseLong(value.toString()));
            } else if (value instanceof Double) {
                ps.setDouble(i, Double.parseDouble(value.toString()));
            } else if (value instanceof byte[]) {
                ps.setBytes(i, (byte[]) value);
            }
        }
        ResultSet rs = ps.executeQuery();
        return rs;
    }

    /**
     * 调用executeUpdate()方法
     *
     * @param sql
     * @return 为更新的记录数,-1为更新失败
     * @throws Exception
     */
    public int executeUpdateSql(String sql) throws Exception {
        int i = stmt.executeUpdate(sql);
        return i;
    }

    /**
     * 关闭数据库连接
     *
     * @throws Exception
     */
    public void closedb() {
        try {
            if (ps != null)
                ps.close();
            if (stmt != null)
                stmt.close();
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入一条数据
     *
     * @param map
     * @throws Exception
     */
    public String insert(HashMap map, String tableName) throws Exception {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder insertSql = new StringBuilder("insert into " + tableName + "(");
        StringBuilder setvalues = new StringBuilder(" values(");
        Set set = map.entrySet();
        Iterator it = set.iterator();
        //遍历Hashmap生成insert语句
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            String field = (String) entry.getKey();
            Object value = entry.getValue();
            insertSql.append(field).append(", ");
            if (value == null) {
                setvalues.append("null, ");
            } else if (value instanceof String) {
                setvalues.append("'").append(value).append("', ");
            } else if (value instanceof Date) {
                setvalues.append("'").append(sdf.format((Date) value)).append("', ");
            } else if (value instanceof Number) {
                setvalues.append(value).append(", ");
            }
        }
        insertSql.delete(insertSql.lastIndexOf(","), insertSql.lastIndexOf(" ") + 1);
        setvalues.delete(setvalues.lastIndexOf(","), setvalues.lastIndexOf(" ") + 1);
        String sql = insertSql.append(")").append(setvalues.append(")")).toString();
        System.out.println(sql);
        return sql;
    }
}
