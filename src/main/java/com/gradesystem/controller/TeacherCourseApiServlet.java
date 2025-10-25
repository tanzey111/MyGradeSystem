package com.gradesystem.controller;

import com.gradesystem.service.GradeService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/teacher/courses/*")
public class TeacherCourseApiServlet extends BaseApiServlet {
    private GradeService gradeService = new GradeService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String teacherId = getCurrentUserId(request);
            String userRole = getCurrentUserRole(request);

            if (!"teacher".equals(userRole)) {
                sendError(response, "需要教师权限");
                return;
            }

            if (pathInfo == null || "/".equals(pathInfo)) {
                // 获取教师所教课程列表
                List<String> courses = gradeService.getTeacherCourses(teacherId);
                sendSuccess(response, courses);
            } else if ("/check".equals(pathInfo)) {
                // 检查教师是否有权限管理某课程
                String courseName = request.getParameter("courseName");
                if (courseName == null || courseName.trim().isEmpty()) {
                    sendError(response, "课程名称不能为空");
                    return;
                }

                boolean canManage = gradeService.canTeacherManageCourse(teacherId, courseName);
                sendSuccess(response, canManage);
            } else {
                sendError(response, "API路径不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }
}