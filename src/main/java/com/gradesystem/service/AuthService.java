package com.gradesystem.service;

import com.gradesystem.dao.DatabaseUtil;

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
     * @param role 角色 (student/teacher)
     * @return 用户信息，认证失败返回null
     */
    public Map<String, Object> authenticate(String username, String password, String role) {
        Map<String, Object> userInfo = null;

        try {
            switch (role) {
                case "student":
                    userInfo = authenticateStudent(username, password);
                    break;
                case "teacher":
                    userInfo = authenticateTeacher(username, password);
                    break;
                case "admin":
                    userInfo = authenticateAdmin(username, password);
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
    private Map<String, Object> authenticateStudent(String studentId, String password) throws SQLException {
        String sql = "SELECT id, name, class FROM students WHERE id = ? AND password = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, password); // 实际应该使用加密密码

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
    private Map<String, Object> authenticateTeacher(String teacherId, String password) throws SQLException {
        String sql = "SELECT id, name FROM teachers WHERE id = ? AND password = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, password); // 实际应该使用加密密码

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", rs.getString("id"));
                userInfo.put("name", rs.getString("name"));
                userInfo.put("role", "teacher");
                return userInfo;
            }
        }
        return null;
    }
    /**
     * 管理员认证
     */
    private Map<String, Object> authenticateAdmin(String adminId, String password) throws SQLException {
        String sql = "SELECT id, name, email FROM admins WHERE id = ? AND password = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminId);
            pstmt.setString(2, password);

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
