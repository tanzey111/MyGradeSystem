package com.gradesystem.service;

import com.gradesystem.dao.TeacherDAO;
import java.util.List;
import java.util.Map;

public class TeacherService {
    private TeacherDAO teacherDAO = new TeacherDAO();

    public List<Map<String, Object>> getAllTeachers() {
        try {
            return teacherDAO.getAllTeachers();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取教师列表失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getTeacherById(String teacherId) {
        try {
            return teacherDAO.getTeacherById(teacherId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取教师信息失败: " + e.getMessage());
        }
    }

    public boolean addTeacher(Map<String, String> teacherData) {
        try {
            // 验证数据
            if (!validateTeacherData(teacherData, true)) {
                return false;
            }

            // 检查工号是否已存在
            if (teacherDAO.teacherExists(teacherData.get("id"))) {
                throw new RuntimeException("工号 " + teacherData.get("id") + " 已存在");
            }

            return teacherDAO.addTeacher(teacherData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("添加教师失败: " + e.getMessage());
        }
    }

    public boolean updateTeacher(String teacherId, Map<String, String> teacherData) {
        try {
            // 验证数据（更新时不需要验证工号）
            if (!validateTeacherData(teacherData, false)) {
                return false;
            }

            return teacherDAO.updateTeacher(teacherId, teacherData);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新教师信息失败: " + e.getMessage());
        }
    }

    public boolean deleteTeacher(String teacherId) {
        try {
            return teacherDAO.deleteTeacher(teacherId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除教师失败: " + e.getMessage());
        }
    }

    /**
     * 验证教师数据
     */
    private boolean validateTeacherData(Map<String, String> teacherData, boolean isNew) {
        if (teacherData == null) {
            throw new RuntimeException("教师数据不能为空");
        }

        // 验证必填字段
        if (isNew) {
            if (teacherData.get("id") == null || teacherData.get("id").trim().isEmpty()) {
                throw new RuntimeException("工号不能为空");
            }
            if (teacherData.get("password") == null || teacherData.get("password").trim().isEmpty()) {
                throw new RuntimeException("密码不能为空");
            }
        }

        if (teacherData.get("name") == null || teacherData.get("name").trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }

        // 验证邮箱格式（如果提供了邮箱）
        if (teacherData.containsKey("email") && teacherData.get("email") != null &&
                !teacherData.get("email").trim().isEmpty()) {
            String email = teacherData.get("email");
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new RuntimeException("邮箱格式不正确");
            }
        }

        return true;
    }

    /**
     * 搜索教师
     */
    public List<Map<String, Object>> searchTeachers(String keyword) {
        try {
            return teacherDAO.searchTeachers(keyword);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("搜索教师失败: " + e.getMessage());
        }
    }
}