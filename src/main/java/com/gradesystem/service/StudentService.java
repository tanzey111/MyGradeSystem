package com.gradesystem.service;

import com.gradesystem.dao.StudentDAO;

import java.util.List;
import java.util.Map;

public class StudentService {
    private StudentDAO studentDAO;

    public StudentService() {
        this.studentDAO = new StudentDAO();
    }
    /**
     * 获取所有学生列表
     */
    public List<Map<String, Object>> getAllStudents() {
        try {
            return studentDAO.getAllStudents();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取学生列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取学生信息
     */
    public Map<String, Object> getStudentById(String studentId) {
        try {
            return studentDAO.getStudentById(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取学生信息失败: " + e.getMessage());
        }
    }

    /**
     * 添加学生
     */
    public boolean addStudent(Map<String, String> studentData) {
        try {
            // 验证数据
            if (!validateStudentData(studentData)) {
                return false;
            }

            // 检查学号是否已存在
            if (studentDAO.studentExists(studentData.get("id"))) {
                throw new RuntimeException("学号已存在");
            }

            return studentDAO.addStudent(studentData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("添加学生失败: " + e.getMessage());
        }
    }

    /**
     * 更新学生信息
     */
    public boolean updateStudent(String studentId, Map<String, String> studentData) {
        try {
            // 验证数据
            if (!validateStudentData(studentData, false)) {
                return false;
            }

            return studentDAO.updateStudent(studentId, studentData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新学生信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除学生
     */
    public boolean deleteStudent(String studentId) {
        try {
            // 检查学生是否有成绩记录
            if (studentDAO.hasGradeRecords(studentId)) {
                throw new RuntimeException("该学生有成绩记录，无法删除");
            }

            return studentDAO.deleteStudent(studentId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除学生失败: " + e.getMessage());
        }
    }

    /**
     * 验证学生数据
     */
    private boolean validateStudentData(Map<String, String> studentData, boolean isNew) {
        if (studentData == null) return false;

        // 验证必填字段
        if (isNew && (studentData.get("id") == null || studentData.get("id").trim().isEmpty())) {
            return false;
        }

        if (studentData.get("name") == null || studentData.get("name").trim().isEmpty()) {
            return false;
        }

        if (studentData.get("class") == null || studentData.get("class").trim().isEmpty()) {
            return false;
        }

        // 验证学号格式
        if (isNew && !studentData.get("id").matches("\\d+")) {
            return false;
        }

        return true;
    }

    private boolean validateStudentData(Map<String, String> studentData) {
        return validateStudentData(studentData, true);
    }

    /**
     * 搜索学生
     */
    public List<Map<String, Object>> searchStudents(String keyword) {
        try {
            return studentDAO.searchStudents(keyword);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("搜索学生失败: " + e.getMessage());
        }
    }

    /**
     * 学生修改密码
     */
    public boolean changeStudentPassword(String studentId, String oldPassword, String newPassword) {
        try {
            // 验证旧密码
            if (!studentDAO.verifyStudentPassword(studentId, oldPassword)) {
                throw new RuntimeException("旧密码错误");
            }

            // 验证新密码强度
            if (newPassword == null || newPassword.trim().length() < 6) {
                throw new RuntimeException("新密码长度至少6位");
            }

            return studentDAO.updateStudentPassword(studentId, newPassword);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 管理员重置学生密码
     */
    public boolean resetStudentPassword(String studentId, String newPassword) {
        try {
            if (newPassword == null || newPassword.trim().length() < 6) {
                throw new RuntimeException("密码长度至少6位");
            }

            return studentDAO.resetStudentPassword(studentId, newPassword);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("重置密码失败: " + e.getMessage());
        }
    }
}
