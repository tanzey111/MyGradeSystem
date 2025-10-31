package com.gradesystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradesystem.model.Grade;
import com.gradesystem.service.GradeService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/grades/*")
public class GradeApiServlet extends BaseApiServlet {
    private GradeService gradeService = new GradeService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String pathInfo = request.getPathInfo();
            String currentUserId = getCurrentUserId(request);
            String userRole = getCurrentUserRole(request);

            if (currentUserId == null) {
                sendError(response, "未登录");
                return;
            }

            if ("/my".equals(pathInfo)) {
                // 查询我的成绩 /api/grades/my
                if (!"student".equals(userRole)) {
                    sendError(response, "无权访问");
                    return;
                }

                // 检查查询时间段
                if (!gradeService.isWithinQueryPeriod()) {
                    sendError(response, "不在成绩查询时间段内");
                    return;
                }

                // 直接返回包含学分信息的Map列表
                List<Map<String, Object>> grades = gradeService.getStudentGradesWithCredits(currentUserId);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", grades);

                // 使用Jackson或其他JSON库返回
                response.setContentType("application/json;charset=UTF-8");
                objectMapper.writeValue(response.getWriter(), result);

            } else if ("/all".equals(pathInfo)) {
                // 查询教师可管理的成绩 /api/grades/all
                if (!"teacher".equals(userRole)) {
                    sendError(response, "需要教师权限");
                    return;
                }

                // 改为获取当前教师所管理的成绩
                List<Grade> grades = gradeService.getGradesByTeacher(currentUserId);
                sendSuccess(response, grades);

            } else if (pathInfo != null && pathInfo.matches("/\\d+")) {
                // 根据ID获取单个成绩 /api/grades/{id}
                if (!"teacher".equals(userRole)) {
                    sendError(response, "需要教师权限");
                    return;
                }

                try {
                    int gradeId = Integer.parseInt(pathInfo.substring(1));

                    // 检查教师是否有权限管理该成绩
                    if (!gradeService.canTeacherManageGrade(currentUserId, gradeId)) {
                        sendError(response, "无权查看此成绩");
                        return;
                    }

                    Grade grade = getGradeById(gradeId);

                    if (grade != null) {
                        sendSuccess(response, grade);
                    } else {
                        sendError(response, "成绩记录不存在");
                    }
                } catch (NumberFormatException e) {
                    sendError(response, "无效的成绩ID");
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
        try {
            String userRole = getCurrentUserRole(request);
            String currentUserId = getCurrentUserId(request);

            if (!"teacher".equals(userRole)) {
                sendError(response, "需要教师权限");
                return;
            }

            // 解析JSON请求体
            Grade grade = objectMapper.readValue(request.getReader(), Grade.class);

            // 传入教师ID进行权限验证
            boolean result = gradeService.addOrUpdateGrade(grade, currentUserId);

            if (result) {
                sendSuccess(response, null, "成绩保存成功");
            } else {
                sendError(response, "成绩保存失败");
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
            String currentUserId = getCurrentUserId(request);

            if (!"teacher".equals(userRole)) {
                sendError(response, "需要教师权限");
                return;
            }

            if (pathInfo == null || !pathInfo.matches("/\\d+")) {
                sendError(response, "需要指定有效的成绩ID");
                return;
            }

            try {
                int gradeId = Integer.parseInt(pathInfo.substring(1));

                // 检查教师是否有权限管理该成绩
                if (!gradeService.canTeacherManageGrade(currentUserId, gradeId)) {
                    sendError(response, "无权修改此成绩");
                    return;
                }

                // 检查成绩是否存在
                Grade existingGrade = getGradeById(gradeId);
                if (existingGrade == null) {
                    sendError(response, "成绩记录不存在");
                    return;
                }

                // 解析更新数据
                Map<String, Object> updateData = objectMapper.readValue(request.getReader(), HashMap.class);
                Double score = updateData.get("score") != null ?
                        Double.parseDouble(updateData.get("score").toString()) : null;
                String semester = (String) updateData.get("semester");

                // 验证数据
                if (score == null || semester == null) {
                    sendError(response, "成绩和学期不能为空");
                    return;
                }

                if (score < 0 || score > 100) {
                    sendError(response, "成绩必须在0-100之间");
                    return;
                }

                // 使用基于ID的更新方法
                boolean result = gradeService.updateGradeById(gradeId, score, semester);

                if (result) {
                    sendSuccess(response, null, "成绩更新成功");
                } else {
                    sendError(response, "成绩更新失败");
                }

            } catch (NumberFormatException e) {
                sendError(response, "无效的成绩ID");
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
            String currentUserId = getCurrentUserId(request);

            if (!"teacher".equals(userRole)) {
                sendError(response, "需要教师权限");
                return;
            }

            if (pathInfo == null || !pathInfo.matches("/\\d+")) {
                sendError(response, "需要指定有效的成绩ID");
                return;
            }

            try {
                int gradeId = Integer.parseInt(pathInfo.substring(1));

                // 检查教师是否有权限管理该成绩
                if (!gradeService.canTeacherManageGrade(currentUserId, gradeId)) {
                    sendError(response, "无权删除此成绩");
                    return;
                }

                boolean result = gradeService.deleteGrade(gradeId);

                if (result) {
                    sendSuccess(response, null, "成绩删除成功");
                } else {
                    sendError(response, "成绩删除失败");
                }

            } catch (NumberFormatException e) {
                sendError(response, "无效的成绩ID");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "服务器错误: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取成绩记录
     */
    private Grade getGradeById(int gradeId) {
        try {
            return gradeService.getGradeById(gradeId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}