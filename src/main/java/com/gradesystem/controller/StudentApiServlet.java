package com.gradesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradesystem.service.StudentService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/student/*")
public class StudentApiServlet extends BaseApiServlet {
    private StudentService studentService = new StudentService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String userRole = getCurrentUserRole(request);
            String userId = getCurrentUserId(request);

            // 验证学生权限
            if (!"student".equals(userRole)) {
                sendError(response, "需要学生权限");
                return;
            }

            if ("/change-password".equals(pathInfo)) {
                // 学生修改密码
                Map<String, String> passwordData = objectMapper.readValue(request.getReader(), HashMap.class);
                String oldPassword = passwordData.get("oldPassword");
                String newPassword = passwordData.get("newPassword");

                if (oldPassword == null || newPassword == null) {
                    sendError(response, "参数不完整");
                    return;
                }

                boolean result = studentService.changeStudentPassword(userId, oldPassword, newPassword);

                if (result) {
                    sendSuccess(response, null, "密码修改成功");
                } else {
                    sendError(response, "密码修改失败");
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