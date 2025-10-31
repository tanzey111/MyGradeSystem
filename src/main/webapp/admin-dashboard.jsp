<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>管理员后台</title>
  <link rel="stylesheet" type="text/css" href="css/style.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="js/api.js"></script>
  <script src="js/auth.js"></script>
  <style>
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
    <button class="nav-btn" onclick="showSection('teacher-management')">教师管理</button>
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
  <!-- 教师管理部分 -->
  <div id="teacher-management" class="admin-section" style="display: none;">
    <h2>教师管理</h2>
    <br>
    <div class="controls">
      <input type="text" id="searchTeacher" placeholder="搜索工号、姓名、邮箱或院系..."
             onkeyup="searchTeachers()">
      <button onclick="showAddTeacherForm()" class="btn-add">添加教师</button>
      <button onclick="loadTeachers()" class="btn-refresh">刷新</button>
    </div>

    <table class="data-table">
      <thead>
      <tr>
        <th>工号</th>
        <th>姓名</th>
        <th>邮箱</th>
        <th>院系</th>
        <th>状态</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody id="teachersTableBody">
      <tr>
        <td colspan="6">加载中...</td>
      </tr>
      </tbody>
    </table>
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

<!-- 添加教师模态框 -->
<div id="addTeacherModal" class="modal" style="display: none;">
  <div class="modal-content">
    <form id="addTeacherForm">
      <div class="form-group">
        <label>工号:</label>
        <input type="text" name="id" required>
      </div>
      <div class="form-group">
        <label>姓名:</label>
        <input type="text" name="name" required>
      </div>
      <div class="form-group">
        <label>邮箱:</label>
        <input type="email" name="email">
      </div>
      <div class="form-group">
        <label>院系:</label>
        <input type="text" name="department">
      </div>
      <div class="form-group">
        <label>初始密码:</label>
        <input type="password" name="password" value="123456">
      </div>
      <div class="form-actions">
        <button type="submit" class="btn-primary">添加</button>
        <button type="button" onclick="hideAddTeacherModal()" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</div>

<!-- 编辑教师模态框 -->
<div id="editTeacherModal" class="modal" style="display: none;">
  <div class="modal-content">
    <form id="editTeacherForm">
      <input type="hidden" id="editTeacherId" name="id">

      <div class="form-group">
        <label>工号:</label>
        <input type="text" id="editTeacherIdDisplay" disabled style="background: #f5f5f5;">
        <small style="color: #666; display: block; margin-top: 5px;">工号不可修改</small>
      </div>

      <div class="form-group">
        <label>姓名:</label>
        <input type="text" id="editTeacherName" name="name" required>
      </div>

      <div class="form-group">
        <label>邮箱:</label>
        <input type="email" id="editTeacherEmail" name="email">
      </div>

      <div class="form-group">
        <label>院系:</label>
        <input type="text" id="editTeacherDepartment" name="department">
      </div>

      <div class="form-group">
        <label>状态:</label>
        <select id="editTeacherStatus" name="status" required>
          <option value="active">正常</option>
          <option value="inactive">禁用</option>
        </select>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary">保存修改</button>
        <button type="button" onclick="hideEditTeacherModal()" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</div>

<script>
  // 管理员功能JavaScript
  $(document).ready(function() {
    loadStudents();
    loadTeachers();

    // 表单提交处理
    $('#addStudentForm').on('submit', handleAddStudent);
    $('#editStudentForm').on('submit', handleEditStudent);
    $('#addTeacherForm').on('submit', handleAddTeacher);
    $('#editTeacherForm').on('submit', handleEditTeacher);
  });

  function showSection(sectionId) {
    $('.admin-section').hide();
    $('#' + sectionId).show();
    $('.nav-btn').removeClass('active');
    event.target.classList.add('active');
  }

  // 修改showSection函数以支持教师管理
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

  // 教师管理相关函数
  async function loadTeachers() {
    try {
      const result = await gradeAPI.callAPI('api/admin/teachers');
      renderTeachersTable(result.data);
    } catch (error) {
      alert('加载教师列表失败: ' + error.message);
    }
  }

  function renderTeachersTable(teachers) {
    const tbody = $('#teachersTableBody');

    if (teachers.length === 0) {
      tbody.html('<tr><td colspan="6">暂无教师数据</td></tr>');
      return;
    }

    tbody.html(teachers.map(teacher => `
        <tr>
            <td>${teacher.id}</td>
            <td>${teacher.name}</td>
            <td>${teacher.email || '-'}</td>
            <td>${teacher.department || '-'}</td>
            <td>${teacher.status == 'active' ? '正常' : '禁用'}</td>
            <td>
                <button onclick="editTeacher('${teacher.id}')" class="btn-edit">编辑</button>
                <button onclick="deleteTeacher('${teacher.id}')" class="btn-danger">删除</button>
            </td>
        </tr>
    `).join(''));
  }

  function showAddTeacherForm() {
    $('#addTeacherModal').show();
  }

  function hideAddTeacherModal() {
    $('#addTeacherModal').hide();
  }

  function showEditTeacherForm() {
    $('#editTeacherModal').show();
  }

  function hideEditTeacherModal() {
    $('#editTeacherModal').hide();
  }

  async function handleAddTeacher(e) {
    e.preventDefault();

    const formData = {
      id: $('#addTeacherModal input[name="id"]').val().trim(),
      name: $('#addTeacherModal input[name="name"]').val().trim(),
      email: $('#addTeacherModal input[name="email"]').val().trim(),
      department: $('#addTeacherModal input[name="department"]').val().trim(),
      password: $('#addTeacherModal input[name="password"]').val().trim()
    };

    console.log('添加教师表单数据:', formData); // 调试用


    try {
      const result = await gradeAPI.callAPI('api/admin/teachers', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      console.log('添加教师响应:', result);

      alert('教师添加成功');
      hideAddTeacherModal();
      loadTeachers();
      $('#addTeacherForm')[0].reset();

    } catch (error) {
      console.error('添加教师错误详情:', error);
      alert('添加教师失败: ' + error.message);
    }
  }

  async function deleteTeacher(teacherId) {
    if (!confirm(`确定要删除教师 ${teacherId} 吗？`)) {
      return;
    }

    try {
      await gradeAPI.callAPI(`api/admin/teachers/${teacherId}`, {
        method: 'DELETE'
      });

      alert('教师删除成功');
      loadTeachers();
    } catch (error) {
      alert('删除教师失败: ' + error.message);
    }
  }

  async function editTeacher(teacherId) {
    try {
      console.log('正在获取教师信息:', teacherId);

      const result = await gradeAPI.callAPI(`api/admin/teachers/${teacherId}`);
      console.log('获取到的教师信息:', result);

      const teacher = result.data;

      // 填充编辑表单
      $('#editTeacherId').val(teacher.id);
      $('#editTeacherIdDisplay').val(teacher.id);
      $('#editTeacherName').val(teacher.name);
      $('#editTeacherEmail').val(teacher.email || '');
      $('#editTeacherDepartment').val(teacher.department || '');
      $('#editTeacherStatus').val(teacher.status || 'active');

      // 显示编辑模态框
      $('#editTeacherModal').show();

    } catch (error) {
      console.error('编辑教师错误详情:', error);
      alert('获取教师信息失败: ' + error.message);
    }
  }

  async function handleEditTeacher(e) {
    e.preventDefault();

    const formData = {
      name: $('#editTeacherName').val(),
      email: $('#editTeacherEmail').val(),
      department: $('#editTeacherDepartment').val(),
      status: $('#editTeacherStatus').val()
    };

    const teacherId = $('#editTeacherId').val();

    try {
      console.log('正在更新教师信息:', teacherId, formData);

      const result = await gradeAPI.callAPI(`api/admin/teachers/${teacherId}`, {
        method: 'PUT',
        body: JSON.stringify(formData)
      });

      console.log('更新结果:', result);

      alert('教师信息更新成功');
      hideEditTeacherModal();
      loadTeachers(); // 重新加载教师列表

    } catch (error) {
      console.error('更新教师错误详情:', error);
      alert('更新教师信息失败: ' + error.message);
    }
  }

  // 搜索教师功能
  function searchTeachers() {
    const searchTerm = $('#searchTeacher').val().toLowerCase().trim();
    const rows = $('#teachersTableBody tr');

    // 如果没有搜索词，显示所有行
    if (!searchTerm) {
      rows.show();
      return;
    }

    rows.each(function() {
      const teacherId = $(this).find('td:eq(0)').text().toLowerCase();
      const teacherName = $(this).find('td:eq(1)').text().toLowerCase();
      const teacherEmail = $(this).find('td:eq(2)').text().toLowerCase();
      const teacherDepartment = $(this).find('td:eq(3)').text().toLowerCase();

      // 搜索工号、姓名、邮箱或院系
      if (teacherId.includes(searchTerm) ||
              teacherName.includes(searchTerm) ||
              teacherEmail.includes(searchTerm) ||
              teacherDepartment.includes(searchTerm)) {
        $(this).show();
      } else {
        $(this).hide();
      }
    });
  }

  // 退出登录
  async function logout() {
    await authManager.logout();
  }
</script>
</body>
</html>