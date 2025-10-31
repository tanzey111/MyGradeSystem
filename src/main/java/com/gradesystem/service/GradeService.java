package com.gradesystem.service;

import com.gradesystem.dao.GradeDAO;
import com.gradesystem.dao.TeacherCourseDAO;
import com.gradesystem.model.Grade;
import com.gradesystem.model.StudentProcessResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeService {
    private GradeDAO gradeDAO;
    private TeacherCourseDAO teacherCourseDAO = new TeacherCourseDAO();

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
     * 从CSV文件导入成绩 - 增加教师权限检查
     */
    public Map<String, Object> importGradesFromCSV(String filePath, String teacherId) {
        System.out.println("=== 开始CSV文件导入 ===");
        System.out.println("文件路径: " + filePath);
        System.out.println("教师ID: " + teacherId);

        Map<String, Object> result = new HashMap<>();
        List<String> parseErrors = new ArrayList<>();

        try {
            // 首先检查文件是否存在和是否为空
            File file = new File(filePath);
            if (!file.exists()) {
                return createErrorResult(parseErrors, "文件不存在: " + filePath);
            }

            if (file.length() == 0) {
                return createErrorResult(parseErrors, "CSV文件为空 - 文件大小为0字节");
            }

            // 解析CSV文件
            List<Grade> grades = gradeDAO.parseCSVFile(file, parseErrors);

            System.out.println("解析结果 - 成绩数量: " + grades.size());
            System.out.println("解析结果 - 错误数量: " + parseErrors.size());

            // 如果有解析错误，优先显示解析错误
            if (!parseErrors.isEmpty()) {
                System.out.println("CSV文件解析失败: " + parseErrors);

                // 检查是否是空文件相关的错误
                boolean isEmptyFileError = parseErrors.stream()
                        .anyMatch(error -> error.contains("文件为空") || error.contains("没有有效的成绩数据"));

                if (isEmptyFileError) {
                    return createErrorResult(parseErrors, "CSV文件为空或没有有效数据");
                } else {
                    return createErrorResult(parseErrors, "CSV文件格式错误");
                }
            }

            // 如果解析没有错误但没有数据，也认为是空文件
            if (grades.isEmpty()) {
                parseErrors.add("CSV文件没有包含任何有效的成绩记录");
                return createErrorResult(parseErrors, "CSV文件没有有效数据");
            }

            // 正常处理导入，传入teacherId
            Map<String, Object> importResult = processGradeImport(grades, teacherId);
            result.putAll(importResult);

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
     * 从Excel文件导入成绩 - 增加教师权限检查
     */
    public Map<String, Object> importGradesFromExcel(String filePath, String teacherId) {
        System.out.println("=== 开始Excel文件导入 ===");
        System.out.println("文件路径: " + filePath);
        System.out.println("教师ID: " + teacherId);

        Map<String, Object> result = new HashMap<>();
        List<String> parseErrors = new ArrayList<>();

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return createErrorResult(parseErrors, "文件不存在: " + filePath);
            }

            if (file.length() == 0) {
                return createErrorResult(parseErrors, "Excel文件为空 - 文件大小为0字节");
            }

            List<Grade> grades = gradeDAO.parseExcelFile(file, parseErrors);

            System.out.println("解析结果 - 成绩数量: " + grades.size());
            System.out.println("解析结果 - 错误数量: " + parseErrors.size());

            if (!parseErrors.isEmpty()) {
                System.out.println("Excel文件解析失败: " + parseErrors);
                return createErrorResult(parseErrors, "Excel文件格式错误");
            }

            if (grades.isEmpty()) {
                parseErrors.add("Excel文件没有包含任何有效的成绩记录");
                return createErrorResult(parseErrors, "Excel文件没有有效数据");
            }

            Map<String, Object> importResult = processGradeImport(grades, teacherId);
            result.putAll(importResult);

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
     * 处理成绩导入的核心逻辑（增加教师权限检查和选课验证）
     */
    private Map<String, Object> processGradeImport(List<Grade> grades, String teacherId) {
        Map<String, Object> result = new HashMap<>();
        List<String> validationErrors = new ArrayList<>();
        List<String> duplicateErrors = new ArrayList<>();
        List<String> nameMismatchErrors = new ArrayList<>();
        List<String> systemErrors = new ArrayList<>();
        List<String> permissionErrors = new ArrayList<>();
        List<String> enrollmentErrors = new ArrayList<>();

        int totalCount = grades.size();
        int successInsertCount = 0;
        int successUpdateCount = 0;
        int duplicateCount = 0;
        int nameMismatchCount = 0;
        int autoCreatedCount = 0;
        int validationErrorCount = 0;
        int systemErrorCount = 0;
        int permissionErrorCount = 0;
        int enrollmentErrorCount = 0;

        System.out.println("=== 开始处理成绩导入 ===");
        System.out.println("教师ID: " + teacherId);
        System.out.println("总记录数: " + totalCount);

        try {
            for (int i = 0; i < grades.size(); i++) {
                Grade grade = grades.get(i);
                int lineNumber = i + 2;

                try {
                    System.out.println("处理第 " + lineNumber + " 行: 学号=" + grade.getStudentId() +
                            ", 姓名=" + grade.getStudentName() +
                            ", 课程=" + grade.getCourseName() +
                            ", 学期=" + grade.getSemester() +
                            ", 成绩=" + grade.getScore());

                    // 1. 基本数据验证
                    if (!validateBasicData(grade, lineNumber, validationErrors)) {
                        validationErrorCount++;
                        continue;
                    }

                    // 2. 检查教师是否有权限管理该课程（特定学期）
                    if (!canTeacherManageCourse(teacherId, grade.getCourseName(), grade.getSemester())) {
                        permissionErrorCount++;
                        permissionErrors.add("第 " + lineNumber + " 行: 教师无权在" + grade.getSemester() +
                                "学期管理课程 '" + grade.getCourseName() + "'");
                        continue;
                    }

                    // 3. 检查学生是否选了该教师该学期的这门课
                    if (!isStudentEnrolled(grade.getStudentId(), teacherId, grade.getCourseName(), grade.getSemester())) {
                        enrollmentErrorCount++;
                        enrollmentErrors.add("第 " + lineNumber + " 行: 学生 " + grade.getStudentId() +
                                " 未在" + grade.getSemester() + "学期选修您的" + grade.getCourseName() + "课程");
                        continue;
                    }

                    // 4. 处理学生信息
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

                    // 5. 检查是否完全重复
                    if (isGradeDuplicate(grade)) {
                        duplicateCount++;
                        duplicateErrors.add("第 " + lineNumber + " 行: 学号 " + grade.getStudentId() +
                                " 的课程 '" + grade.getCourseName() +
                                "' 成绩已存在，跳过导入");
                        continue;
                    }

                    // 6. 添加或更新成绩
                    try {
                        boolean success = gradeDAO.addOrUpdateGrade(grade);
                        if (success) {
                            Grade existingGrade = findExistingGrade(grade);
                            if (existingGrade != null) {
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
        allErrors.addAll(permissionErrors);
        allErrors.addAll(enrollmentErrors);

        // 设置返回结果
        result.put("totalCount", totalCount);
        result.put("successInsertCount", successInsertCount);
        result.put("successUpdateCount", successUpdateCount);
        result.put("duplicateCount", duplicateCount);
        result.put("nameMismatchCount", nameMismatchCount);
        result.put("autoCreatedCount", autoCreatedCount);
        result.put("validationErrorCount", validationErrorCount);
        result.put("systemErrorCount", systemErrorCount);
        result.put("permissionErrorCount", permissionErrorCount);
        result.put("enrollmentErrorCount", enrollmentErrorCount);

        // 分类错误信息
        result.put("validationErrors", validationErrors);
        result.put("nameMismatchErrors", nameMismatchErrors);
        result.put("duplicateErrors", duplicateErrors);
        result.put("systemErrors", systemErrors);
        result.put("permissionErrors", permissionErrors);
        result.put("enrollmentErrors", enrollmentErrors);
        result.put("allErrors", allErrors);

        result.put("hasErrors", !allErrors.isEmpty());

        // 生成汇总消息
        int totalSuccess = successInsertCount + successUpdateCount;
        String message = String.format("导入完成: 成功 %d 条 (新增 %d, 更新 %d), 重复 %d 条, 自动创建学生 %d 条",
                totalSuccess, successInsertCount, successUpdateCount, duplicateCount, autoCreatedCount);
        if (nameMismatchCount > 0) {
            message += String.format(", 姓名不一致 %d 条", nameMismatchCount);
        }
        if (permissionErrorCount > 0) {
            message += String.format(", 权限错误 %d 条", permissionErrorCount);
        }
        if (enrollmentErrorCount > 0) {
            message += String.format(", 选课验证错误 %d 条", enrollmentErrorCount);
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
        result.put("permissionErrorCount", 0);  // 新增
        result.put("enrollmentErrorCount", 0);  // 新增

        result.put("validationErrors", new ArrayList<>());
        result.put("nameMismatchErrors", new ArrayList<>());
        result.put("duplicateErrors", new ArrayList<>());
        result.put("systemErrors", new ArrayList<>());
        result.put("permissionErrors", new ArrayList<>());  // 新增
        result.put("enrollmentErrors", new ArrayList<>());  // 新增
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


    /**
     * 获取教师可管理的所有成绩
     */
    public List<Grade> getGradesByTeacher(String teacherId) {
        try {
            List<Map<String, Object>> gradeMaps = teacherCourseDAO.getGradesByTeacher(teacherId);
            List<Grade> grades = new ArrayList<>();

            for (Map<String, Object> gradeMap : gradeMaps) {
                Grade grade = new Grade();
                grade.setId((Integer) gradeMap.get("id"));
                grade.setStudentId((String) gradeMap.get("studentId"));
                grade.setStudentName((String) gradeMap.get("studentName"));
                grade.setCourseName((String) gradeMap.get("courseName"));
                grade.setScore((Double) gradeMap.get("score"));
                grade.setSemester((String) gradeMap.get("semester"));
                grade.setCreatedAt((java.util.Date) gradeMap.get("createdAt"));
                grades.add(grade);
            }
            return grades;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取教师成绩失败: " + e.getMessage());
        }
    }


    /**
     * 检查教师是否有权限管理成绩
     */
    public boolean canTeacherManageGrade(String teacherId, int gradeId) {
        try {
            return teacherCourseDAO.canTeacherManageGrade(teacherId, gradeId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查教师是否有权限管理课程
     */
    public boolean canTeacherManageCourse(String teacherId, String courseName) {
        try {
            return teacherCourseDAO.canTeacherManageCourse(teacherId, courseName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查教师是否有权限管理课程（特定学期）
     */
    public boolean canTeacherManageCourse(String teacherId, String courseName, String semester) {
        try {
            return teacherCourseDAO.canTeacherManageCourse(teacherId, courseName, semester);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查学生是否选了该教师该学期的这门课
     */
    public boolean isStudentEnrolled(String studentId, String teacherId, String courseName, String semester) {
        try {
            return teacherCourseDAO.isStudentEnrolled(studentId, teacherId, courseName, semester);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取教师所教的课程名称列表
     */
    public List<String> getTeacherCourses(String teacherId) {
        try {
            return teacherCourseDAO.getCourseNamesByTeacher(teacherId);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 添加或更新成绩（增加教师权限检查和选课验证）
     */
    public boolean addOrUpdateGrade(Grade grade, String teacherId) {
        try {
            // 验证成绩数据
            if (!validateGrade(grade)) {
                return false;
            }

            // 检查教师是否有权限管理该课程（特定学期）
            if (!canTeacherManageCourse(teacherId, grade.getCourseName(), grade.getSemester())) {
                throw new RuntimeException("您没有权限在" + grade.getSemester() + "学期管理课程: " + grade.getCourseName());
            }

            // 检查学生是否选了该教师该学期的这门课
            if (!isStudentEnrolled(grade.getStudentId(), teacherId, grade.getCourseName(), grade.getSemester())) {
                throw new RuntimeException("学生 " + grade.getStudentId() + " 未在" + grade.getSemester() +
                        "学期选修您的" + grade.getCourseName() + "课程");
            }

            // 检查学生是否存在，如果不存在则自动创建
            if (!gradeDAO.studentExists(grade.getStudentId())) {
                boolean created = gradeDAO.createStudent(grade.getStudentId(), grade.getStudentName());
                if (!created) {
                    throw new RuntimeException("学生不存在且创建失败: " + grade.getStudentId());
                }
            } else {
                // 学生存在，验证姓名一致性
                String existingName = gradeDAO.getStudentName(grade.getStudentId());
                if (existingName != null && !existingName.equals(grade.getStudentName())) {
                    throw new RuntimeException("学生姓名不一致: 系统记录 '" + existingName + "', 输入记录 '" + grade.getStudentName() + "'");
                }
            }

            // 添加或更新成绩
            return gradeDAO.addOrUpdateGrade(grade);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("保存成绩失败: " + e.getMessage());
        }
    }



    /**
     * 获取学生成绩（包含学分信息）
     */
    public List<Map<String, Object>> getStudentGradesWithCredits(String studentId) {
        try {
            return gradeDAO.getGradesWithCreditsByStudentId(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取学生成绩失败: " + e.getMessage());
        }
    }


}