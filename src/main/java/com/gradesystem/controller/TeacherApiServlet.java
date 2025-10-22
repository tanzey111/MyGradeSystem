package com.gradesystem.controller;

import com.gradesystem.service.GradeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/teacher/*")
public class TeacherApiServlet extends BaseApiServlet {
    private GradeService gradeService = new GradeService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String userRole = getCurrentUserRole(request);

            // 验证教师权限
            if (!"teacher".equals(userRole)) {
                sendError(response, "需要教师权限");
                return;
            }

            if ("/system/config".equals(pathInfo)) {
                // 获取系统配置
                Map<String, Object> config = gradeService.getQueryPeriodConfig();
                sendSuccess(response, config);

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

            // 验证教师权限
            if (!"teacher".equals(userRole)) {
                sendError(response, "需要教师权限");
                return;
            }

            if ("/system/config".equals(pathInfo)) {
                // 更新系统配置
                Map<String, Object> configData = objectMapper.readValue(request.getReader(), HashMap.class);
                Long startTime = null;
                Long endTime = null;
                Boolean isActive = null;

                // 安全地处理可能为null的值
                if (configData.containsKey("startTime") && configData.get("startTime") != null) {
                    startTime = Long.parseLong(configData.get("startTime").toString());
                }

                if (configData.containsKey("endTime") && configData.get("endTime") != null) {
                    endTime = Long.parseLong(configData.get("endTime").toString());
                }

                if (configData.containsKey("isActive")) {
                    isActive = Boolean.valueOf(configData.get("isActive").toString());
                }

                boolean result = gradeService.setQueryPeriod(startTime, endTime, isActive);

                if (result) {
                    sendSuccess(response, null, "系统配置更新成功");
                } else {
                    sendError(response, "系统配置更新失败");
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