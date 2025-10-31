package com.gradesystem.dao;

import com.gradesystem.model.Grade;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeDAO {
    /**
     * 根据学生ID查询成绩
     */
    public List<Grade> getGradesByStudentId(String studentId) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT id, student_id, student_name, course_name, score, semester, created_at " +
                "FROM grades WHERE student_id = ? ORDER BY semester DESC, course_name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Grade grade = new Grade();
                grade.setId(rs.getInt("id"));
                grade.setStudentId(rs.getString("student_id"));
                grade.setStudentName(rs.getString("student_name"));  // 新增
                grade.setCourseName(rs.getString("course_name"));
                grade.setScore(rs.getDouble("score"));
                grade.setSemester(rs.getString("semester"));
                grade.setCreatedAt(rs.getTimestamp("created_at"));
                grades.add(grade);
            }
        }
        return grades;
    }

    public Grade getGradeById(int gradeId) throws SQLException {
        String sql = "SELECT id, student_id, student_name, course_name, score, semester, created_at, updated_at " +
                "FROM grades WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gradeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Grade grade = new Grade();
                grade.setId(rs.getInt("id"));
                grade.setStudentId(rs.getString("student_id"));
                grade.setStudentName(rs.getString("student_name"));  // 新增
                grade.setCourseName(rs.getString("course_name"));
                grade.setScore(rs.getDouble("score"));
                grade.setSemester(rs.getString("semester"));
                grade.setCreatedAt(rs.getTimestamp("created_at"));
                return grade;
            }
            return null;
        }
    }

    /**
     * 查询所有成绩（教师权限）
     */
    public List<Grade> getAllGrades() throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT id, student_id, student_name, course_name, score, semester, created_at " +
                "FROM grades ORDER BY student_id, semester DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Grade grade = new Grade();
                grade.setId(rs.getInt("id"));
                grade.setStudentId(rs.getString("student_id"));
                grade.setStudentName(rs.getString("student_name"));  // 新增
                grade.setCourseName(rs.getString("course_name"));
                grade.setScore(rs.getDouble("score"));
                grade.setSemester(rs.getString("semester"));
                grade.setCreatedAt(rs.getTimestamp("created_at"));
                grades.add(grade);
            }
        }
        return grades;
    }


    /**
     * 添加或更新成绩 - 修复返回值问题
     */
    public boolean addOrUpdateGrade(Grade grade) throws SQLException {
        String sql = "INSERT INTO grades (student_id, student_name, course_name, score, semester) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score = VALUES(score), student_name = VALUES(student_name), updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, grade.getStudentId());
            pstmt.setString(2, grade.getStudentName());
            pstmt.setString(3, grade.getCourseName());
            pstmt.setDouble(4, grade.getScore());
            pstmt.setString(5, grade.getSemester());

            int affectedRows = pstmt.executeUpdate();
            // 对于 INSERT ... ON DUPLICATE KEY UPDATE:
            // - 插入新记录时返回 1
            // - 更新已存在记录时返回 2
            // 所以只要 affectedRows > 0 就表示成功
            return affectedRows > 0;
        }
    }

    /**
     * 根据ID更新成绩
     */
    public boolean updateGradeById(int gradeId, double score, String semester) throws SQLException {
        String sql = "UPDATE grades SET score = ?, semester = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, score);
            pstmt.setString(2, semester);
            pstmt.setInt(3, gradeId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 检查学生是否存在
     */
    public boolean studentExists(String studentId) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }


    /**
     * 删除成绩
     */
    public boolean deleteGrade(int gradeId) throws SQLException {
        String sql = "DELETE FROM grades WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gradeId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 获取查询时间段配置
     */
    public Map<String, Object> getQueryPeriodConfig() throws SQLException {
        Map<String, Object> config = new HashMap<>();
        String sql = "SELECT start_time, end_time, is_active FROM query_periods WHERE id = 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                config.put("start_time", rs.getLong("start_time"));
                config.put("end_time", rs.getLong("end_time"));
                config.put("is_active", rs.getBoolean("is_active"));

                // 处理数据库中的NULL值
                if (rs.wasNull()) {
                    config.put("start_time", null);
                    config.put("end_time", null);
                }
            }
        }
        return config;
    }

    /**
     * 设置查询时间段配置
     */
    public boolean setQueryPeriodConfig(Long startTime, Long endTime, Boolean isActive) throws SQLException {
        String sql = "INSERT INTO query_periods (id, start_time, end_time, is_active) " +
                "VALUES (1, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE start_time = ?, end_time = ?, is_active = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 使用 setNull 而不是转换为 0
            if (startTime != null) {
                pstmt.setLong(1, startTime);
                pstmt.setLong(4, startTime);
            } else {
                pstmt.setNull(1, java.sql.Types.BIGINT);
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }

            if (endTime != null) {
                pstmt.setLong(2, endTime);
                pstmt.setLong(5, endTime);
            } else {
                pstmt.setNull(2, java.sql.Types.BIGINT);
                pstmt.setNull(5, java.sql.Types.BIGINT);
            }

            pstmt.setBoolean(3, isActive != null ? isActive : false);
            pstmt.setBoolean(6, isActive != null ? isActive : false);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 获取学生成绩统计
     */
    public Map<String, Object> getStudentGradeStats(String studentId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT " +
                "COUNT(*) as total_courses, " +
                "AVG(score) as average_score, " +
                "MAX(score) as highest_score, " +
                "MIN(score) as lowest_score, " +
                "SUM(CASE WHEN score >= 60 THEN 1 ELSE 0 END) as passed_courses " +
                "FROM grades WHERE student_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                stats.put("totalCourses", rs.getInt("total_courses"));
                stats.put("averageScore", rs.getDouble("average_score"));
                stats.put("highestScore", rs.getDouble("highest_score"));
                stats.put("lowestScore", rs.getDouble("lowest_score"));
                stats.put("passedCourses", rs.getInt("passed_courses"));

                int total = rs.getInt("total_courses");
                int passed = rs.getInt("passed_courses");
                double passRate = total > 0 ? (passed * 100.0 / total) : 0;
                stats.put("passRate", Math.round(passRate * 100.0) / 100.0);
            }
        }
        return stats;
    }

    /**
     * 根据课程名称搜索成绩
     */
    public List<Grade> searchGradesByCourse(String courseName) throws SQLException {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT id, student_id, course_name, score, semester, created_at " +
                "FROM grades WHERE course_name LIKE ? ORDER BY student_id, score DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + courseName + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Grade grade = new Grade();
                grade.setId(rs.getInt("id"));
                grade.setStudentId(rs.getString("student_id"));
                grade.setCourseName(rs.getString("course_name"));
                grade.setScore(rs.getDouble("score"));
                grade.setSemester(rs.getString("semester"));
                grade.setCreatedAt(rs.getTimestamp("created_at"));
                grades.add(grade);
            }
        }
        return grades;
    }


    /**
     * 解析CSV文件 - 使用OpenCSV库
     */
    public List<Grade> parseCSVFile(File file, List<String> errors) {
        List<Grade> grades = new ArrayList<>();

        try (FileReader fileReader = new FileReader(file);
             CSVReader csvReader = new CSVReaderBuilder(fileReader).build()) {

            List<String[]> allData = csvReader.readAll();

            if (allData.isEmpty()) {
                errors.add("CSV文件为空");
                return grades;
            }

            // 检查表头（第一行）
            String[] headers = allData.get(0);
            if (!isValidCSVHeader(headers)) {
                errors.add("CSV文件格式不正确，需要的列：学号,姓名,课程名称,成绩,学期");
                return grades;
            }

            // 从第二行开始解析数据
            for (int i = 1; i < allData.size(); i++) {
                String[] row = allData.get(i);

                // 跳过空行
                if (row.length == 0 || (row.length == 1 && row[0].trim().isEmpty())) {
                    continue;
                }

                try {
                    Grade grade = parseCSVRow(row, i + 1);
                    if (grade != null) {
                        grades.add(grade);
                    }
                } catch (Exception e) {
                    errors.add("第 " + (i + 1) + " 行数据格式错误: " + e.getMessage());
                }
            }

        } catch (IOException | CsvException e) {
            errors.add("读取CSV文件失败: " + e.getMessage());
        }
        return grades;
    }

    /**
     * 解析Excel文件 - 使用Apache POI库
     */
    public List<Grade> parseExcelFile(File file, List<String> errors) {
        List<Grade> grades = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;

            // 根据文件扩展名创建不同的Workbook
            if (file.getName().toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = WorkbookFactory.create(fis);
            }

            // 获取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                errors.add("Excel文件为空或只有表头");
                return grades;
            }

            // 获取表头行
            Row headerRow = sheet.getRow(0);
            if (!isValidExcelHeader(headerRow)) {
                errors.add("Excel文件格式不正确，需要的列：学号、姓名、课程名称、成绩、学期");
                return grades;
            }

            // 创建列索引映射
            Map<String, Integer> columnIndexMap = createColumnIndexMap(headerRow);

            // 遍历数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // 跳过空行
                }

                try {
                    Grade grade = parseExcelRow(row, columnIndexMap, i + 1);
                    if (grade != null) {
                        grades.add(grade);
                    }
                } catch (Exception e) {
                    errors.add("第 " + (i + 1) + " 行数据格式错误: " + e.getMessage());
                }
            }

            workbook.close();

        } catch (IOException e) {
            errors.add("读取Excel文件失败: " + e.getMessage());
        } catch (Exception e) {
            errors.add("解析Excel文件失败: " + e.getMessage());
        }

        return grades;
    }

    /**
     * 验证CSV文件表头
     */
    private boolean isValidCSVHeader(String[] headers) {
        if (headers.length < 4) return false;

        // 检查必要的列是否存在（不区分大小写）
        String headerStr = String.join(",", headers).toLowerCase();
        return headerStr.contains("学号") && headerStr.contains("姓名") &&
                headerStr.contains("课程") && headerStr.contains("成绩");
    }

    /**
     * 解析CSV数据行
     */
    private Grade parseCSVRow(String[] row, int lineNumber) {
        if (row.length < 4) {  // 现在需要4列：学号、姓名、课程名称、成绩
            throw new IllegalArgumentException("数据列不足，需要学号、学生姓名、课程名称、成绩");
        }

        String studentId = row[0].trim();
        String studentName = row[1].trim();
        String courseName = row[2].trim();
        String scoreStr = row[3].trim();
        String semester = row.length > 4 ? row[4].trim() : "2024-2025-1";

        // 数据验证
        if (studentId.isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (studentName.isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (courseName.isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (scoreStr.isEmpty()) {
            throw new IllegalArgumentException("成绩不能为空");
        }

        // 解析成绩
        double score;
        try {
            score = Double.parseDouble(scoreStr);
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("成绩必须在 0-100 之间");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("成绩格式不正确，必须是数字");
        }

        // 如果学期为空，使用默认值
        if (semester.isEmpty()) {
            semester = "2024-2025-1";
        }

        return new Grade(studentId, studentName, courseName, score, semester);
    }

    /**
     * 验证Excel文件表头
     */
    private boolean isValidExcelHeader(Row headerRow) {
        if (headerRow == null) return false;

        boolean hasStudentId = false;
        boolean hasStudentName = false;
        boolean hasCourseName = false;
        boolean hasScore = false;

        for (Cell cell : headerRow) {
            String cellValue = getCellValueAsString(cell).toLowerCase();
            if (cellValue.contains("学号")) hasStudentId = true;
            if (cellValue.contains("姓名")) hasStudentName = true;
            if (cellValue.contains("课程")) hasCourseName = true;
            if (cellValue.contains("成绩")) hasScore = true;
        }

        return hasStudentId && hasStudentName && hasCourseName && hasScore;
    }

    /**
     * 创建列索引映射
     */
    private Map<String, Integer> createColumnIndexMap(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            String cellValue = getCellValueAsString(cell).toLowerCase();

            if (cellValue.contains("学号")) {
                columnMap.put("studentId", i);
            } else if (cellValue.contains("姓名")) {
                columnMap.put("studentName", i);  // 新增
            } else if (cellValue.contains("课程")) {
                columnMap.put("courseName", i);
            } else if (cellValue.contains("成绩")) {
                columnMap.put("score", i);
            } else if (cellValue.contains("学期")) {
                columnMap.put("semester", i);
            }
        }

        return columnMap;
    }

    /**
     * 解析Excel数据行
     */
    private Grade parseExcelRow(Row row, Map<String, Integer> columnIndexMap, int lineNumber) {
        // 获取各列的索引
        Integer studentIdIndex = columnIndexMap.get("studentId");
        Integer studentNameIndex = columnIndexMap.get("studentName");
        Integer courseNameIndex = columnIndexMap.get("courseName");
        Integer scoreIndex = columnIndexMap.get("score");
        Integer semesterIndex = columnIndexMap.get("semester");

        if (studentIdIndex == null || studentNameIndex == null || courseNameIndex == null || scoreIndex == null) {
            throw new IllegalArgumentException("缺少必要的列：学号、姓名、课程名称、成绩");
        }

        // 读取单元格数据
        String studentId = getCellValueAsString(row.getCell(studentIdIndex)).trim();
        String studentName = getCellValueAsString(row.getCell(studentNameIndex)).trim();  // 新增
        String courseName = getCellValueAsString(row.getCell(courseNameIndex)).trim();
        String scoreStr = getCellValueAsString(row.getCell(scoreIndex)).trim();
        String semester = semesterIndex != null ?
                getCellValueAsString(row.getCell(semesterIndex)).trim() : "2024-2025-1";

        // 数据验证
        if (studentId.isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (studentName.isEmpty()) {  // 新增
            throw new IllegalArgumentException("学生姓名不能为空");
        }
        if (courseName.isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (scoreStr.isEmpty()) {
            throw new IllegalArgumentException("成绩不能为空");
        }

        // 解析成绩
        double score;
        try {
            score = Double.parseDouble(scoreStr);
            if (score < 0 || score > 100) {
                throw new IllegalArgumentException("成绩必须在 0-100 之间");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("成绩格式不正确，必须是数字");
        }

        // 如果学期为空，使用默认值
        if (semester.isEmpty()) {
            semester = "2024-2025-1";
        }

        return new Grade(studentId, studentName, courseName, score, semester);
    }

    /**
     * 通用方法：获取单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法和多余的.0
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((int) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    /**
     * 检查成绩是否完全重复
     */
    public boolean isGradeDuplicate(String studentId, String courseName, String semester, double score) throws SQLException {
        String sql = "SELECT COUNT(*) FROM grades WHERE student_id = ? AND course_name = ? AND semester = ? AND score = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, courseName);
            pstmt.setString(3, semester);
            pstmt.setDouble(4, score);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * 获取学生姓名
     */
    public String getStudentName(String studentId) throws SQLException {
        String sql = "SELECT name FROM students WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
            return null;
        }
    }

    /**
     * 创建学生
     */
    public boolean createStudent(String studentId, String studentName) throws SQLException {
        String sql = "INSERT INTO students (id, name, class, password, status) VALUES (?, ?, '未知班级', ?, 'active')";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, studentName);
            pstmt.setString(3, studentId); // 使用学号作为默认密码

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * 根据学号、课程、学期查找成绩
     */
    public Grade findGradeByStudentCourseSemester(String studentId, String courseName, String semester) throws SQLException {
        String sql = "SELECT id, student_id, student_name, course_name, score, semester " +
                "FROM grades WHERE student_id = ? AND course_name = ? AND semester = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            pstmt.setString(2, courseName);
            pstmt.setString(3, semester);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Grade grade = new Grade();
                grade.setId(rs.getInt("id"));
                grade.setStudentId(rs.getString("student_id"));
                grade.setStudentName(rs.getString("student_name"));
                grade.setCourseName(rs.getString("course_name"));
                grade.setScore(rs.getDouble("score"));
                grade.setSemester(rs.getString("semester"));
                return grade;
            }
            return null;
        }
    }

    /**
     * 根据学生ID查询成绩（包含学分信息）
     */
    public List<Map<String, Object>> getGradesWithCreditsByStudentId(String studentId) throws SQLException {
        List<Map<String, Object>> grades = new ArrayList<>();

        // 使用JOIN查询，直接从courses表获取学分
        String sql = "SELECT g.id, g.student_id, g.student_name, g.course_name, g.score, g.semester, g.created_at, " +
                "COALESCE(c.credit, 0) as credit " +  // 如果没有找到课程，学分设为0
                "FROM grades g " +
                "LEFT JOIN courses c ON g.course_name = c.course_name AND g.semester = c.semester " +
                "WHERE g.student_id = ? " +
                "ORDER BY g.semester DESC, g.course_name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> grade = new HashMap<>();
                grade.put("id", rs.getInt("id"));
                grade.put("studentId", rs.getString("student_id"));
                grade.put("studentName", rs.getString("student_name"));
                grade.put("courseName", rs.getString("course_name"));
                grade.put("score", rs.getDouble("score"));
                grade.put("semester", rs.getString("semester"));
                grade.put("createdAt", rs.getTimestamp("created_at"));
                grade.put("credit", rs.getInt("credit"));  // 学分信息
                grades.add(grade);
            }
        }
        return grades;
    }

}