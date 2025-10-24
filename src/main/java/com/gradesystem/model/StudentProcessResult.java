package com.gradesystem.model;

/**
 * 学生处理结果类
 * 用于封装学生信息处理的结果状态
 */
public class StudentProcessResult {
    private boolean success;        // 处理是否成功
    private boolean autoCreated;    // 是否自动创建了学生
    private boolean nameMismatch;   // 是否存在姓名不匹配
    private String studentId;       // 学生学号
    private String studentName;     // 学生姓名
    private String message;         // 处理消息

    // 默认构造函数
    public StudentProcessResult() {}

    // 简化构造函数
    public StudentProcessResult(boolean success, boolean autoCreated, boolean nameMismatch) {
        this.success = success;
        this.autoCreated = autoCreated;
        this.nameMismatch = nameMismatch;
    }

    // 完整构造函数
    public StudentProcessResult(boolean success, boolean autoCreated, boolean nameMismatch,
                                String studentId, String studentName, String message) {
        this.success = success;
        this.autoCreated = autoCreated;
        this.nameMismatch = nameMismatch;
        this.studentId = studentId;
        this.studentName = studentName;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isAutoCreated() {
        return autoCreated;
    }

    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    public boolean isNameMismatch() {
        return nameMismatch;
    }

    public void setNameMismatch(boolean nameMismatch) {
        this.nameMismatch = nameMismatch;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // 便捷的静态工厂方法
    public static StudentProcessResult success() {
        return new StudentProcessResult(true, false, false);
    }

    public static StudentProcessResult successWithAutoCreate() {
        return new StudentProcessResult(true, true, false);
    }

    public static StudentProcessResult nameMismatch(String studentId, String systemName, String fileName) {
        return new StudentProcessResult(false, false, true, studentId, systemName,
                "姓名不一致: 系统记录 '" + systemName + "', 文件记录 '" + fileName + "'");
    }

    public static StudentProcessResult failure(String studentId, String message) {
        StudentProcessResult result = new StudentProcessResult();
        result.setSuccess(false);
        result.setStudentId(studentId);
        result.setMessage(message);
        return result;
    }

    @Override
    public String toString() {
        return "StudentProcessResult{" +
                "success=" + success +
                ", autoCreated=" + autoCreated +
                ", nameMismatch=" + nameMismatch +
                ", studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}