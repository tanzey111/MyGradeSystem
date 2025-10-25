package com.gradesystem.service;

import com.gradesystem.dao.GradeDAO;
import com.gradesystem.model.Grade;
import com.gradesystem.model.StudentProcessResult;

import java.io.File;
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
     * 删除成绩
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
     * 验证成绩数据是否有效（原有简单验证，用于单个成绩操作）
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
     * 检查当前是否在允许查询成绩的时间段内
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
     * 从CSV文件导入成绩 - 修复版本
     */
    public Map<String, Object> importGradesFromCSV(String filePath) {
        System.out.println("=== 开始CSV文件导入 ===");
        System.out.println("文件路径: " + filePath);

        Map<String, Object> result = new HashMap<>();
        List<String> parseErrors = new ArrayList<>();

        try {
            // 解析CSV文件
            List<Grade> grades = gradeDAO.parseCSVFile(new File(filePath), parseErrors);

            System.out.println("解析结果 - 成绩数量: " + grades.size());
            System.out.println("解析结果 - 错误数量: " + parseErrors.size());

            if (!parseErrors.isEmpty()) {
                System.out.println("解析错误: " + parseErrors);
            }

            // 如果解析有错误且没有解析出任何成绩数据，直接返回错误
            if (!parseErrors.isEmpty() && grades.isEmpty()) {
                System.out.println("CSV文件解析失败，没有有效数据");
                return createErrorResult(parseErrors, "CSV文件解析失败，请检查文件格式");
            }

            // 如果解析出成绩数据，进行导入
            if (!grades.isEmpty()) {
                Map<String, Object> importResult = processGradeImport(grades);
                result.putAll(importResult);
            } else {
                // 没有有效数据
                initializeZeroResult(result);
                result.put("message", "CSV文件为空或没有有效数据");
                result.put("hasErrors", true);
            }

            // 添加解析错误到最终结果
            addParseErrorsToResult(result, parseErrors);

            System.out.println("导入完成，统计结果: " + result);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CSV导入异常: " + e.getMessage());
            parseErrors.add("文件处理异常: " + e.getMessage());
            return createErrorResult(parseErrors, "解析CSV文件失败: " + e.getMessage());
        }
    }

    /**
     * 从Excel文件导入成绩 - 修复版本
     */
    public Map<String, Object> importGradesFromExcel(String filePath) {
        System.out.println("=== 开始Excel文件导入 ===");
        System.out.println("文件路径: " + filePath);

        Map<String, Object> result = new HashMap<>();
        List<String> parseErrors = new ArrayList<>();

        try {
            // 解析Excel文件
            List<Grade> grades = gradeDAO.parseExcelFile(new File(filePath), parseErrors);

            System.out.println("解析结果 - 成绩数量: " + grades.size());
            System.out.println("解析结果 - 错误数量: " + parseErrors.size());

            if (!parseErrors.isEmpty()) {
                System.out.println("解析错误: " + parseErrors);
            }

            // 如果解析有错误且没有解析出任何成绩数据，直接返回错误
            if (!parseErrors.isEmpty() && grades.isEmpty()) {
                System.out.println("Excel文件解析失败，没有有效数据");
                return createErrorResult(parseErrors, "Excel文件解析失败，请检查文件格式");
            }

            // 如果解析出成绩数据，进行导入
            if (!grades.isEmpty()) {
                Map<String, Object> importResult = processGradeImport(grades);
                result.putAll(importResult);
            } else {
                // 没有有效数据
                initializeZeroResult(result);
                result.put("message", "Excel文件为空或没有有效数据");
                result.put("hasErrors", true);
            }

            // 添加解析错误到最终结果
            addParseErrorsToResult(result, parseErrors);

            System.out.println("导入完成，统计结果: " + result);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Excel导入异常: " + e.getMessage());
            parseErrors.add("文件处理异常: " + e.getMessage());
            return createErrorResult(parseErrors, "解析Excel文件失败: " + e.getMessage());
        }
    }

    /**
     * 添加解析错误到结果 - 修复版本
     */
    private void addParseErrorsToResult(Map<String, Object> result, List<String> parseErrors) {
        if (!parseErrors.isEmpty()) {
            // 确保错误列表存在
            if (!result.containsKey("systemErrors")) {
                result.put("systemErrors", new ArrayList<String>());
            }
            if (!result.containsKey("allErrors")) {
                result.put("allErrors", new ArrayList<String>());
            }

            @SuppressWarnings("unchecked")
            List<String> systemErrors = (List<String>) result.get("systemErrors");
            @SuppressWarnings("unchecked")
            List<String> allErrors = (List<String>) result.get("allErrors");

            systemErrors.addAll(parseErrors);
            allErrors.addAll(parseErrors);

            int currentSystemErrors = (int) result.getOrDefault("systemErrorCount", 0);
            result.put("systemErrorCount", currentSystemErrors + parseErrors.size());
            result.put("hasErrors", true);

            // 更新总错误数
            int totalErrors = allErrors.size();
            result.put("validationErrorCount",
                    (int) result.getOrDefault("validationErrorCount", 0) +
                            (totalErrors > 0 ? 1 : 0)); // 至少算一个验证错误
        }
    }

    /**
     * 处理成绩导入的核心逻辑
     */
    private Map<String, Object> processGradeImport(List<Grade> grades) {
        Map<String, Object> result = new HashMap<>();
        List<String> validationErrors = new ArrayList<>();
        List<String> duplicateErrors = new ArrayList<>();
        List<String> nameMismatchErrors = new ArrayList<>();
        List<String> systemErrors = new ArrayList<>();

        int totalCount = grades.size();
        int successInsertCount = 0;
        int successUpdateCount = 0;
        int duplicateCount = 0;
        int nameMismatchCount = 0;
        int autoCreatedCount = 0;
        int validationErrorCount = 0;
        int systemErrorCount = 0;

        System.out.println("=== 开始处理成绩导入 ===");
        System.out.println("总记录数: " + totalCount);

        try {
            for (int i = 0; i < grades.size(); i++) {
                Grade grade = grades.get(i);
                int lineNumber = i + 2;

                try {
                    System.out.println("处理第 " + lineNumber + " 行: 学号=" + grade.getStudentId() +
                            ", 姓名=" + grade.getStudentName() +
                            ", 课程=" + grade.getCourseName() +
                            ", 成绩=" + grade.getScore());

                    // 1. 基本数据验证
                    if (!validateBasicData(grade, lineNumber, validationErrors)) {
                        validationErrorCount++;
                        continue;
                    }

                    // 2. 处理学生信息
                    StudentProcessResult studentResult = processStudentInfo(grade, lineNumber, nameMismatchErrors);
                    if (!studentResult.isSuccess()) {
                        if (studentResult.isNameMismatch()) {
                            nameMismatchCount++;
                        } else {
                            systemErrorCount++;
                            if (studentResult.getMessage() != null) {
                                systemErrors.add(studentResult.getMessage());
                            }
                        }
                        continue;
                    }

                    if (studentResult.isAutoCreated()) {
                        autoCreatedCount++;
                    }

                    // 3. 检查是否完全重复
                    if (isGradeDuplicate(grade)) {
                        duplicateCount++;
                        duplicateErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() +
                                " 的课程 '" + grade.getCourseName() +
                                "' 成绩已存在，跳过导入");
                        continue;
                    }

                    // 4. 检查是否已存在相同学号、课程、学期的记录（需要更新）
                    Grade existingGrade = findExistingGrade(grade);
                    boolean isUpdate = existingGrade != null;

                    // 5. 添加或更新成绩
                    try {
                        boolean success = gradeDAO.addOrUpdateGrade(grade);
                        if (success) {
                            if (isUpdate) {
                                successUpdateCount++;
                                System.out.println("第 " + lineNumber + " 行: 成绩更新成功");
                            } else {
                                successInsertCount++;
                                System.out.println("第 " + lineNumber + " 行: 成绩插入成功");
                            }
                        } else {
                            systemErrorCount++;
                            systemErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() + " 的成绩保存失败");
                        }
                    } catch (Exception e) {
                        systemErrorCount++;
                        systemErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() + " 的成绩保存异常 - " + e.getMessage());
                    }

                } catch (Exception e) {
                    systemErrorCount++;
                    systemErrors.add("第 " + lineNumber + " 行: 处理失败 - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            systemErrorCount++;
            systemErrors.add("批量导入过程中发生错误: " + e.getMessage());
        }

        // 合并所有错误
        List<String> allErrors = new ArrayList<>();
        allErrors.addAll(validationErrors);
        allErrors.addAll(nameMismatchErrors);
        allErrors.addAll(duplicateErrors);
        allErrors.addAll(systemErrors);

        // 输出最终统计信息
        System.out.println("=== 导入完成统计 ===");
        System.out.println("总记录数: " + totalCount);
        System.out.println("成功插入: " + successInsertCount);
        System.out.println("成功更新: " + successUpdateCount);
        System.out.println("重复跳过: " + duplicateCount);
        System.out.println("自动创建学生: " + autoCreatedCount);
        System.out.println("姓名不匹配: " + nameMismatchCount);
        System.out.println("验证错误: " + validationErrorCount);
        System.out.println("系统错误: " + systemErrorCount);

        // 设置返回结果
        result.put("totalCount", totalCount);
        result.put("successInsertCount", successInsertCount);
        result.put("successUpdateCount", successUpdateCount);
        result.put("duplicateCount", duplicateCount);
        result.put("nameMismatchCount", nameMismatchCount);
        result.put("autoCreatedCount", autoCreatedCount);
        result.put("validationErrorCount", validationErrorCount);
        result.put("systemErrorCount", systemErrorCount);

        // 分类错误信息
        result.put("validationErrors", validationErrors);
        result.put("nameMismatchErrors", nameMismatchErrors);
        result.put("duplicateErrors", duplicateErrors);
        result.put("systemErrors", systemErrors);
        result.put("allErrors", allErrors);

        result.put("hasErrors", !allErrors.isEmpty());

        // 生成汇总消息
        int totalSuccess = successInsertCount + successUpdateCount;
        String message = String.format("导入完成: 成功 %d 条 (新增 %d, 更新 %d), 重复 %d 条, 自动创建学生 %d 条",
                totalSuccess, successInsertCount, successUpdateCount, duplicateCount, autoCreatedCount);
        if (nameMismatchCount > 0) {
            message += String.format(", 姓名不一致 %d 条", nameMismatchCount);
        }
        if (!allErrors.isEmpty()) {
            message += String.format(", 错误 %d 条", allErrors.size());
        }
        result.put("message", message);

        return result;
    }

    /**
     * 初始化零结果
     */
    private void initializeZeroResult(Map<String, Object> result) {
        result.put("totalCount", 0);
        result.put("successInsertCount", 0);
        result.put("successUpdateCount", 0);
        result.put("duplicateCount", 0);
        result.put("nameMismatchCount", 0);
        result.put("autoCreatedCount", 0);
        result.put("validationErrorCount", 0);
        result.put("systemErrorCount", 0);
        result.put("validationErrors", new ArrayList<>());
        result.put("nameMismatchErrors", new ArrayList<>());
        result.put("duplicateErrors", new ArrayList<>());
        result.put("systemErrors", new ArrayList<>());
        result.put("allErrors", new ArrayList<>());
    }


    /**
     * 创建错误结果
     */
    private Map<String, Object> createErrorResult(List<String> parseErrors, String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        initializeZeroResult(result);

        List<String> errors = new ArrayList<>();
        errors.addAll(parseErrors);
        errors.add(errorMessage);

        result.put("systemErrors", errors);
        result.put("allErrors", errors);
        result.put("systemErrorCount", errors.size());
        result.put("hasErrors", true);
        result.put("message", "导入失败: " + errorMessage);

        return result;
    }

    /**
     * 查找已存在的成绩记录
     */
    private Grade findExistingGrade(Grade grade) {
        try {
            return gradeDAO.findGradeByStudentCourseSemester(
                    grade.getStudentId(), grade.getCourseName(), grade.getSemester());
        } catch (Exception e) {
            System.err.println("查找现有成绩记录失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 处理学生信息
     */
    private StudentProcessResult processStudentInfo(Grade grade, int lineNumber, List<String> nameMismatchErrors) {
        try {
            String studentId = grade.getStudentId();
            String studentName = grade.getStudentName();

            // 检查学生是否存在
            if (!gradeDAO.studentExists(studentId)) {
                // 学生不存在，自动创建学生
                boolean created = gradeDAO.createStudent(studentId, studentName);
                if (created) {
                    System.out.println("第 " + lineNumber + " 行: 自动创建学生 " + studentId + " - " + studentName);
                    return StudentProcessResult.successWithAutoCreate();
                } else {
                    String errorMsg = "第 " + lineNumber + " 行: 学号 " + studentId + " 的学生创建失败";
                    nameMismatchErrors.add(errorMsg);
                    return StudentProcessResult.failure(studentId, errorMsg);
                }
            } else {
                // 学生存在，验证姓名一致性
                String existingName = gradeDAO.getStudentName(studentId);
                if (existingName != null && !existingName.equals(studentName)) {
                    String errorMsg = "第 " + lineNumber + " 行: 学号 " + studentId + " 的姓名不一致，系统记录: " + existingName + "，文件记录: " + studentName;
                    nameMismatchErrors.add(errorMsg);
                    return StudentProcessResult.nameMismatch(studentId, existingName, studentName);
                }
                return StudentProcessResult.success();
            }
        } catch (Exception e) {
            String errorMsg = "第 " + lineNumber + " 行: 验证学号 " + grade.getStudentId() + " 的学生信息时出错: " + e.getMessage();
            nameMismatchErrors.add(errorMsg);
            return StudentProcessResult.failure(grade.getStudentId(), errorMsg);
        }
    }

    /**
     * 验证基本数据
     */
    private boolean validateBasicData(Grade grade, int lineNumber, List<String> validationErrors) {
        if (grade == null) {
            validationErrors.add("第 " + lineNumber + " 行: 成绩数据为空");
            return false;
        }

        // 验证学号
        if (grade.getStudentId() == null || grade.getStudentId().trim().isEmpty()) {
            validationErrors.add("第 " + lineNumber + " 行: 学号不能为空");
            return false;
        }

        // 验证姓名
        if (grade.getStudentName() == null || grade.getStudentName().trim().isEmpty()) {
            validationErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() + " 的姓名为空");
            return false;
        }

        // 验证课程名称
        if (grade.getCourseName() == null || grade.getCourseName().trim().isEmpty()) {
            validationErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() + " 的课程名称为空");
            return false;
        }

        // 验证成绩范围 (0-100)
        if (grade.getScore() < 0 || grade.getScore() > 100) {
            validationErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() + " 的成绩 " + grade.getScore() + " 无效，必须在0-100之间");
            return false;
        }

        // 验证学期
        if (grade.getSemester() == null || grade.getSemester().trim().isEmpty()) {
            validationErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() + " 的学期为空");
            return false;
        }

        return true;
    }

    /**
     * 检查成绩是否完全重复
     */
    private boolean isGradeDuplicate(Grade grade) {
        try {
            return gradeDAO.isGradeDuplicate(grade.getStudentId(), grade.getCourseName(),
                    grade.getSemester(), grade.getScore());
        } catch (Exception e) {
            System.err.println("检查成绩重复失败: " + e.getMessage());
            return false;
        }
    }
}