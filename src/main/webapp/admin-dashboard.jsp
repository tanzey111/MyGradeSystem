<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>管理员后台</title>
  <link rel="stylesheet" type="text/css" href="css/style.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="js/api.js"></script>
  <script src="js/auth.js"></script>
  <style>
    /* 专门针对管理员页面的复选框样式 */
    .admin-checkbox-group {
      margin: 15px 0;
    }

    .admin-checkbox-label {
      display: inline-flex !important;
      align-items: center;
      gap: 8px;
      margin-bottom: 0 !important;
      font-weight: normal;
      cursor: pointer;
      user-select: none;
    }

    .admin-checkbox-label input[type="checkbox"] {
      margin: 0;
      width: 16px;
      height: 16px;
    }

    /* 悬停效果 */
    .admin-checkbox-label:hover {
      color: #2980b9;
    }

    .admin-checkbox-label:hover input[type="checkbox"] {
      border-color: #2980b9;
    }
  </style>
</head>
<body>
<div class="header">
  <h1>学生成绩查询系统 - 管理员后台</h1>
  <div class="user-info">
    <span id="userWelcome">管理员</span> |
    <a href="#" onclick="logout()">退出登录</a>
  </div>
</div>

<div class="container">
  <div class="admin-nav">
    <button class="nav-btn active" onclick="showSection('student-management')">学生管理</button>
    <button class="nav-btn" onclick="showSection('system-config')">系统配置</button>
  </div>

  <!-- 学生管理部分 -->
  <div id="student-management" class="admin-section">
    <h2>学生管理</h2>
    <br>
    <div class="controls">
      <input type="text" id="searchStudent" placeholder="搜索学号、姓名或班级..."
             onkeyup="searchStudents()">
      <button onclick="showAddStudentForm()" class="btn-add">添加学生</button>
      <button onclick="loadStudents()" class="btn-refresh">刷新</button>
    </div>

    <table class="data-table">
      <thead>
      <tr>
        <th>学号</th>
        <th>姓名</th>
        <th>班级</th>
        <th>邮箱</th>
        <th>状态</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody id="studentsTableBody">
      <tr>
        <td colspan="6">加载中...</td>
      </tr>
      </tbody>
    </table>
  </div>

  <!-- 系统配置部分 -->
  <div id="system-config" class="admin-section" style="display: none;">
    <h2>系统配置</h2>
    <br>
    <div class="config-form">
      <h3>成绩查询时间设置</h3>
      <form id="timeConfigForm">
        <div class="form-group">
          <label>开始时间:</label>
          <input type="datetime-local" id="startTime" name="startTime">
        </div>
        <div class="form-group">
          <label>结束时间:</label>
          <input type="datetime-local" id="endTime" name="endTime">
        </div>
        <div class="form-group admin-checkbox-group">
          <label class="admin-checkbox-label">
            <input type="checkbox" id="isActive" name="isActive"> 启用时间限制
          </label>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-save">保存配置</button>
          <button type="button" onclick="clearTimeRestrictions()" class="btn-clear">清除时间限制</button>
        </div>
      </form>

      <!-- 当前状态显示 -->
      <div class="config-status" style="margin-top: 20px; padding: 10px; background: #f5f5f5; border-radius: 4px;">
        <h4>当前状态</h4>
        <p id="currentConfigStatus">加载中...</p>
      </div>
    </div>
  </div>
</div>

<!-- 添加学生模态框 -->
<div id="addStudentModal" class="modal" style="display: none;">
  <div class="modal-content">
    <h3>添加学生</h3>
    <form id="addStudentForm">
      <div class="form-group">
        <label>学号:</label>
        <input type="text" name="id" required>
      </div>
      <div class="form-group">
        <label>姓名:</label>
        <input type="text" name="name" required>
      </div>
      <div class="form-group">
        <label>班级:</label>
        <input type="text" name="class" required>
      </div>
      <div class="form-group">
        <label>邮箱:</label>
        <input type="email" name="email">
      </div>
      <div class="form-group">
        <label>电话:</label>
        <input type="text" name="phone">
      </div>
      <div class="form-group">
        <label>初始密码:</label>
        <input type="password" name="password" value="123456">
      </div>
      <div class="form-actions">
        <button type="submit" class="btn-primary">添加</button>
        <button type="button" onclick="hideAddModal()" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</div>
<!-- 编辑学生模态框 -->
<div id="editStudentModal" class="modal" style="display: none;">
  <div class="modal-content">
    <h3>编辑学生信息</h3>
    <form id="editStudentForm">
      <input type="hidden" id="editStudentId" name="id">

      <div class="form-group">
        <label>学号:</label>
        <input type="text" id="editStudentIdDisplay" disabled style="background: #f5f5f5;">
        <small style="color: #666; display: block; margin-top: 5px;">学号不可修改</small>
      </div>

      <div class="form-group">
        <label>姓名:</label>
        <input type="text" id="editStudentName" name="name" required>
      </div>

      <div class="form-group">
        <label>班级:</label>
        <input type="text" id="editStudentClass" name="class" required>
      </div>

      <div class="form-group">
        <label>邮箱:</label>
        <input type="email" id="editStudentEmail" name="email">
      </div>

      <div class="form-group">
        <label>电话:</label>
        <input type="text" id="editStudentPhone" name="phone">
      </div>

      <div class="form-group">
        <label>状态:</label>
        <select id="editStudentStatus" name="status" required>
          <option value="active">正常</option>
          <option value="inactive">禁用</option>
        </select>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary">保存修改</button>
        <button type="button" onclick="hideEditModal()" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</div>

<script>
  // 管理员功能JavaScript
  $(document).ready(function() {
    loadStudents();
    loadSystemConfig();

    // 表单提交处理
    $('#addStudentForm').on('submit', handleAddStudent);
    $('#timeConfigForm').on('submit', handleSaveConfig);
  });

  function showSection(sectionId) {
    $('.admin-section').hide();
    $('#' + sectionId).show();
    $('.nav-btn').removeClass('active');
    event.target.classList.add('active');
  }

  // 搜索学生功能
  function searchStudents() {
    const searchTerm = $('#searchStudent').val().toLowerCase().trim();
    const rows = $('#studentsTableBody tr');

    // 如果没有搜索词，显示所有行
    if (!searchTerm) {
      rows.show();
      return;
    }

    rows.each(function() {
      const studentId = $(this).find('td:eq(0)').text().toLowerCase();
      const studentName = $(this).find('td:eq(1)').text().toLowerCase();
      const studentClass = $(this).find('td:eq(2)').text().toLowerCase();

      // 搜索学号、姓名或班级
      if (studentId.includes(searchTerm) ||
              studentName.includes(searchTerm) ||
              studentClass.includes(searchTerm)) {
        $(this).show();
      } else {
        $(this).hide();
      }
    });
  }


  async function loadStudents() {
    try {
      const result = await gradeAPI.callAPI('api/admin/students');
      renderStudentsTable(result.data);
    } catch (error) {
      alert('加载学生列表失败: ' + error.message);
    }
  }

  function renderStudentsTable(students) {
    const tbody = $('#studentsTableBody');

    if (students.length === 0) {
      tbody.html('<tr><td colspan="6">暂无学生数据</td></tr>');
      return;
    }

    tbody.html(students.map(student => `
                <tr>
                    <td>${student.id}</td>
                    <td>${student.name}</td>
                    <td>${student.class}</td>
                    <td>${student.email || '-'}</td>
                    <td>${student.status == 'active' ? '正常' : '禁用'}</td>
                    <td>
                        <button onclick="editStudent('${student.id}')" class="btn-edit">编辑</button>
                        <button onclick="deleteStudent('${student.id}')" class="btn-danger">删除</button>
                    </td>
                </tr>
            `).join(''));
  }

  // 模态框管理函数
  function showModal(modalId) {
    $('#' + modalId).show();
  }

  function hideModal(modalId) {
    $('#' + modalId).hide();
  }

  // 学生管理相关函数
  function showAddStudentForm() {
    showModal('addStudentModal');
  }

  function hideAddModal() {
    hideModal('addStudentModal');
  }

  function showEditStudentForm() {
    showModal('editStudentModal');
  }

  function hideEditModal() {
    hideModal('editStudentModal');
  }

  async function handleAddStudent(e) {
    e.preventDefault();

    const formData = {
      id: $('input[name="id"]').val(),
      name: $('input[name="name"]').val(),
      class: $('input[name="class"]').val(),
      email: $('input[name="email"]').val(),
      phone: $('input[name="phone"]').val(),
      password: $('input[name="password"]').val()
    };

    try {
      await gradeAPI.callAPI('api/admin/students', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      alert('学生添加成功');
      hideModal();
      loadStudents();
      $('#addStudentForm')[0].reset();

    } catch (error) {
      alert('添加学生失败: ' + error.message);
    }
  }

  async function deleteStudent(studentId) {
    if (!confirm(`确定要删除学生 ${studentId} 吗？`)) {
      return;
    }

    try {
      await gradeAPI.callAPI(`api/admin/students/${studentId}`, {
        method: 'DELETE'
      });

      alert('学生删除成功');
      loadStudents();
    } catch (error) {
      alert('删除学生失败: ' + error.message);
    }
  }

  // 编辑学生功能
  async function editStudent(studentId) {
    try {
      console.log('正在获取学生信息:', studentId);

      const result = await gradeAPI.callAPI(`api/admin/students/${studentId}`);
      console.log('获取到的学生信息:', result);

      const student = result.data;

      // 填充编辑表单
      $('#editStudentId').val(student.id);
      $('#editStudentIdDisplay').val(student.id);
      $('#editStudentName').val(student.name);
      $('#editStudentClass').val(student.class);
      $('#editStudentEmail').val(student.email || '');
      $('#editStudentPhone').val(student.phone || '');
      $('#editStudentStatus').val(student.status || 'active');

      // 显示编辑模态框
      $('#editStudentModal').show();

    } catch (error) {
      console.error('编辑学生错误详情:', error);
      alert('获取学生信息失败: ' + error.message);
    }
  }

  // 隐藏编辑模态框
  function hideEditModal() {
    $('#editStudentModal').hide();
  }

  // 处理编辑表单提交
  async function handleEditStudent(e) {
    e.preventDefault();

    const formData = {
      name: $('#editStudentName').val(),
      class: $('#editStudentClass').val(),
      email: $('#editStudentEmail').val(),
      phone: $('#editStudentPhone').val(),
      status: $('#editStudentStatus').val()
    };

    const studentId = $('#editStudentId').val();

    try {
      console.log('正在更新学生信息:', studentId, formData);

      const result = await gradeAPI.callAPI(`api/admin/students/${studentId}`, {
        method: 'PUT',
        body: JSON.stringify(formData)
      });

      console.log('更新结果:', result);

      alert('学生信息更新成功');
      hideEditModal();
      loadStudents(); // 重新加载学生列表

    } catch (error) {
      console.error('更新学生错误详情:', error);
      alert('更新学生信息失败: ' + error.message);
    }
  }


  async function loadSystemConfig() {
    try {
      const result = await gradeAPI.callAPI('api/admin/system/config');
      const config = result.data;

      // 设置表单值 - 处理时间戳
      if (config.start_time && config.start_time > 0) {
        // 将时间戳转换为 datetime-local 需要的格式 (YYYY-MM-DDTHH:mm)
        const startDate = new Date(config.start_time);
        $('#startTime').val(startDate.toISOString().slice(0, 16));
      } else {
        $('#startTime').val('');
      }

      if (config.end_time && config.end_time > 0) {
        const endDate = new Date(config.end_time);
        $('#endTime').val(endDate.toISOString().slice(0, 16));
      } else {
        $('#endTime').val('');
      }

      $('#isActive').prop('checked', config.is_active || false);

      // 更新状态显示
      updateConfigStatus();

    } catch (error) {
      console.error('加载系统配置失败:', error);
      $('#currentConfigStatus').html('❌ 加载配置失败: ' + error.message);
    }
  }

  async function handleSaveConfig(e) {
    e.preventDefault();

    const startTimeVal = $('#startTime').val();
    const endTimeVal = $('#endTime').val();

    const formData = {
      startTime: startTimeVal ? new Date(startTimeVal).getTime() : null,
      endTime: endTimeVal ? new Date(endTimeVal).getTime() : null,
      isActive: $('#isActive').is(':checked')
    };

    console.log('发送的时间配置:', formData);

    try {
      const result = await gradeAPI.callAPI('api/admin/system/config', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      alert('系统配置保存成功');
      updateConfigStatus(); // 保存后更新状态显示

    } catch (error) {
      alert('保存配置失败: ' + error.message);
    }
  }

  // 添加表单变化监听，实时更新状态
  $(document).ready(function() {
    loadStudents();
    loadSystemConfig();

    // 表单提交处理
    $('#addStudentForm').on('submit', handleAddStudent);
    $('#editStudentForm').on('submit', handleEditStudent);
    $('#timeConfigForm').on('submit', handleSaveConfig);

    // 监听表单变化，实时更新状态
    $('#startTime, #endTime, #isActive').on('change', updateConfigStatus);
  });

  // 清除时间限制功能
  async function clearTimeRestrictions() {
    if (!confirm('确定要清除所有时间限制吗？\n\n清除后，学生将可以随时查询成绩。')) {
      return;
    }

    try {
      const result = await gradeAPI.callAPI('api/admin/system/config', {
        method: 'POST',
        body: JSON.stringify({
          startTime: null,
          endTime: null,
          isActive: false
        })
      });

      alert('时间限制已成功清除！');
      await loadSystemConfig(); // 重新加载配置以更新界面
      updateConfigStatus(); // 更新状态显示

    } catch (error) {
      alert('清除时间限制失败: ' + error.message);
      console.error('清除时间限制错误:', error);
    }
  }

  /// 日期处理辅助函数
  function formatDateForDisplay(dateValue) {
    if (!dateValue) return null;

    try {
      const date = new Date(dateValue);
      if (isNaN(date.getTime())) {
        return null;
      }
      return date.toLocaleString();
    } catch (e) {
      console.error('日期格式化错误:', e);
      return null;
    }
  }

  function formatDateForInput(dateValue) {
    if (!dateValue) return '';

    try {
      const date = new Date(dateValue);
      if (isNaN(date.getTime())) {
        return '';
      }
      return date.toISOString().slice(0, 16);
    } catch (e) {
      console.error('日期格式化错误:', e);
      return '';
    }
  }

  // 使用辅助函数重写 updateConfigStatus
  function updateConfigStatus() {
    const startTimeVal = $('#startTime').val();
    const endTimeVal = $('#endTime').val();
    const isActive = $('#isActive').is(':checked');

    const currentTime = new Date().getTime();
    let statusText = '';

    if (!isActive) {
      statusText = '🟢 状态: 时间限制未启用 - 学生可以随时查询成绩';
    } else if (!startTimeVal && !endTimeVal) {
      statusText = '🟡 状态: 时间限制已启用但未设置具体时间';
    } else {
      const startTime = startTimeVal ? new Date(startTimeVal).getTime() : null;
      const endTime = endTimeVal ? new Date(endTimeVal).getTime() : null;

      if (startTime && currentTime < startTime) {
        statusText = '🟡 状态: 时间限制已启用 - 查询尚未开始';
      } else if (endTime && currentTime > endTime) {
        statusText = '🔴 状态: 时间限制已启用 - 查询已结束';
      } else {
        statusText = '🟢 状态: 时间限制已启用 - 当前在查询时间内';
      }

      // 使用辅助函数安全地显示日期
      const startDisplay = formatDateForDisplay(startTimeVal);
      const endDisplay = formatDateForDisplay(endTimeVal);

      if (startDisplay) {
        statusText += `<br>📅 开始时间: ${startDisplay}`;
      }
      if (endDisplay) {
        statusText += `<br>📅 结束时间: ${endDisplay}`;
      }
    }

    $('#currentConfigStatus').html(statusText);
  }

  // 退出登录
  async function logout() {
    await authManager.logout();
  }
</script>
</body>
</html>