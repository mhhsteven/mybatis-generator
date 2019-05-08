package org.mao.utils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * 用于自动生成数据库表的实体类，映射xml，dao类，service类
 *
 * @author admin
 */
public class DBSchemaUtil extends JFrame {

    // private DBSchemaUtil util=null;
    private int width = 800;
    private int height = 660;
    private String url = null;
    private String drivername = null;
    private String username = null;
    private String password = null;
    private String dbname = null;
    private JTextField driverNameField = null;
    private JTextField urlField = null;
    private JTextField usernameField = null;
    private JTextField passwordField = null;
    private JTextField filePathField = null;
    private JTextField packageField = null;
    private JTextField moduleField = null;
    private JTable jtable = null;
    private DefaultTableModel tableModel = null;
    //数据库配置文件路径
    private String jdbcPropertiesFilePath = "/conf/jdbc.properties";
    //仓库前缀
    private String repositoryPrefix = "";
    //模块的名称
    private String basePackage = "";
    //生成源代码的根路径，一般是git库的本地路径
    private String filePath = "E:/mybatis-generator/" + repositoryPrefix + "-" + basePackage;
    //子模块名称
    private String module = "";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        DBSchemaUtil util = new DBSchemaUtil();
        util.setVisible(true);
        util.readJdbcProperties();
    }

    public DBSchemaUtil() {
        this.setLayout(null);
        this.setTitle("基础类生成工具");
        this.setSize(width, height);
        this.setResizable(false);
        this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width / 2 - width / 2, Toolkit.getDefaultToolkit().getScreenSize().height / 2 - height / 2);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setSize(width, height);
        panel.setLocation(0, 0);
        // panel.setBackground(Color.red);
        panel.setLayout(null);

        JLabel label1 = new JLabel();
        label1.setText("驱动类名");
        label1.setSize(100, 25);
        label1.setLocation(20, 20);
        panel.add(label1);

        driverNameField = new JTextField();
        driverNameField.setSize(600, 25);
        driverNameField.setLocation(140, 20);
        panel.add(driverNameField);

        JLabel label2 = new JLabel();
        label2.setText("连接字符串");
        label2.setSize(100, 25);
        label2.setLocation(20, 50);
        panel.add(label2);

        urlField = new JTextField();
        urlField.setSize(600, 25);
        urlField.setLocation(140, 50);
        panel.add(urlField);

        JLabel label3 = new JLabel();
        label3.setText("用户名");
        label3.setSize(100, 25);
        label3.setLocation(20, 80);
        panel.add(label3);

        usernameField = new JTextField();
        usernameField.setSize(600, 25);
        usernameField.setLocation(140, 80);
        panel.add(usernameField);

        JLabel label4 = new JLabel();
        label4.setText("密码");
        label4.setSize(100, 25);
        label4.setLocation(20, 110);
        panel.add(label4);

        passwordField = new JTextField();
        passwordField.setSize(600, 25);
        passwordField.setLocation(140, 110);
        panel.add(passwordField);

        JLabel label5 = new JLabel();
        label5.setText("生成文件路径");
        label5.setSize(120, 25);
        label5.setLocation(20, 140);
        panel.add(label5);

        filePathField = new JTextField();
        filePathField.setSize(600, 25);
        filePathField.setLocation(140, 140);
        filePathField.setText(filePath);
        panel.add(filePathField);

        JLabel label6 = new JLabel();
        label6.setText("模块名称");
        label6.setSize(100, 25);
        label6.setLocation(20, 170);
        panel.add(label6);

        packageField = new JTextField();
        packageField.setSize(600, 25);
        packageField.setLocation(140, 170);
        packageField.setText(basePackage);
        panel.add(packageField);

        JLabel label7 = new JLabel();
        label7.setText("子模块名称");
        label7.setSize(100, 25);
        label7.setLocation(20, 200);
        panel.add(label7);

        moduleField = new JTextField();
        moduleField.setSize(600, 25);
        moduleField.setLocation(140, 200);
        moduleField.setText(module);
        panel.add(moduleField);

        JButton connButton = new JButton();
        connButton.setText("读取表");
        connButton.setSize(100, 30);
        connButton.setLocation(350, 235);
        connButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                // 清空表格
                tableModel.setRowCount(0);
                readTables();
            }
        });
        panel.add(connButton);

        // 初始化表头
        String[] headers = {"", "表名", "表类型", "注释名"};
        tableModel = new DefaultTableModel(null, headers) {
            // 设置第一列能修改，其余列不能修改
            public boolean isCellEditable(int row, int column) {
                if (column > 0) {
                    return false;
                }
                return true;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                Object value = getValueAt(0, c);
                if (value != null) {
                    return value.getClass();
                } else {
                    return super.getClass();
                }
            }
        };
        jtable = new JTable(tableModel);

        // 设置第一列不能改变宽度
        TableColumn firsetColumn = jtable.getColumnModel().getColumn(0);
        firsetColumn.setPreferredWidth(30);
        firsetColumn.setMaxWidth(30);
        firsetColumn.setMinWidth(30);
        // 表头第一列加入checkbox
        final MyCheckBoxRenderer check = new MyCheckBoxRenderer();
        firsetColumn.setHeaderRenderer(check);
        // 表头第一列checkbox添加全选事件
        jtable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (jtable.getColumnModel().getColumnIndexAtX(e.getX()) == 0) {
                    boolean b = !check.isSelected();
                    check.setSelected(b);
                    jtable.getTableHeader().repaint();
                    for (int i = 0; i < jtable.getRowCount(); i++) {
                        jtable.getModel().setValueAt(b, i, 0);// 把这一列都设成和表头一样
                    }
                }
            }
        });

        // 滚动面板
        JScrollPane scrollPane = new JScrollPane(jtable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setSize(750, 300);
        scrollPane.setLocation(20, 280);
        panel.add(scrollPane);

        JButton autoButton = new JButton();
        autoButton.setText("生成");
        autoButton.setSize(100, 30);
        autoButton.setLocation(350, 590);
        autoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int rowcount = tableModel.getRowCount();
                for (int i = 0; i < rowcount; i++) {
                    Boolean checked = (Boolean) tableModel.getValueAt(i, 0);
                    if (checked) {
                        String tableName = (String) tableModel.getValueAt(i, 1);
                        System.out.println("选择了表：" + tableName);
                        readColumns(tableName.toLowerCase());
                    }
                }
            }
        });
        panel.add(autoButton);

        this.add(panel);
    }

    /**
     * 读取数据库参数配置文件
     */
    public void readJdbcProperties() {
        try {
            Properties properties = new Properties();
            InputStream is = this.getClass().getResourceAsStream(jdbcPropertiesFilePath);
            properties.load(is);
            drivername = properties.getProperty("jdbc.driverClass");
            username = properties.getProperty("jdbc.username");
            password = properties.getProperty("jdbc.password");
            url = properties.getProperty("jdbc.jdbcUrl");
            dbname = url.substring(url.lastIndexOf("/") + 1, url.indexOf("?"));
            driverNameField.setText(drivername);
            urlField.setText(url);
            usernameField.setText(username);
            passwordField.setText(password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取数据库中的所有表和视图
     */
    public void readTables() {
        try {
            drivername = driverNameField.getText();
            username = usernameField.getText();
            password = passwordField.getText();
            url = urlField.getText();
            dbname = url.substring(url.lastIndexOf("/") + 1, url.indexOf("?"));
            DBConnect db = new DBConnect(drivername, url, username, password);
            db.opendb();
            ResultSet rs = db.executeQuerySql("select table_name,table_type,table_comment from information_schema.tables where TABLE_SCHEMA='" + dbname + "' order by table_type,table_name");
            while (rs.next()) {
                // System.out.println(rs.getString("table_name"));
                Object[] data = new Object[4];
                data[0] = false;
                data[1] = rs.getString("table_name");
                data[2] = rs.getString("table_type");
                data[3] = rs.getString("table_comment");
                tableModel.addRow(data);
            }
            jtable.invalidate();
            rs.close();
            db.closedb();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "出错了", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 读取表结构, 自动生成基础类
     *
     * @param tableName
     */
    public void readColumns(String tableName) {
        try {
            DBConnect db = new DBConnect(drivername, url, username, password);
            db.opendb();
            ResultSet rs = db.executeQuerySql("select * from " + tableName);
            ResultSetMetaData rsmd = rs.getMetaData();
            Map<String, Column> colMap = new LinkedHashMap<String, Column>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String columnName = rsmd.getColumnName(i);
                if (columnName.equalsIgnoreCase("id")) {
                    continue;
                }
                Integer columnType = rsmd.getColumnType(i);
                Column col = new Column(columnName, columnType);
                colMap.put(columnName, col);
            }
            rs = db.executeQuerySql("show full columns from " + tableName);
            while (rs.next()) {
                String columnName = rs.getString("Field");
                Column col = colMap.get(columnName);
                if (col != null) {
                    col.setComments(rs.getString("Comment"));
                }
            }

            List<Column> colList = new ArrayList<Column>();
            colList.addAll(colMap.values());

            rs.close();
            db.closedb();

            // 解析表名，从user_login_log变为UserLoginLog
            String[] tableNameArray = tableName.split("_");
            String fileName = "";
            if (tableNameArray.length > 1) {
                for (int i = 0; i < tableNameArray.length; i++) {
                    fileName += tableNameArray[i].substring(0, 1).toUpperCase() + tableNameArray[i].substring(1, tableNameArray[i].length());
                }
            } else {
                fileName += tableNameArray[0].substring(0, 1).toUpperCase() + tableNameArray[0].substring(1, tableNameArray[0].length());
            }

            // 判断文件路径
            filePath = filePathField.getText().trim();
            basePackage = packageField.getText().trim();
            module = moduleField.getText().trim();
            if (filePath.length() == 0) {
                JOptionPane.showMessageDialog(null, "请填写需要生成文件所在的路径", "出错了", JOptionPane.ERROR_MESSAGE);
            } else if (basePackage.length() == 0) {
                JOptionPane.showMessageDialog(null, "请填写需要生成文件所在的模块名称", "出错了", JOptionPane.ERROR_MESSAGE);
            } else {
                String sqlXmlPath = filePath + "/" + repositoryPrefix + "-" + basePackage + "/" + basePackage + "-service-impl/src/main/resources/com/"+repositoryPrefix+"/" + basePackage + "/service/dao/impl/mapper/";
                createMapperXML(fileName, tableName, sqlXmlPath, colList);

                String mapperJavaPath = filePath + "/"+repositoryPrefix+"-" + basePackage + "/" + basePackage + "-service-impl/src/main/java/com/"+repositoryPrefix+"/" + basePackage + "/service/dao/impl/mapper/";
                createMapperJava(fileName, mapperJavaPath, colList);

                String daoInterfacePath = filePath + "/"+repositoryPrefix+"-" + basePackage + "/" + basePackage + "-service-impl/src/main/java/com/"+repositoryPrefix+"/" + basePackage + "/service/dao/api/";
                createDaoInterfaceJava(fileName, daoInterfacePath, colList);

                String daoImplementsPath = filePath + "/"+repositoryPrefix+"-" + basePackage + "/" + basePackage + "-service-impl/src/main/java/com/"+repositoryPrefix+"/" + basePackage + "/service/dao/impl/";
                createDaoImplementsJava(fileName, daoImplementsPath, colList);

                String entityPath = filePath + "/"+repositoryPrefix+"-" + basePackage + "/" + basePackage + "-service-impl/src/main/java/com/"+repositoryPrefix+"/" + basePackage + "/service/dao/entity/";
                createEntityJava(fileName, entityPath, colList);

                String serviceInterfacePath = filePath + "/"+repositoryPrefix+"-" + basePackage + "/" + basePackage + "-service-api/src/main/java/com/"+repositoryPrefix+"/" + basePackage + "/service/api/";
                if (module.length() != 0) {
                    serviceInterfacePath += module + "/";
                }
                createServiceInterfaceJava(fileName, serviceInterfacePath, colList);

                String serviceImplementsPath = filePath + "/"+repositoryPrefix+"-" + basePackage + "/" + basePackage + "-service-impl/src/main/java/com/"+repositoryPrefix+"/" + basePackage + "/service/impl/";
                if (module.length() != 0) {
                    serviceImplementsPath += module + "/";
                }
                createServiceImplJava(fileName, serviceImplementsPath, colList);

            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "出错了", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 生成Mapper.xml
     */
    public void createMapperXML(String fileName, String tableName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = fileName + "Mapper.xml";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);
        fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        fw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n");
        fw.write("<mapper namespace=\"com."+repositoryPrefix+"." + basePackage + ".service.dao.impl.mapper." + fileName + "Mapper\">\n");
        fw.write("\n");
        fw.write("    <!-- 以下为人工智能生成的mapper基础方法 -->\n");

        //生成表名引用
        fw.write("    <sql id=\"tableName\">\n");
        fw.write("        " + basePackage + "." + tableName + "\n");
        fw.write("    </sql>\n");
        fw.write("\n");

        //生成字段映射
        fw.write("    <!-- BaseResultMap for table columns  -->\n");
        fw.write("    <resultMap id=\"BaseResultMap\" type=\"com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity\">\n");
        fw.write("        <id column=\"id\" property=\"id\" jdbcType=\"BIGINT\"/>\n");
        fw.write(this.parseXmlBaseResultMap(colList));
        fw.write("    </resultMap>\n");
        fw.write("\n");

        //生成查询字段
        fw.write("    <!-- Base_Column_List -->\n");
        fw.write("    <sql id=\"Base_Column_List\">\n");
        fw.write("        " + this.parseXmlBaseColumnList(colList) + "\n");
        fw.write("    </sql>\n");
        fw.write("\n");

        //生成insert方法
        fw.write("    <!-- insert -->\n");
        fw.write("    <insert id=\"insert\" parameterType=\"com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity\" keyProperty=\"id\" useGeneratedKeys=\"true\">\n");
        fw.write("        insert into\n");
        fw.write("            <include refid=\"tableName\"/>\n");
        fw.write("        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >\n");
        fw.write(parseXmlInsert(colList));
        fw.write("        </trim>\n");
        fw.write("        <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >\n");
        fw.write(parseXmlInsertValue(colList));
        fw.write("        </trim>\n");
        fw.write("    </insert>\n");
        fw.write("\n");

        //生成update方法
        fw.write("    <!-- update -->\n");
        fw.write("    <update id=\"update\" parameterType=\"com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity\">\n");
        fw.write("        update\n");
        fw.write("            <include refid=\"tableName\"/>\n");
        fw.write("        <set>\n");
        fw.write(parseXmlUpdate(colList));
        fw.write("        </set>\n");
        fw.write("        where id = #{id, jdbcType=BIGINT}\n");
        fw.write("    </update>\n");
        fw.write("\n");

        //生成findById方法
        fw.write("    <!-- findById -->\n");
        fw.write("    <select id=\"findById\" resultMap=\"BaseResultMap\" parameterType=\"java.lang.Integer\">\n");
        fw.write("        select\n");
        fw.write("            <include refid=\"Base_Column_List\"/>\n");
        fw.write("        from\n");
        fw.write("            <include refid=\"tableName\"/>\n");
        fw.write("        where id = #{id, jdbcType=BIGINT}\n");
        fw.write("    </select>\n");
        fw.write("\n");

        //生成findByIds方法
        fw.write("    <!-- findByIds -->\n");
        fw.write("    <select id=\"findByIds\" resultMap=\"BaseResultMap\" parameterType=\"java.util.List\">\n");
        fw.write("        select\n");
        fw.write("            <include refid=\"Base_Column_List\"/>\n");
        fw.write("        from\n");
        fw.write("            <include refid=\"tableName\"/>\n");
        fw.write("        where id in\n");
        fw.write("        <foreach item=\"item\" index=\"index\" collection=\"list\" open=\"(\" separator=\",\" close=\")\">\n");
        fw.write("            #{item, jdbcType=BIGINT}\n");
        fw.write("        </foreach>\n");
        fw.write("    </select>\n");

        fw.write("    <!-- 以上为人工智能生成的mapper基础方法 -->\n");
        fw.write("\n");
        fw.write("</mapper>");
        fw.flush();
        fw.close();
    }

    /**
     * 生成Mapper.java
     */
    public void createMapperJava(String fileName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = fileName + "Mapper.java";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);

        fw.write("package com."+repositoryPrefix+"." + basePackage + ".service.dao.impl.mapper;\n");
        fw.write("\n");
        fw.write("import com."+repositoryPrefix+".base.dao.mapper.BaseMogoMapper;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity;\n");
        fw.write("import java.util.List;\n");
        fw.write("\n");
        fw.write("/**\n");
        fw.write(" * " + fileName + "Mapper\n");
        fw.write(" *\n");
        fw.write(" * @author 人工智能\n");
        fw.write(" * @date " + this.sdf.format(new Date()) + "\n");
        fw.write(" */\n");
        fw.write("public interface " + fileName + "Mapper extends BaseMogoMapper<" + fileName + "Entity> {\n");
        fw.write("\n");
        fw.write("}\n");
        fw.flush();
        fw.close();
    }

    /**
     * 生成DAO Interface.java
     */
    public void createDaoInterfaceJava(String fileName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = "I" + fileName + "DAO.java";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);

        fw.write("package com."+repositoryPrefix+"." + basePackage + ".service.dao.api;\n");
        fw.write("\n");
        fw.write("import com."+repositoryPrefix+".base.dao.api.BaseMogoDAO;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity;\n");
        fw.write("import java.util.List;\n");
        fw.write("\n");
        fw.write("/**\n");
        fw.write(" * I" + fileName + "DAO\n");
        fw.write(" *\n");
        fw.write(" * @author 人工智能\n");
        fw.write(" * @date " + this.sdf.format(new Date()) + "\n");
        fw.write(" */\n");
        fw.write("public interface I" + fileName + "DAO extends BaseMogoDAO<" + fileName + "Entity> {\n");
        fw.write("\n");
        fw.write("}\n");
        fw.flush();
        fw.close();
    }

    /**
     * 生成DAO Implements.java
     */
    public void createDaoImplementsJava(String fileName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = fileName + "DAOImpl.java";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);

        fw.write("package com."+repositoryPrefix+"." + basePackage + ".service.dao.impl;\n");
        fw.write("\n");
        fw.write("import com."+repositoryPrefix+".base.dao.impl.BaseMogoDAOImpl;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.api.I" + fileName + "DAO;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.impl.mapper." + fileName + "Mapper;\n");
        fw.write("import org.springframework.beans.factory.annotation.Autowired;\n");
        fw.write("import org.springframework.stereotype.Service;\n");
        fw.write("import java.util.List;\n");
        fw.write("\n");
        fw.write("/**\n");
        fw.write(" * " + fileName + "Impl\n");
        fw.write(" *\n");
        fw.write(" * @author 人工智能\n");
        fw.write(" * @date " + this.sdf.format(new Date()) + "\n");
        fw.write(" */\n");
        fw.write("@Service\n");
        fw.write("public class " + fileName + "DAOImpl extends BaseMogoDAOImpl<" + fileName + "Mapper, " + fileName + "Entity> implements I" + fileName + "DAO {\n");
        fw.write("\n");
        fw.write("    @Autowired\n");
        fw.write("    @Override\n");
        fw.write("    public void setEntityMapper(" + fileName + "Mapper " + this.varName(fileName) + "Mapper) {\n");
        fw.write("        entityMapper = " + this.varName(fileName) + "Mapper;\n");
        fw.write("    }\n");
        fw.write("}\n");
        fw.flush();
        fw.close();
    }

    /**
     * 生成Entity.java
     */
    public void createEntityJava(String fileName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = fileName + "Entity.java";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);

        fw.write("package com."+repositoryPrefix+"." + basePackage + ".service.dao.entity;\n");
        fw.write("\n");
        fw.write("import com."+repositoryPrefix+".base.dao.entity.BaseMogoEntity;\n");
        fw.write("import java.util.Date;\n");
        fw.write("import java.math.BigDecimal;\n");
        fw.write("\n");
        fw.write("/**\n");
        fw.write(" * " + fileName + "Entity\n");
        fw.write(" *\n");
        fw.write(" * @author 人工智能\n");
        fw.write(" * @date " + this.sdf.format(new Date()) + "\n");
        fw.write(" */\n");
        fw.write("public class " + fileName + "Entity extends BaseMogoEntity {\n");
        fw.write("\n");
        fw.write("    /** 序列化ID */\n");
        fw.write("    private static final long serialVersionUID = " + this.serialVersionUID() + "L;\n");
        fw.write("\n");
        fw.write(parseJavaField(colList));
        fw.write(parseJavaGetSet(colList));
        fw.write("}\n");
        fw.flush();
        fw.close();
    }

    /**
     * 生成Service Interface.java
     */
    public void createServiceInterfaceJava(String fileName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = "I" + fileName + "Service.java";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);

        fw.write("package com."+repositoryPrefix+"." + basePackage + ".service.api" + (module.length() == 0 ? "" : ("." + module)) + ";\n");
        fw.write("\n");
        fw.write("import com."+repositoryPrefix+".base.dao.api.BaseMogoDAO;\n");
        fw.write("import java.util.List;\n");
        fw.write("\n");
        fw.write("/**\n");
        fw.write(" * I" + fileName + "Service\n");
        fw.write(" *\n");
        fw.write(" * @author 人工智能\n");
        fw.write(" * @date " + this.sdf.format(new Date()) + "\n");
        fw.write(" */\n");
        fw.write("public interface I" + fileName + "Service {\n");
        fw.write("\n");
        fw.write("}\n");
        fw.flush();
        fw.close();
    }

    /**
     * 生成Service Implements.java
     */
    public void createServiceImplJava(String fileName, String path, List<Column> colList) throws Exception {
        File xmlFile = new File(path);
        if (!xmlFile.exists()) {
            xmlFile.mkdirs();
        }
        String xmlfileName = fileName + "ServiceImpl.java";
        xmlFile = new File(path + xmlfileName);
        System.out.println("开始生成：" + xmlFile.getAbsolutePath());
        FileWriter fw = new FileWriter(xmlFile);

        fw.write("package com."+repositoryPrefix+"." + basePackage + ".service.impl" + (module.length() == 0 ? "" : ("." + module)) + ";\n");
        fw.write("\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.api" + (module.length() == 0 ? "" : ("." + module)) + ".I" + fileName + "Service;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.api.I" + fileName + "DAO;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.entity." + fileName + "Entity;\n");
        fw.write("import com."+repositoryPrefix+"." + basePackage + ".service.dao.impl.mapper." + fileName + "Mapper;\n");
        fw.write("import org.springframework.beans.factory.annotation.Autowired;\n");
        fw.write("import org.springframework.stereotype.Service;\n");
        fw.write("import java.util.List;\n");
        fw.write("\n");
        fw.write("/**\n");
        fw.write(" * " + fileName + "ServiceImpl\n");
        fw.write(" *\n");
        fw.write(" * @author 人工智能\n");
        fw.write(" * @date " + this.sdf.format(new Date()) + "\n");
        fw.write(" */\n");
        fw.write("@Service\n");
        fw.write("public class " + fileName + "ServiceImpl implements I" + fileName + "Service {\n");
        fw.write("\n");
        fw.write("    @Autowired\n");
        fw.write("    private I" + fileName + "DAO " + varName(fileName) + "DAO;\n");
        fw.write("\n");
        fw.write("}\n");
        fw.flush();
        fw.close();
    }

    private String varName(String className) {
        return className.substring(0, 1).toLowerCase() + className.substring(1, className.length());
    }

    private String serialVersionUID() {
        Random random = new Random();
        Long longNumber = random.nextLong();
        String longStr = longNumber + "";
        longStr = longStr.replace("-", "");
        return longStr + "00000000000000000000000000".substring(0, 19 - longStr.length());
    }

    private String parseXmlBaseResultMap(List<Column> colList) {
        String getString = "";
        for (Column col : colList) {
            String colName = col.getName();
            Integer colType = col.getType();
            getString += "        <result column=\"" + colName + "\" property=\"" + colName + "\" jdbcType=\"" + this.convertSqlType(colType) + "\"/>\n";
        }
        return getString;
    }

    private String parseXmlBaseColumnList(List<Column> colList) {
        String getString = "id, ";
        for (Column col : colList) {
            String colName = col.getName();
            getString += colName + ", ";
        }
        getString = getString.trim();
        getString = getString.substring(0, getString.length() - 1);
        return getString;
    }

    private String parseXmlInsert(List<Column> colList) {
        String insertString = "";
        insertString += "            <if test=\"id != null\" >\n";
        insertString += "                id,\n";
        insertString += "            </if>\n";
        for (Column col : colList) {
            String colName = col.getName();
            insertString += "            <if test=\"" + colName + " != null\" >\n";
            insertString += "                " + colName + ",\n";
            insertString += "            </if>\n";
        }
        return insertString;
    }

    private String parseXmlInsertValue(List<Column> colList) {
        String insertValueString = "";
        insertValueString += "            <if test=\"id != null\" >\n";
        insertValueString += "                #{id, jdbcType=BIGINT},\n";
        insertValueString += "            </if>\n";
        for (Column col : colList) {
            String colName = col.getName();
            Integer colType = col.getType();
            insertValueString += "            <if test=\"" + colName + " != null\" >\n";
            insertValueString += "                #{" + colName + ", jdbcType=" + this.convertSqlType(colType) + "},\n";
            insertValueString += "            </if>\n";
        }
        return insertValueString;
    }

    private String parseXmlUpdate(List<Column> colList) {
        String updateString = "";
        for (Column col : colList) {
            String colName = col.getName();
            if (colName.equalsIgnoreCase("createTime") || colName.equalsIgnoreCase("createBy") || colName.equalsIgnoreCase("createByType")) {
                continue;
            }
            Integer colType = col.getType();
            updateString += "            <if test=\"" + colName + " != null\" >\n";
            updateString += "                " + colName + " = #{" + colName + ", jdbcType=" + this.convertSqlType(colType) + "},\n";
            updateString += "            </if>\n";
        }
        return updateString;
    }

    private String parseJavaField(List<Column> colList) {
        String fieldString = "";
        for (Column col : colList) {
            String colName = col.getName();
            Integer colType = col.getType();
            String comments = col.getComments();
            fieldString += "    \n";
            fieldString += "    /** " + comments + " **/\n";
            fieldString += "    private " + convertType(colType) + " " + colName + ";\n";
        }
        return fieldString;
    }

    private String parseJavaGetSet(List<Column> colList) {
        String getsetString = "";
        getsetString += "    \n";
        for (Column col : colList) {
            String colName = col.getName();
            Integer colType = col.getType();
            String comments = col.getComments();
            getsetString += "    \n";
            getsetString += "    /** 获取属性值：" + comments + " **/\n";
            getsetString += "    public " + convertType(colType) + " get" + colName.substring(0, 1).toUpperCase() + colName.substring(1, colName.length()) + "() {\n";
            getsetString += "        return " + colName + ";\n";
            getsetString += "    }\n";
            getsetString += "    \n";
            getsetString += "    /** 设置属性值：" + comments + " **/\n";
            getsetString += "    public void set" + colName.substring(0, 1).toUpperCase() + colName.substring(1, colName.length()) + "(" + convertType(colType) + " " + colName + ") {\n";
            getsetString += "        this." + colName + " = " + colName + ";\n";
            getsetString += "    }\n";
        }
        return getsetString;
    }

    private String convertType(Integer sqlType) {
        switch (sqlType) {
            case java.sql.Types.VARCHAR:
            case java.sql.Types.CHAR:
                return "String";
            case java.sql.Types.BLOB:
            case java.sql.Types.BINARY:
                return "byte[]";
            case java.sql.Types.BIT:
            case java.sql.Types.TINYINT:
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER:
                return "Integer";
            case Types.BIGINT:
                return "Long";
            case java.sql.Types.DECIMAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
                return "BigDecimal";
            case java.sql.Types.DATE:
            case java.sql.Types.TIMESTAMP:
                return "Date";
        }
        return null;
    }

    private String convertSqlType(Integer sqlType) {
        switch (sqlType) {
            case java.sql.Types.VARCHAR:
                return "VARCHAR";
            case java.sql.Types.CHAR:
                return "CHAR";
            case java.sql.Types.BLOB:
                return "BLOB";
            case java.sql.Types.BINARY:
                return "BINARY";
            case java.sql.Types.BIT:
                return "BIT";
            case java.sql.Types.TINYINT:
                return "TINYINT";
            case java.sql.Types.SMALLINT:
                return "SMALLINT";
            case java.sql.Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case java.sql.Types.DECIMAL:
                return "DECIMAL";
            case java.sql.Types.DOUBLE:
                return "DOUBLE";
            case java.sql.Types.FLOAT:
                return "FLOAT";
            case java.sql.Types.DATE:
                return "DATE";
            case java.sql.Types.TIMESTAMP:
                return "TIMESTAMP";
        }
        return null;
    }

    class MyCheckBoxRenderer extends JCheckBox implements TableCellRenderer {

        public MyCheckBoxRenderer() {
            this.setBorderPainted(true);
            this.setHorizontalAlignment(this.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // TODO Auto-generated method stub
            return this;
        }
    }

}
