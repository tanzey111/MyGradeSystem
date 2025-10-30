package com.gradesystem.controller;

import com.gradesystem.service.AuthService;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthApiServlet extends BaseApiServlet {
    private AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 添加CORS配置
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        try {
            String pathInfo = request.getPathInfo();

            if ("/login".equals(pathInfo)) {
                // 登录API
                Map<String, String> credentials = objectMapper.readValue(request.getReader(), HashMap.class);
                String username = credentials.get("username");
                String password = credentials.get("password");
                String role = credentials.get("role");

                Map<String, Object> userInfo = authService.authenticate(username, password, role);

                if (userInfo != null) {
                    // 创建会话
                    HttpSession session = request.getSession();
                    session.setAttribute("userId", userInfo.get("id"));
                    session.setAttribute("userRole", role);
                    session.setAttribute("userName", userInfo.get("name"));

                    sendSuccess(response, userInfo, "登录成功");
                } else {
                    sendError(response, "用户名或密码错误");
                }

            } else if ("/logout".equals(pathInfo)) {
                // 退出登录
                request.getSession().invalidate();
                sendSuccess(response, null, "退出成功");

            } else {
                sendError(response, "API路径不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }

    // 处理OPTIONS请求
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
