package com.gradesystem.dao;

import java.sql.*;
import java.util.Properties;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:mysql://mygradesystem-db.c5nbssvvaang.us-east-1.rds.amazonaws.com:3306/MyGradeSystem?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    private static final String DB_USERNAME = "main";
    private static final String DB_PASSWORD = "MyRDS!Pwd2025";

//    private static final String DB_URL = "jdbc:mysql://localhost:3306/grade_system?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
//    private static final String DB_USERNAME = "root";
//    private static final String DB_PASSWORD = "password";

    // 静态代码块，加载数据库驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC驱动加载失败: " + e.getMessage());
            throw new RuntimeException("数据库驱动加载失败", e);
        }
    }

    //获取数据库连接
    public static Connection getConnection() throws SQLException {
        try {
            Properties props = new Properties();
            props.setProperty("user", DB_USERNAME);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("useSSL", "false");
            props.setProperty("autoReconnect", "true");

            Connection conn = DriverManager.getConnection(DB_URL, props);
            System.out.println("数据库连接成功");
            return conn;
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试数据库连接
     * @return boolean 连接是否成功
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 安全关闭连接
     * @param conn 数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("数据库连接已关闭");
            } catch (SQLException e) {
                System.err.println("关闭数据库连接时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 安全关闭Statement
     * @param stmt Statement对象
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("关闭Statement时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 安全关闭ResultSet
     * @param rs ResultSet对象
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("关闭ResultSet时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 关闭所有数据库资源
     * @param conn 连接
     * @param stmt Statement
     * @param rs ResultSet
     */
    public static void closeAll(Connection conn, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

    /**
     * 初始化数据库表结构
     * 用于第一次运行系统时创建必要的表
     */
    public static void initializeDatabase() {
        String[] createTables = {
                // 学生表
                "CREATE TABLE IF NOT EXISTS students (" +
                        "  id VARCHAR(20) PRIMARY KEY," +
                        "  name VARCHAR(50) NOT NULL," +
                        "  class VARCHAR(50) NOT NULL," +
                        "  password VARCHAR(100) NOT NULL," +
                        "  email VARCHAR(100)," +
                        "  phone VARCHAR(20)," +
                        "  status ENUM('active', 'inactive') DEFAULT 'active'," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 教师表
                "CREATE TABLE IF NOT EXISTS teachers (" +
                        "  id VARCHAR(20) PRIMARY KEY," +
                        "  name VARCHAR(50) NOT NULL," +
                        "  password VARCHAR(100) NOT NULL," +
                        "  email VARCHAR(100)," +
                        "  department VARCHAR(100)," +
                        "  status ENUM('active', 'inactive') DEFAULT 'active'," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 管理员表
                "CREATE TABLE IF NOT EXISTS admins (" +
                        "  id VARCHAR(20) PRIMARY KEY," +
                        "  name VARCHAR(50) NOT NULL," +
                        "  password VARCHAR(100) NOT NULL," +
                        "  email VARCHAR(100)," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                //课程表
                "CREATE TABLE IF NOT EXISTS courses (" +
                        "  id INT AUTO_INCREMENT PRIMARY KEY," +
                        "  course_name VARCHAR(100) NOT NULL," +
                        "  credit INT NOT NULL," +
                        "  semester VARCHAR(20) NOT NULL," +
                        "  description TEXT," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  UNIQUE KEY unique_course_semester (course_name, semester)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 成绩表
                "CREATE TABLE IF NOT EXISTS grades (" +
                        "  id INT AUTO_INCREMENT PRIMARY KEY," +
                        "  student_id VARCHAR(20) NOT NULL," +
                        "  student_name VARCHAR(50) NOT NULL," +  // 新增学生姓名列
                        "  course_name VARCHAR(100) NOT NULL," +
                        "  score DECIMAL(5,2) NOT NULL," +
                        "  semester VARCHAR(20) NOT NULL," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE," +
                        "  UNIQUE KEY unique_grade (student_id, course_name, semester)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 查询时间段配置表
                "CREATE TABLE IF NOT EXISTS query_periods (" +
                        "  id INT PRIMARY KEY DEFAULT 1," +
                        "  start_time BIGINT," +
                        "  end_time BIGINT," +
                        "  is_active BOOLEAN DEFAULT false," +
                        "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 教师课程关联表（增加学期字段）
                "CREATE TABLE IF NOT EXISTS teacher_courses (" +
                        "  id INT AUTO_INCREMENT PRIMARY KEY," +
                        "  teacher_id VARCHAR(20) NOT NULL," +
                        "  course_name VARCHAR(100) NOT NULL," +
                        "  semester VARCHAR(20) NOT NULL," +             // 具体学期
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE," +
                        "  UNIQUE KEY unique_teacher_course_semester (teacher_id, course_name, semester)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 学生选课表（记录学生选了哪个老师的哪门课）
                "CREATE TABLE IF NOT EXISTS student_courses (" +
                        "  id INT AUTO_INCREMENT PRIMARY KEY," +
                        "  student_id VARCHAR(20) NOT NULL," +
                        "  teacher_id VARCHAR(20) NOT NULL," +           // 具体教师
                        "  course_name VARCHAR(100) NOT NULL," +
                        "  semester VARCHAR(20) NOT NULL," +
                        "  status ENUM('selected', 'completed', 'dropped') DEFAULT 'selected'," +
                        "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "  FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE," +
                        "  FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE," +
                        "  UNIQUE KEY unique_student_teacher_course (student_id, teacher_id, course_name, semester)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",

                // 创建索引提高查询性能
                "CREATE INDEX IF NOT EXISTS idx_grades_student_id ON grades(student_id)",
                "CREATE INDEX IF NOT EXISTS idx_grades_course ON grades(course_name)",
                "CREATE INDEX IF NOT EXISTS idx_grades_semester ON grades(semester)"
        };

        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // 执行所有建表语句
            for (String sql : createTables) {
                try {
                    stmt.executeUpdate(sql);
                    System.out.println("执行SQL成功: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                } catch (SQLException e) {
                    System.err.println("执行SQL失败: " + e.getMessage());
                    System.err.println("SQL: " + sql);
                }
            }

            // 插入默认管理员账户（如果不存在）
            String insertAdmin = "INSERT IGNORE INTO admins (id, name, password, email) VALUES ('admin', '系统管理员', '" + MD5Util.md5("admin123") + "', 'admin@school.edu')";
            stmt.executeUpdate(insertAdmin);
            System.out.println("默认管理员账户已创建: admin/admin123");

            // 插入示例学生数据（如果不存在）
            String insertSampleStudents =
                    "INSERT IGNORE INTO students (id, name, class, password) VALUES " +
                            "('2024001', '张三', '计算机科学与技术1班',  '" + MD5Util.md5("123456") + "')," +
                            "('2024002', '李四', '计算机科学与技术1班','" + MD5Util.md5("123456") + "')," +
                            "('2024003', '王五', '软件工程2班', '" + MD5Util.md5("123456") + "')," +
                            "('2024004', '赵六', '软件工程2班','" + MD5Util.md5("123456") + "')," +
                            "('2024005', '钱七', '计算机科学与技术1班', '" + MD5Util.md5("123456") + "')," +
                            "('2023001', '孙八', '计算机科学与技术2班', '" + MD5Util.md5("123456") + "')," +
                            "('2023002', '周九', '软件工程1班', '" + MD5Util.md5("123456") + "')";
            stmt.executeUpdate(insertSampleStudents);

            // 插入示例教师数据（如果不存在）
            String insertSampleTeachers =
                    "INSERT IGNORE INTO teachers (id, name, password, department) VALUES " +
                            "('T1001', '张老师', '" + MD5Util.md5("123456") + "', '计算机学院')," +
                            "('T1002', '李老师', '" + MD5Util.md5("123456") + "', '软件学院')," +
                            "('T1003', '王老师', '" + MD5Util.md5("123456") + "', '计算机学院')," +
                            "('T1004', '赵老师', '" + MD5Util.md5("123456") + "', '软件学院')";
            stmt.executeUpdate(insertSampleTeachers);

            // 插入课程数据
            String insertCourses =
                    "INSERT IGNORE INTO courses (course_name, credit, semester) VALUES " +
                            "('Java程序设计', 3, '2023-2024-2')," +
                            "('数据库原理', 2, '2023-2024-2')," +
                            "('Web开发', 3, '2023-2024-2')," +
                            "('数据结构', 4, '2023-2024-2')," +
                            "('软件工程', 3, '2023-2024-2')," +

                            "('Java程序设计', 3, '2024-2025-1')," +
                            "('数据库原理', 2, '2024-2025-1')," +
                            "('Web开发', 3, '2024-2025-1')," +
                            "('数据结构', 4, '2024-2025-1')," +
                            "('软件工程', 3, '2024-2025-1')," +
                            "('移动应用开发', 2, '2024-2025-1')," +

                            "('Java程序设计', 3, '2024-2025-2')," +
                            "('数据库原理', 2, '2024-2025-2')," +
                            "('数据结构', 4, '2024-2025-2')," +
                            "('软件工程', 3, '2024-2025-2')," +
                            "('Web开发', 3, '2024-2025-2')," +
                            "('算法设计', 3, '2024-2025-2')";
            stmt.executeUpdate(insertCourses);

            // 插入示例教师课程数据（不同学期）
            String insertTeacherCourses =
                    "INSERT IGNORE INTO teacher_courses (teacher_id, course_name, semester) VALUES " +
                            "('T1001', 'Java程序设计', '2023-2024-2')," +
                            "('T1001', '数据库原理', '2023-2024-2')," +
                            "('T1002', 'Web开发', '2023-2024-2')," +
                            "('T1003', '数据结构', '2023-2024-2')," +
                            "('T1004', '软件工程', '2023-2024-2')," +

                            "('T1001', 'Java程序设计', '2024-2025-1')," +
                            "('T1001', '数据库原理', '2024-2025-1')," +
                            "('T1002', 'Web开发', '2024-2025-1')," +
                            "('T1003', '数据结构', '2024-2025-1')," +
                            "('T1004', '软件工程', '2024-2025-1')," +
                            "('T1004', '移动应用开发', '2024-2025-1')," +

                            "('T1002', 'Java程序设计', '2024-2025-2')," +
                            "('T1001', '数据库原理', '2024-2025-2')," +
                            "('T1003', '数据结构', '2024-2025-2')," +
                            "('T1004', '软件工程', '2024-2025-2')," +
                            "('T1002', 'Web开发', '2024-2025-2')," +
                            "('T1003', '算法设计', '2024-2025-2')";
            stmt.executeUpdate(insertTeacherCourses);

            // 插入示例学生选课数据
            String insertStudentCourses =
                    "INSERT IGNORE INTO student_courses (student_id, teacher_id, course_name, semester) VALUES " +

                            "('2023001', 'T1001', 'Java程序设计', '2023-2024-2')," +
                            "('2023001', 'T1001', '数据库原理', '2023-2024-2')," +
                            "('2023002', 'T1002', 'Web开发', '2023-2024-2')," +
                            "('2023002', 'T1003', '数据结构', '2023-2024-2')," +
                            "('2023002', 'T1004', '软件工程', '2023-2024-2')," +

                            "('2024001', 'T1001', 'Java程序设计', '2024-2025-1')," +
                            "('2024001', 'T1001', '数据库原理', '2024-2025-1')," +
                            "('2024002', 'T1001', 'Java程序设计', '2024-2025-1')," +
                            "('2024002', 'T1002', 'Web开发', '2024-2025-1')," +
                            "('2024003', 'T1002', 'Web开发', '2024-2025-1')," +
                            "('2024003', 'T1003', '数据结构', '2024-2025-1')," +
                            "('2024004', 'T1004', '移动应用开发', '2024-2025-1')," +
                            "('2024005', 'T1001', 'Java程序设计', '2024-2025-1')," +

                            "('2024001', 'T1003', '数据结构', '2024-2025-2')," +
                            "('2024003', 'T1003', '算法设计', '2024-2025-2')," +
                            "('2024004', 'T1004', '软件工程', '2024-2025-2')," +
                            "('2024005', 'T1004', '软件工程', '2024-2025-2')";
            stmt.executeUpdate(insertStudentCourses);

            // 插入示例成绩数据（如果不存在）
            String insertSampleGrades =
                    "INSERT IGNORE INTO grades (student_id, student_name, course_name, score, semester) VALUES " +

                            "('2023001', '孙八', 'Java程序设计', 88.5, '2023-2024-2')," +
                            "('2023001', '孙八', '数据库原理', 92.0, '2023-2024-2')," +
                            "('2023002', '周九', 'Web开发', 76.0, '2023-2024-2')," +
                            "('2023002', '周九', '数据结构', 85.5, '2023-2024-2')," +
                            "('2023002', '周九', '软件工程', 79.0, '2023-2024-2')," +

                            "('2024001', '张三', 'Java程序设计', 85.5, '2024-2025-1')," +
                            "('2024001', '张三', '数据库原理', 92.0, '2024-2025-1')," +
                            "('2024002', '李四', 'Java程序设计', 78.0, '2024-2025-1')," +
                            "('2024002', '李四', 'Web开发', 88.5, '2024-2025-1')," +
                            "('2024003', '王五', 'Web开发', 91.0, '2024-2025-1')," +
                            "('2024003', '王五', '数据结构', 79.5, '2024-2025-1')," +
                            "('2024004', '赵六', '移动应用开发', 89.5, '2024-2025-1')," +
                            "('2024005', '钱七', 'Java程序设计', 67.5, '2024-2025-1')," +

                            "('2024001', '张三', '数据结构', 87.0, '2024-2025-2')," +
                            "('2024003', '王五', '算法设计', 88.5, '2024-2025-2')," +
                            "('2024004', '赵六', '软件工程', 90.5, '2024-2025-2')," +
                            "('2024005', '钱七', '软件工程', 75.5, '2024-2025-2')";
            stmt.executeUpdate(insertSampleGrades);
            System.out.println("数据库初始化完成！");

        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeAll(conn, stmt, null);
        }
    }

    /**
     * 执行SQL查询（用于测试和调试）
     * @param sql SQL语句
     * @return 执行是否成功
     */
    public static boolean executeSQL(String sql) {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            boolean result = stmt.execute(sql);
            System.out.println("SQL执行成功: " + sql);
            return result;
        } catch (SQLException e) {
            System.err.println("SQL执行失败: " + e.getMessage());
            System.err.println("SQL: " + sql);
            return false;
        } finally {
            closeAll(conn, stmt, null);
        }
    }

    /**
     * 获取数据库连接信息（用于调试）
     * @return 连接信息字符串
     */
    public static String getConnectionInfo() {
        return String.format("Database URL: %s, User: %s", DB_URL, DB_USERNAME);
    }

    /**
     * 检查表是否存在
     * @param tableName 表名
     * @return 是否存在
     */
    public static boolean tableExists(String tableName) {
        Connection conn = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            rs = conn.getMetaData().getTables(null, null, tableName, null);
            return rs.next();
        } catch (SQLException e) {
            System.err.println("检查表是否存在时出错: " + e.getMessage());
            return false;
        } finally {
            closeAll(conn, null, rs);
        }
    }

    /**
     * 更新现有用户的密码为MD5加密格式
     */
    public static void updatePasswordsToMD5() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();

            // 更新学生密码（只更新未加密的密码）
            String updateStudents = "UPDATE students SET password = MD5(password) WHERE LENGTH(password) < 32";
            int studentCount = stmt.executeUpdate(updateStudents);
            System.out.println("更新了 " + studentCount + " 个学生的密码");

            // 更新教师密码（只更新未加密的密码）
            String updateTeachers = "UPDATE teachers SET password = MD5(password) WHERE LENGTH(password) < 32";
            int teacherCount = stmt.executeUpdate(updateTeachers);
            System.out.println("更新了 " + teacherCount + " 个教师的密码");

            // 更新管理员密码（只更新未加密的密码）
            String updateAdmins = "UPDATE admins SET password = MD5(password) WHERE LENGTH(password) < 32";
            int adminCount = stmt.executeUpdate(updateAdmins);
            System.out.println("更新了 " + adminCount + " 个管理员的密码");

            System.out.println("密码加密更新完成！");

        } catch (SQLException e) {
            System.err.println("更新密码失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeAll(conn, stmt, null);
        }
    }

}