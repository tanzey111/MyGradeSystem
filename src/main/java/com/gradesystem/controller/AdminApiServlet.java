package com.gradesystem.controller;
import com.gradesystem.service.StudentService;
import com.gradesystem.service.GradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradesystem.service.TeacherService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminApiServlet extends BaseApiServlet {
    private StudentService studentService = new StudentService();
    private GradeService gradeService = new GradeService();
    private ObjectMapper objectMapper = new ObjectMapper();
    private TeacherService teacherService=new TeacherService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String userRole = getCurrentUserRole(request);

            // 验证管理员权限
            if (!"admin".equals(userRole)) {
                sendError(response, "需要管理员权限");
                return;
            }

            if ("/students".equals(pathInfo)) {
                // 获取所有学生列表
                List<Map<String, Object>> students = studentService.getAllStudents();
                sendSuccess(response, students);

            } else if ("/teachers".equals(pathInfo)) {
                // 获取所有教师列表
                List<Map<String, Object>> teachers = teacherService.getAllTeachers();
                sendSuccess(response, teachers);

            } else if (pathInfo != null && pathInfo.startsWith("/students/")) {
                // 获取单个学生信息
                String studentId = pathInfo.substring("/students/".length());
                Map<String, Object> student = studentService.getStudentById(studentId);

                if (student != null) {
                    sendSuccess(response, student);
                } else {
                    sendError(response, "学生不存在");
                }

            } else if (pathInfo != null && pathInfo.startsWith("/teachers/")) {
                // 获取单个教师信息
                String teacherId = pathInfo.substring("/teachers/".length());
                Map<String, Object> teacher = teacherService.getTeacherById(teacherId);

                if (teacher != null) {
                    sendSuccess(response, teacher);
                } else {
                    sendError(response, "教师不存在");
                }

            } else {
                sendError(response, "API路径不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置字符编码
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        try {
            String pathInfo = request.getPathInfo();
            String userRole = getCurrentUserRole(request);

            if (!"admin".equals(userRole)) {
                sendError(response, "需要管理员权限");
                return;
            }

            if ("/students".equals(pathInfo)) {
                // 添加学生
                Map<String, String> studentData = objectMapper.readValue(request.getReader(), HashMap.class);
                boolean result = studentService.addStudent(studentData);

                if (result) {
                    sendSuccess(response, null, "学生添加成功");
                } else {
                    sendError(response, "学生添加失败");
                }

            } else if ("/teachers".equals(pathInfo)) {
                // 添加教师
                Map<String, String> teacherData = objectMapper.readValue(request.getReader(), HashMap.class);
                boolean result = teacherService.addTeacher(teacherData);

                if (result) {
                    sendSuccess(response, null, "教师添加成功");
                } else {
                    sendError(response, "教师添加失败");
                }

            } else {
                sendError(response, "API路径不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String userRole = getCurrentUserRole(request);

            if (!"admin".equals(userRole)) {
                sendError(response, "需要管理员权限");
                return;
            }

            if (pathInfo != null && pathInfo.startsWith("/students/")) {
                // 更新学生信息
                String studentId = pathInfo.substring("/students/".length());
                Map<String, String> studentData = objectMapper.readValue(request.getReader(), HashMap.class);

                boolean result = studentService.updateStudent(studentId, studentData);

                if (result) {
                    sendSuccess(response, null, "学生信息更新成功");
                } else {
                    sendError(response, "学生信息更新失败");
                }

            } else if (pathInfo != null && pathInfo.startsWith("/teachers/")) {
                // 更新教师信息
                String teacherId = pathInfo.substring("/teachers/".length());
                Map<String, String> teacherData = objectMapper.readValue(request.getReader(), HashMap.class);

                boolean result = teacherService.updateTeacher(teacherId, teacherData);

                if (result) {
                    sendSuccess(response, null, "教师信息更新成功");
                } else {
                    sendError(response, "教师信息更新失败");
                }

            } else {
                sendError(response, "API路径不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String userRole = getCurrentUserRole(request);

            if (!"admin".equals(userRole)) {
                sendError(response, "需要管理员权限");
                return;
            }

            if (pathInfo != null && pathInfo.startsWith("/students/")) {
                // 删除学生
                String studentId = pathInfo.substring("/students/".length());
                boolean result = studentService.deleteStudent(studentId);

                if (result) {
                    sendSuccess(response, null, "学生删除成功");
                } else {
                    sendError(response, "学生删除失败");
                }

            } else if (pathInfo != null && pathInfo.startsWith("/teachers/")) {
                // 删除教师
                String teacherId = pathInfo.substring("/teachers/".length());
                boolean result = teacherService.deleteTeacher(teacherId);

                if (result) {
                    sendSuccess(response, null, "教师删除成功");
                } else {
                    sendError(response, "教师删除失败");
                }

            } else {
                sendError(response, "API路径不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }
}