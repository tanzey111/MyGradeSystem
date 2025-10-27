package com.gradesystem.service;

import com.gradesystem.dao.DatabaseUtil;
import com.gradesystem.dao.MD5Util;  // 添加导入

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    /**
     * 用户认证
     * @param username 用户名/学号/工号
     * @param password 密码
     * @param role 角色 (student/teacher/admin)
     * @return 用户信息，认证失败返回null
     */
    public Map<String, Object> authenticate(String username, String password, String role) {
        Map<String, Object> userInfo = null;

        try {
            // 对密码进行MD5加密
            String encryptedPassword = MD5Util.md5(password);

            switch (role) {
                case "student":
                    userInfo = authenticateStudent(username, encryptedPassword);
                    break;
                case "teacher":
                    userInfo = authenticateTeacher(username, encryptedPassword);
                    break;
                case "admin":
                    userInfo = authenticateAdmin(username, encryptedPassword);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    /**
     * 学生认证
     */
    private Map<String, Object> authenticateStudent(String studentId, String encryptedPassword) throws SQLException {
        String sql = "SELECT id, name, class FROM students WHERE id = ? AND password = ? AND status = 'active'";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, encryptedPassword); // 使用加密后的密码

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", rs.getString("id"));
                userInfo.put("name", rs.getString("name"));
                userInfo.put("class", rs.getString("class"));
                userInfo.put("role", "student");
                return userInfo;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 教师认证
     */
    private Map<String, Object> authenticateTeacher(String teacherId, String encryptedPassword) throws SQLException {
        String sql = "SELECT id, name, department FROM teachers WHERE id = ? AND password = ? AND status = 'active'";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, encryptedPassword); // 使用加密后的密码

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", rs.getString("id"));
                userInfo.put("name", rs.getString("name"));
                userInfo.put("department", rs.getString("department"));
                userInfo.put("role", "teacher");
                return userInfo;
            }
        }
        return null;
    }

    /**
     * 管理员认证
     */
    private Map<String, Object> authenticateAdmin(String adminId, String encryptedPassword) throws SQLException {
        String sql = "SELECT id, name, email FROM admins WHERE id = ? AND password = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminId);
            pstmt.setString(2, encryptedPassword); // 使用加密后的密码

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", rs.getString("id"));
                userInfo.put("name", rs.getString("name"));
                userInfo.put("email", rs.getString("email"));
                userInfo.put("role", "admin");
                return userInfo;
            }
        }
        return null;
    }
}