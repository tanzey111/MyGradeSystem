package com.gradesystem.controller;

import com.gradesystem.service.GradeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/system/*")
public class SystemApiServlet extends BaseApiServlet {
    private GradeService gradeService = new GradeService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置统一的响应类型和编码
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();
            System.out.println("System API 请求路径: " + pathInfo);

            if (pathInfo == null || "/config".equals(pathInfo)) {
                // 获取系统配置 - 允许所有登录用户访问
                Map<String, Object> config = gradeService.getQueryPeriodConfig();
                System.out.println("系统配置: " + config);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", config);

                String jsonResponse = objectMapper.writeValueAsString(result);
                response.getWriter().write(jsonResponse);

            } else {
                // API路径不存在
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("message", "API路径不存在: " + pathInfo);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(objectMapper.writeValueAsString(errorResult));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "服务器错误: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置统一的响应类型和编码
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            String pathInfo = request.getPathInfo();

            // 系统配置的POST请求只允许管理员操作，所以这里直接返回错误
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "此操作需要管理员权限");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "服务器错误: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        }
    }
}