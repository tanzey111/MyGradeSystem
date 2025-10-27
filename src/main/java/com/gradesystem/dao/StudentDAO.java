package com.gradesystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDAO {
    /**
     * 获取所有学生
     */
    public List<Map<String, Object>> getAllStudents() throws SQLException {
        List<Map<String, Object>> students = new ArrayList<>();
        String sql = "SELECT id, name, class, email, phone, status, created_at FROM students ORDER BY id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("id", rs.getString("id"));
                student.put("name", rs.getString("name"));
                student.put("class", rs.getString("class"));
                student.put("email", rs.getString("email"));
                student.put("phone", rs.getString("phone"));
                student.put("status", rs.getString("status"));
                student.put("createdAt", rs.getTimestamp("created_at"));
                students.add(student);
            }
        }
        return students;
    }

    /**
     * 根据ID获取学生
     */
    public Map<String, Object> getStudentById(String studentId) throws SQLException {
        String sql = "SELECT id, name, class, email, phone, status, created_at FROM students WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("id", rs.getString("id"));
                student.put("name", rs.getString("name"));
                student.put("class", rs.getString("class"));
                student.put("email", rs.getString("email"));
                student.put("phone", rs.getString("phone"));
                student.put("status", rs.getString("status"));
                student.put("createdAt", rs.getTimestamp("created_at"));
                return student;
            }
        }
        return null;
    }

    /**
     * 添加学生
     */
    public boolean addStudent(Map<String, String> studentData) throws SQLException {
        String sql = "INSERT INTO students (id, name, class, password, email, phone) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentData.get("id"));
            pstmt.setString(2, studentData.get("name"));
            pstmt.setString(3, studentData.get("class"));
            pstmt.setString(4, MD5Util.md5(studentData.get("password"))); // 加密存储
            pstmt.setString(5, studentData.get("email"));
            pstmt.setString(6, studentData.get("phone"));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 更新学生信息
     */
    public boolean updateStudent(String studentId, Map<String, String> studentData) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE students SET ");
        List<Object> params = new ArrayList<>();

        // 动态构建SET子句
        if (studentData.containsKey("name")) {
            sql.append("name = ?, ");
            params.add(studentData.get("name"));
        }
        if (studentData.containsKey("class")) {
            sql.append("class = ?, ");
            params.add(studentData.get("class"));
        }
        if (studentData.containsKey("email")) {
            sql.append("email = ?, ");
            params.add(studentData.get("email"));
        }
        if (studentData.containsKey("phone")) {
            sql.append("phone = ?, ");
            params.add(studentData.get("phone"));
        }
        if (studentData.containsKey("status")) {
            sql.append("status = ?, ");
            params.add(studentData.get("status"));
        }

        // 移除最后一个逗号
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(studentId);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 删除学生
     */
    public boolean deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 检查学生是否存在
     */
    public boolean studentExists(String studentId) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 检查学生是否有成绩记录
     */
    public boolean hasGradeRecords(String studentId) throws SQLException {
        String sql = "SELECT 1 FROM grades WHERE student_id = ? LIMIT 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 搜索学生
     */
    public List<Map<String, Object>> searchStudents(String keyword) throws SQLException {
        List<Map<String, Object>> students = new ArrayList<>();
        String sql = "SELECT id, name, class, email, phone, status FROM students " +
                "WHERE id LIKE ? OR name LIKE ? OR class LIKE ? ORDER BY id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> student = new HashMap<>();
                student.put("id", rs.getString("id"));
                student.put("name", rs.getString("name"));
                student.put("class", rs.getString("class"));
                student.put("email", rs.getString("email"));
                student.put("phone", rs.getString("phone"));
                student.put("status", rs.getString("status"));
                students.add(student);
            }
        }
        return students;
    }
    /**
     * 更新学生密码
     */
    public boolean updateStudentPassword(String studentId, String newPassword) throws SQLException {
        String sql = "UPDATE students SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, MD5Util.md5(newPassword)); // 加密存储
            pstmt.setString(2, studentId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 验证学生密码
     */
    public boolean verifyStudentPassword(String studentId, String password) throws SQLException {
        String sql = "SELECT password FROM students WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return MD5Util.verify(password, storedPassword);
            }
            return false;
        }
    }

    /**
     * 重置学生密码
     */
    public boolean resetStudentPassword(String studentId, String newPassword) throws SQLException {
        return updateStudentPassword(studentId, newPassword);
    }
}
