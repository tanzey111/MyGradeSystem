<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>我的成绩查询</title>
  <link rel="stylesheet" type="text/css" href="css/style.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="js/api.js"></script>
  <script src="js/auth.js"></script>
  <script src="js/grade.js"></script>
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
  <h2>我的成绩</h2>

  <div class="controls">
    <button onclick="refreshGrades()" class="btn-refresh">刷新成绩</button>
    <button onclick="goBack()" class="btn-back">返回首页</button>
  </div>

  <div class="grade-section">
    <table class="grade-table">
      <thead>
      <tr>
        <th>课程名称</th>
        <th>成绩</th>
        <th>学期</th>
      </tr>
      </thead>
      <tbody id="gradesTableBody">
      <tr>
        <td colspan="3" class="no-data">加载中...</td>
      </tr>
      </tbody>
    </table>
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

  async function loadUserInfo() {
    try {
      // 这里可以从sessionStorage获取用户信息
      const userData = sessionStorage.getItem('userData');
      if (userData) {
        const user = JSON.parse(userData);
        $('#userWelcome').text(`欢迎，${user.name} 同学`);
      }
    } catch (error) {
      console.error('加载用户信息失败:', error);
    }
  }

  async function loadGrades() {
    await gradeManager.loadMyGrades();
  }

  async function refreshGrades() {
    await loadGrades();
  }

  async function logout() {
    await authManager.logout();
  }

  function goBack() {
    window.location.href = 'student-dashboard.jsp';
  }
</script>
</body>
</html>