package com.gradesystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherDAO {

    public List<Map<String, Object>> getAllTeachers() throws Exception {
        List<Map<String, Object>> teachers = new ArrayList<>();
        String sql = "SELECT id, name, email, department, status FROM teachers ORDER BY id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> teacher = new HashMap<>();
                teacher.put("id", rs.getString("id"));
                teacher.put("name", rs.getString("name"));
                teacher.put("email", rs.getString("email"));
                teacher.put("department", rs.getString("department"));
                teacher.put("status", rs.getString("status"));
                teachers.add(teacher);
            }
        }
        return teachers;
    }

    public Map<String, Object> getTeacherById(String teacherId) throws Exception {
        String sql = "SELECT id, name, email, department, status FROM teachers WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> teacher = new HashMap<>();
                teacher.put("id", rs.getString("id"));
                teacher.put("name", rs.getString("name"));
                teacher.put("email", rs.getString("email"));
                teacher.put("department", rs.getString("department"));
                teacher.put("status", rs.getString("status"));
                return teacher;
            }
            return null;
        }
    }

    public boolean addTeacher(Map<String, String> teacherData) throws Exception {
        String sql = "INSERT INTO teachers (id, name, email, department, password) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherData.get("id"));
            pstmt.setString(2, teacherData.get("name"));
            pstmt.setString(3, teacherData.get("email"));
            pstmt.setString(4, teacherData.get("department"));
            pstmt.setString(5, teacherData.get("password")); // 在实际应用中应该加密

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean updateTeacher(String teacherId, Map<String, String> teacherData) throws Exception {
        StringBuilder sql = new StringBuilder("UPDATE teachers SET ");
        List<Object> params = new ArrayList<>();

        // 动态构建SET子句
        if (teacherData.containsKey("name")) {
            sql.append("name = ?, ");
            params.add(teacherData.get("name"));
        }
        if (teacherData.containsKey("email")) {
            sql.append("email = ?, ");
            params.add(teacherData.get("email"));
        }
        if (teacherData.containsKey("department")) {
            sql.append("department = ?, ");
            params.add(teacherData.get("department"));
        }
        if (teacherData.containsKey("status")) {
            sql.append("status = ?, ");
            params.add(teacherData.get("status"));
        }

        // 移除最后一个逗号
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(teacherId);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean deleteTeacher(String teacherId) throws Exception {
        String sql = "DELETE FROM teachers WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 检查教师是否存在
     */
    public boolean teacherExists(String teacherId) throws Exception {
        String sql = "SELECT 1 FROM teachers WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 搜索教师
     */
    public List<Map<String, Object>> searchTeachers(String keyword) throws Exception {
        List<Map<String, Object>> teachers = new ArrayList<>();
        String sql = "SELECT id, name, email, department, status FROM teachers " +
                "WHERE id LIKE ? OR name LIKE ? OR email LIKE ? OR department LIKE ? ORDER BY id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> teacher = new HashMap<>();
                teacher.put("id", rs.getString("id"));
                teacher.put("name", rs.getString("name"));
                teacher.put("email", rs.getString("email"));
                teacher.put("department", rs.getString("department"));
                teacher.put("status", rs.getString("status"));
                teachers.add(teacher);
            }
        }
        return teachers;
    }
}