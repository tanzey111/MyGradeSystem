package com.gradesystem.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherCourseDAO {

    /**
     * 获取教师所教的所有课程名称
     */
    public List<String> getCourseNamesByTeacher(String teacherId) throws SQLException {
        List<String> courseNames = new ArrayList<>();
        String sql = "SELECT DISTINCT course_name FROM teacher_courses WHERE teacher_id = ? ORDER BY course_name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courseNames.add(rs.getString("course_name"));
            }
        }
        return courseNames;
    }

    /**
     * 获取教师某学期所教的所有课程名称
     */
    public List<String> getCourseNamesByTeacherAndSemester(String teacherId, String semester) throws SQLException {
        List<String> courseNames = new ArrayList<>();
        String sql = "SELECT course_name FROM teacher_courses WHERE teacher_id = ? AND semester = ? ORDER BY course_name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, semester);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courseNames.add(rs.getString("course_name"));
            }
        }
        return courseNames;
    }

    /**
     * 检查教师是否有权限管理该课程
     */
    public boolean canTeacherManageCourse(String teacherId, String courseName) throws SQLException {
        String sql = "SELECT 1 FROM teacher_courses WHERE teacher_id = ? AND course_name = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, courseName);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 检查教师是否有权限管理该课程（特定学期）
     */
    public boolean canTeacherManageCourse(String teacherId, String courseName, String semester) throws SQLException {
        String sql = "SELECT 1 FROM teacher_courses WHERE teacher_id = ? AND course_name = ? AND semester = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, courseName);
            pstmt.setString(3, semester);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 检查教师是否有权限管理该成绩
     */
    public boolean canTeacherManageGrade(String teacherId, int gradeId) throws SQLException {
        String sql = "SELECT 1 FROM grades g " +
                "INNER JOIN teacher_courses tc ON " +
                "  g.course_name = tc.course_name AND " +
                "  g.semester = tc.semester AND " +
                "  tc.teacher_id = ? " +
                "INNER JOIN student_courses sc ON " +
                "  g.student_id = sc.student_id AND " +
                "  g.course_name = sc.course_name AND " +
                "  g.semester = sc.semester AND " +
                "  sc.teacher_id = ? AND " +
                "  sc.status = 'selected' " +
                "WHERE g.id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, teacherId);
            pstmt.setInt(3, gradeId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 检查学生是否选了该教师该学期的这门课
     */
    public boolean isStudentEnrolled(String studentId, String teacherId, String courseName, String semester) throws SQLException {
        String sql = "SELECT 1 FROM student_courses " +
                "WHERE student_id = ? AND teacher_id = ? AND course_name = ? AND semester = ? AND status = 'selected'";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, teacherId);
            pstmt.setString(3, courseName);
            pstmt.setString(4, semester);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * 为教师添加课程
     */
    public boolean addCourseForTeacher(String teacherId, String courseName, String semester) throws SQLException {
        String sql = "INSERT INTO teacher_courses (teacher_id, course_name, semester) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, courseName);
            pstmt.setString(3, semester);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 获取教师所教课程的所有成绩 （考虑课程+学期+教师+选课）
     */
    public List<Map<String, Object>> getGradesByTeacher(String teacherId) throws SQLException {
        List<Map<String, Object>> grades = new ArrayList<>();

        // 精确查询：必须同时满足课程名称、学期、教师ID和学生选课
        String sql = "SELECT g.* FROM grades g " +
                "INNER JOIN teacher_courses tc ON " +
                "  g.course_name = tc.course_name AND " +
                "  g.semester = tc.semester AND " +
                "  tc.teacher_id = ? " +
                "INNER JOIN student_courses sc ON " +
                "  g.student_id = sc.student_id AND " +
                "  g.course_name = sc.course_name AND " +
                "  g.semester = sc.semester AND " +
                "  sc.teacher_id = ? AND " +
                "  sc.status = 'selected' " +
                "ORDER BY g.semester DESC, g.student_id, g.course_name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, teacherId);
            pstmt.setString(2, teacherId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> grade = new HashMap<>();
                grade.put("id", rs.getInt("id"));
                grade.put("studentId", rs.getString("student_id"));
                grade.put("studentName", rs.getString("student_name"));
                grade.put("courseName", rs.getString("course_name"));
                grade.put("score", rs.getDouble("score"));
                grade.put("semester", rs.getString("semester"));
                grade.put("createdAt", rs.getTimestamp("created_at"));
                grades.add(grade);
            }
        }
        return grades;
    }
}