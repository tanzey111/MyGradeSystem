package com.gradesystem.service;

import com.gradesystem.dao.GradeDAO;
import com.gradesystem.model.Grade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeService {
    private GradeDAO gradeDAO;

    public GradeService() {
        this.gradeDAO = new GradeDAO();
    }

    /**
     * 获取指定学生的所有成绩
     * @param studentId 学生ID
     * @return 成绩列表
     */
    public List<Grade> getStudentGrades(String studentId) {
        try {
            return gradeDAO.getGradesByStudentId(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取学生成绩失败: " + e.getMessage());
        }
    }
    public Grade getGradeById(int gradeId) {
        try {
            return gradeDAO.getGradeById(gradeId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有学生的成绩（教师权限）
     * @return 所有成绩列表
     */
    public List<Grade> getAllGrades() {
        try {
            return gradeDAO.getAllGrades();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取所有成绩失败: " + e.getMessage());
        }
    }

    /**
     * 添加或更新成绩
     * @param grade 成绩对象
     * @return 操作是否成功
     */
    public boolean addOrUpdateGrade(Grade grade) {
        try {
            // 验证成绩数据
            if (!validateGrade(grade)) {
                return false;
            }

            // 检查学生是否存在
            if (!gradeDAO.studentExists(grade.getStudentId())) {
                return false;
            }

            // 添加或更新成绩
            return gradeDAO.addOrUpdateGrade(grade);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("保存成绩失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID更新成绩
     */
    public boolean updateGradeById(int gradeId, double score, String semester) {
        try {
            return gradeDAO.updateGradeById(gradeId, score, semester);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("更新成绩失败: " + e.getMessage());
        }
    }



    /**
     * 批量导入成绩
     * @param grades 成绩列表
     * @return 成功导入的数量
     */
    public int importGrades(List<Grade> grades) {
        int successCount = 0;

        try {
            for (Grade grade : grades) {
                if (validateGrade(grade) && gradeDAO.studentExists(grade.getStudentId())) {
                    if (gradeDAO.addOrUpdateGrade(grade)) {
                        successCount++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("批量导入成绩失败: " + e.getMessage());
        }

        return successCount;
    }

    /**
     * 删除成绩
     * @param gradeId 成绩ID
     * @return 删除是否成功
     */
    public boolean deleteGrade(int gradeId) {
        try {
            return gradeDAO.deleteGrade(gradeId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("删除成绩失败: " + e.getMessage());
        }
    }

    /**
     * 验证成绩数据是否有效
     * @param grade 成绩对象
     * @return 是否有效
     */
    private boolean validateGrade(Grade grade) {
        if (grade == null) {
            return false;
        }

        // 验证学号
        if (grade.getStudentId() == null || grade.getStudentId().trim().isEmpty()) {
            return false;
        }

        // 验证课程名称
        if (grade.getCourseName() == null || grade.getCourseName().trim().isEmpty()) {
            return false;
        }

        // 验证成绩范围 (0-100)
        if (grade.getScore() < 0 || grade.getScore() > 100) {
            return false;
        }

        // 验证学期
        if (grade.getSemester() == null || grade.getSemester().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 检查当前是否在允许查询成绩的时间段内 - 修复版本
     */
    public boolean isWithinQueryPeriod() {
        try {
            // 从数据库获取查询时间段配置
            Map<String, Object> periodConfig = gradeDAO.getQueryPeriodConfig();

            if (periodConfig == null || periodConfig.isEmpty()) {
                // 如果没有配置，默认允许查询
                return true;
            }

            // 获取配置
            Long startTime = (Long) periodConfig.get("start_time");
            Long endTime = (Long) periodConfig.get("end_time");
            Boolean isActive = (Boolean) periodConfig.get("is_active");

            // 检查是否激活时间限制
            if (isActive != null && !isActive) {
                // 时间限制未激活，允许查询
                return true;
            }

            // 获取当前时间
            long currentTime = System.currentTimeMillis();

            // 修复时间验证逻辑
            boolean withinPeriod = true;

            // 如果设置了开始时间，检查当前时间是否在开始时间之后
            if (startTime != null && startTime > 0) {
                withinPeriod = withinPeriod && (currentTime >= startTime);
            }

            // 如果设置了结束时间，检查当前时间是否在结束时间之前
            if (endTime != null && endTime > 0) {
                withinPeriod = withinPeriod && (currentTime <= endTime);
            }

            return withinPeriod;

        } catch (Exception e) {
            e.printStackTrace();
            // 如果检查失败，默认不允许查询以保证安全
            return false;
        }
    }

    /**
     * 设置成绩查询时间段
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @param isActive 是否激活
     * @return 设置是否成功
     */
    public boolean setQueryPeriod(Long startTime, Long endTime, Boolean isActive) {
        try {
            return gradeDAO.setQueryPeriodConfig(startTime, endTime, isActive);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("设置查询时间段失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生的成绩统计信息
     * @param studentId 学生ID
     * @return 统计信息Map
     */
    public Map<String, Object> getStudentGradeStats(String studentId) {
        try {
            return gradeDAO.getStudentGradeStats(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取成绩统计失败: " + e.getMessage());
        }
    }

    /**
     * 根据课程名称搜索成绩
     * @param courseName 课程名称（支持模糊搜索）
     * @return 成绩列表
     */
    public List<Grade> searchGradesByCourse(String courseName) {
        try {
            return gradeDAO.searchGradesByCourse(courseName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("搜索成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取查询时间段配置
     */
    public Map<String, Object> getQueryPeriodConfig() {
        try {
            return gradeDAO.getQueryPeriodConfig();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取查询时间段配置失败: " + e.getMessage());
        }
    }


    /**
     * 从CSV文件导入成绩
     */
    public Map<String, Object> importGradesFromCSV(String filePath) {
        Map<String, Object> result = new HashMap<>();

        try {
            int successCount = gradeDAO.importGradesFromCSV(filePath);
            result.put("successCount", successCount);
            result.put("message", String.format("成功导入 %d 条成绩记录", successCount));
            result.put("errors", new ArrayList<String>()); // 可以扩展返回具体错误

        } catch (Exception e) {
            e.printStackTrace();
            result.put("successCount", 0);
            result.put("message", "导入失败: " + e.getMessage());
            result.put("errors", List.of(e.getMessage()));
        }

        return result;
    }

    /**
     * 从Excel文件导入成绩
     */
    public Map<String, Object> importGradesFromExcel(String filePath) {
        Map<String, Object> result = new HashMap<>();

        try {
            int successCount = gradeDAO.importGradesFromExcel(filePath);
            result.put("successCount", successCount);
            result.put("message", String.format("成功导入 %d 条成绩记录", successCount));
            result.put("errors", new ArrayList<String>());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("successCount", 0);
            result.put("message", "导入失败: " + e.getMessage());
            result.put("errors", List.of(e.getMessage()));
        }

        return result;
    }
}
