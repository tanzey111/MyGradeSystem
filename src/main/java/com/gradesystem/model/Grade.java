package com.gradesystem.model;

import java.util.Date;

public class Grade {
    private int id;
    private String studentId;    // 学号
    private String studentName;  // 新增：学生姓名
    private String courseName;   // 课程名称
    private double score;        // 分数
    private String semester;     // 学期
    private Date createdAt;

    public Grade() {}

    public Grade(String studentId, String studentName, String courseName, double score, String semester) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseName = courseName;
        this.score = score;
        this.semester = semester;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}