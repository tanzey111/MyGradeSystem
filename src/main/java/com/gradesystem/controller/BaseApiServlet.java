package com.gradesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradesystem.model.ApiResponse;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class BaseApiServlet extends HttpServlet {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 为所有请求设置字符编码
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        super.service(request, response);
    }

    // 发送JSON响应
    protected void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        String jsonResponse = objectMapper.writeValueAsString(data);
        out.print(jsonResponse);
        out.flush();
    }

    // 发送成功响应
    protected void sendSuccess(HttpServletResponse response, Object data) throws IOException {
        sendJsonResponse(response, ApiResponse.success(data));
    }

    protected void sendSuccess(HttpServletResponse response, Object data, String message) throws IOException {
        sendJsonResponse(response, ApiResponse.success(data, message));
    }

    // 发送错误响应
    protected void sendError(HttpServletResponse response, String error) throws IOException {
        sendJsonResponse(response, ApiResponse.error(error));
    }

    // 获取当前登录用户ID
    protected String getCurrentUserId(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("userId");
    }

    // 获取当前用户角色
    protected String getCurrentUserRole(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("userRole");
    }
}