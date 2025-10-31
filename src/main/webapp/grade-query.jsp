<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>我的成绩查询</title>
  <link rel="stylesheet" type="text/css" href="css/query_style.css">
  <link rel="stylesheet" type="text/css" href="css/style.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="js/api.js"></script>
  <script src="js/auth.js"></script>
</head>
<body>
<div class="header">
  <h1>学生成绩查询系统</h1>
  <div class="user-info">
    <span id="userWelcome">加载中...</span>
    <a href="#" onclick="logout()">退出登录</a>
  </div>
</div>

<div class="container">
  <div class="page-header">
    <h2 class="page-title">我的成绩</h2>
    <div class="filter-controls">
      <select id="semesterSelect" class="semester-select" onchange="filterBySemester()">
        <option value="">全部学期</option>
        <!-- 学期选项将通过JavaScript动态添加 -->
      </select>
      <button onclick="goBack()" class="btn-back">
        <i class="fas fa-arrow-left"></i> 返回首页
      </button>
    </div>
  </div>

  <div class="grade-section">
    <table class="grade-table">
      <thead>
      <tr>
        <th>课程名称</th>
        <th>成绩</th>
        <th>绩点</th>
        <th>学分</th>
        <th>学期</th>
      </tr>
      </thead>
      <tbody id="gradesTableBody">
      <tr>
        <td colspan="5" class="no-data">
          <i class="fas fa-spinner fa-spin"></i>
          <h3>加载中...</h3>
        </td>
      </tr>
      </tbody>
    </table>
  </div>

  <!-- 统计信息 -->
  <div id="statsSummary" style="display: none;">
    <div class="stats-text">
      <div class="stat-item gpa-stat">
        <span class="stat-label">加权平均绩点：</span>
        <span class="stat-value" id="weightedGpaValue">0.0</span>
      </div>
      <div class="stat-item credit-stat">
        <span class="stat-label">已修总学分：</span>
        <span class="stat-value" id="totalCredits">0</span>
      </div>
      <div class="stat-item courses-stat">
        <span class="stat-label">已修课程：</span>
        <span class="stat-value" id="courseCount">0</span>
      </div>
    </div>
  </div>

  <div id="timeInfo" class="time-info" style="display: none;">
    <p>查询时间限制: <span id="queryPeriod"></span></p>
  </div>
</div>

<script>
  // 页面加载完成后执行
  $(document).ready(function() {
    loadUserInfo();
    loadGrades();
  });

  // 全局变量存储所有成绩数据
  let allGrades = [];
  let currentSemester = '';

  async function loadUserInfo() {
    try {
      const userData = sessionStorage.getItem('userData');
      if (userData) {
        const user = JSON.parse(userData);
        $('#userWelcome').text(`欢迎，${user.name} 同学`);
      }
    } catch (error) {
      console.error('加载用户信息失败:', error);
    }
  }

  // 计算绩点函数
  function calculateGPA(score) {
    if (score < 60) return 0.0;
    return 1.0 + (score - 60) * 0.1;
  }

  // 获取成绩等级样式
  function getGradeClass(score) {
    if (score >= 90) return 'grade-excellent';
    if (score >= 80) return 'grade-good';
    if (score >= 70) return 'grade-average';
    return 'grade-poor';
  }

  // 显示成绩数据
  function displayGrades(grades) {
    const tbody = $('#gradesTableBody');

    if (!grades || grades.length === 0) {
      tbody.html(`
        <tr>
          <td colspan="5" class="no-data">
            <i class="fas fa-inbox"></i>
            <h3>暂无成绩数据</h3>
            <p>您目前还没有任何成绩记录</p>
          </td>
        </tr>
      `);
      return;
    }

    let html = '';
    grades.forEach(grade => {
      const gpa = calculateGPA(grade.score);
      const gradeClass = getGradeClass(grade.score);
      const credit = grade.credit || 0;  // 直接从grade对象获取credit字段

      html += `
        <tr>
            <td><strong>${escapeHtml(grade.courseName || '未知课程')}</strong></td>
            <td><span class="grade-value ${gradeClass}">${grade.score}</span></td>
            <td><span class="gpa-badge">${gpa.toFixed(1)}</span></td>
            <td><span class="credit-badge">${credit}</span></td>
            <td>${escapeHtml(grade.semester || '未知学期')}</td>
        </tr>
    `;
    });

    tbody.html(html);
  }

  // 填充学期选择框
  function populateSemesterSelect(grades) {
    const semesterSelect = $('#semesterSelect');

    // 获取所有不重复的学期
    const semesters = [...new Set(grades.map(grade => grade.semester))].filter(s => s);
    semesters.sort().reverse(); // 按学期倒序排列

    // 清空现有选项（除了"全部学期"）
    semesterSelect.find('option:not(:first)').remove();

    // 添加学期选项
    semesters.forEach(semester => {
      semesterSelect.append(`<option value="${semester}">${semester}</option>`);
    });
  }

  // 计算加权平均绩点和总学分
  function calculateWeightedStats(grades) {
    if (!grades || grades.length === 0) {
      $('#statsSummary').hide();
      return;
    }

    const totalCourses = grades.length;

    // 计算总学分和加权绩点
    let totalCredits = 0;
    let totalWeightedGPA = 0;

    grades.forEach(grade => {
      const credit = grade.credit || 0;
      const gpa = calculateGPA(grade.score);

      totalCredits += credit;
      totalWeightedGPA += credit * gpa;
    });

    // 计算加权平均绩点
    const weightedGPA = totalCredits > 0 ? (totalWeightedGPA / totalCredits).toFixed(2) : 0;

    // 更新统计信息
    $('#weightedGpaValue').text(weightedGPA);
    $('#totalCredits').text(totalCredits);
    $('#courseCount').text(totalCourses);

    $('#statsSummary').show();
  }

  // 按学期筛选成绩
  function filterBySemester() {
    const selectedSemester = $('#semesterSelect').val();
    currentSemester = selectedSemester;

    let filteredGrades = allGrades;
    if (selectedSemester) {
      filteredGrades = allGrades.filter(grade => grade.semester === selectedSemester);
    }

    displayGrades(filteredGrades);
    calculateWeightedStats(filteredGrades);
  }

  // 加载成绩数据
  async function loadGrades() {
    try {
      showLoading();

      // 使用 gradeAPI 加载成绩
      const result = await gradeAPI.getMyGrades();
      allGrades = result.data || [];

      // 如果没有学分信息，需要从课程表获取
      if (allGrades.length > 0 && !allGrades[0].credit) {
        // 这里需要调用API获取课程学分信息
        await loadCourseCredits();
      }

      displayGrades(allGrades);
      populateSemesterSelect(allGrades);
      calculateWeightedStats(allGrades);

      hideLoading();

    } catch (error) {
      console.error('加载成绩失败:', error);
      hideLoading();

      $('#gradesTableBody').html(`
        <tr>
          <td colspan="5" class="no-data">
            <i class="fas fa-exclamation-triangle"></i>
            <h3>加载失败</h3>
            <p>无法获取成绩数据，请稍后重试</p>
            <p style="color: #e53e3e; font-size: 0.9rem; margin-top: 0.5rem;">错误信息: ${error.message}</p>
          </td>
        </tr>
      `);
    }
  }

  // 加载课程学分信息
  async function loadCourseCredits() {
    try {
      // 这里需要调用API获取课程信息
      // 假设有一个API可以获取课程学分信息
      const coursesResult = await gradeAPI.callAPI('api/courses/with-credits');
      const courses = coursesResult.data || [];

      // 创建课程学分映射
      const courseCreditMap = {};
      courses.forEach(course => {
        const key = `${course.courseName}_${course.semester}`;
        courseCreditMap[key] = course.credit;
      });

      // 为成绩数据添加学分信息
      allGrades.forEach(grade => {
        const key = `${grade.courseName}_${grade.semester}`;
        grade.credit = courseCreditMap[key] || 0;
      });

    } catch (error) {
      console.error('加载课程学分失败:', error);
      // 如果无法获取学分，默认设为0
      allGrades.forEach(grade => {
        grade.credit = 0;
      });
    }
  }

  async function logout() {
    try {
      await gradeAPI.logout();
      sessionStorage.removeItem('userData');
      window.location.href = 'login.jsp';
    } catch (error) {
      console.error('退出登录失败:', error);
      sessionStorage.removeItem('userData');
      window.location.href = 'login.jsp';
    }
  }

  function goBack() {
    window.location.href = 'student-dashboard.jsp';
  }

  // 加载动画函数
  function showLoading() {
    let loading = $('#loading');
    if (loading.length === 0) {
      $('body').append(`
        <div id="loading" style="
          position: fixed;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          background: rgba(0, 0, 0, 0.8);
          color: white;
          padding: 20px 30px;
          border-radius: 8px;
          z-index: 9999;
          font-size: 16px;
        ">
          <i class="fas fa-spinner fa-spin"></i> 加载中...
        </div>
      `);
    } else {
      loading.show();
    }
  }

  function hideLoading() {
    $('#loading').hide();
  }

  // HTML转义函数
  function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
  }
</script>
</body>
</html>