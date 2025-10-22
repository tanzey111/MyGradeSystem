<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>教师管理后台 - 学生成绩查询系统</title>
  <link rel="stylesheet" type="text/css" href="css/style.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="js/api.js"></script>
  <script src="js/auth.js"></script>
  <script src="js/grade.js"></script>
  <style>
    /* 美化下拉多选框样式 */
    .custom-multiselect {
      position: relative;
      width: 100%;
      max-width: 300px;
    }

    .select-box {
      border: 1px solid #cbd5e0;
      border-radius: 6px;
      padding: 10px 12px;
      background: white;
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      transition: all 0.3s ease;
      font-size: 14px;
      color: #4a5568;
    }

    .select-box:hover {
      border-color: #4299e1;
    }

    .select-box.open {
      border-color: #4299e1;
      box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
    }

    .select-arrow {
      transition: transform 0.3s ease;
      color: #718096;
    }

    .select-box.open .select-arrow {
      transform: rotate(180deg);
    }

    .dropdown-content {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 6px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
      z-index: 1000;
      max-height: 200px;
      overflow-y: auto;
      display: none;
      margin-top: 4px;
    }

    .dropdown-content.show {
      display: block;
    }

    .dropdown-item {
      padding: 8px 12px;
      cursor: pointer;
      display: flex;
      align-items: center;
      transition: background-color 0.2s ease;
      font-size: 14px;
    }

    .dropdown-item:hover {
      background-color: #f7fafc;
    }

    .dropdown-item input[type="checkbox"] {
      margin-right: 8px;
      width: 16px;
      height: 16px;
      cursor: pointer;
    }

    .dropdown-item label {
      cursor: pointer;
      margin: 0;
      flex: 1;
    }

    .dropdown-actions {
      padding: 8px 12px;
      border-top: 1px solid #e2e8f0;
      display: flex;
      gap: 8px;
    }

    .dropdown-btn {
      padding: 4px 8px;
      border: 1px solid #4299e1;
      background: white;
      color: #4299e1;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
      transition: all 0.3s ease;
    }

    .dropdown-btn:hover {
      background: #4299e1;
      color: white;
    }

    .selected-count {
      font-size: 12px;
      color: #718096;
      margin-left: 8px;
    }
  </style>
</head>
<body>
<div class="header">
  <h1>学生成绩查询系统 - 教师管理后台</h1>
  <div class="user-info">
    <span id="userWelcome">教师</span> |
    <a href="#" onclick="logout()">退出登录</a>
  </div>
</div>

<div class="container">
  <div class="teacher-nav">
    <button class="nav-btn active" onclick="showSection('grade-upload')">成绩导入</button>
    <button class="nav-btn" onclick="showSection('grade-management')">成绩管理</button>
  </div>

  <!-- 成绩导入部分 -->
  <div id="grade-upload" class="teacher-section">
    <div class="card">
      <h2>批量导入成绩</h2>

      <div class="upload-form">
        <form id="uploadForm" enctype="multipart/form-data">
          <div class="form-group">
            <label for="gradeFile">选择成绩文件:</label>
            <div class="upload-area" id="uploadArea">
              <div class="upload-icon">📁</div>
              <p>拖拽文件到这里或点击选择文件</p>
              <p class="file-types">支持格式: CSV, Excel (.xlsx)</p>
              <input type="file" id="gradeFile" name="file" accept=".csv,.xlsx" style="display: none;">
              <button type="button" class="btn-primary" onclick="document.getElementById('gradeFile').click()">
                选择文件
              </button>
            </div>
            <div id="fileName" style="margin-top: 1rem; font-weight: 500;"></div>
          </div>

          <div class="form-group">
            <label>文件格式说明:</label>
            <div style="background: #f7fafc; padding: 1rem; border-radius: 6px; font-size: 0.9rem;">
              <p>CSV/Excel文件应包含以下列：</p>
              <ul style="margin: 0.5rem 0 0 1rem;">
                <li><strong>学号</strong> - 学生学号</li>
                <li><strong>课程名称</strong> - 课程完整名称</li>
                <li><strong>成绩</strong> - 分数 (0-100)</li>
                <li><strong>学期</strong> - 如: 2024-2025-1 (可选)</li>
              </ul>
            </div>
          </div>

          <button type="submit" class="btn-upload" id="uploadBtn">开始导入</button>
        </form>
      </div>

      <div class="template-download" style="margin-top: 2rem;">
        <h3>下载模板文件</h3>
        <button onclick="downloadTemplate()" class="btn-primary">下载CSV模板</button>
      </div>
    </div>
  </div>

  <!-- 成绩管理部分 -->
  <div id="grade-management" class="teacher-section" style="display: none;">
    <div class="card">
      <h2>成绩管理</h2>

      <div class="controls">
        <input type="text" id="searchStudentGrade" placeholder="搜索学号或姓名..."
               onkeyup="searchStudentGrades()">

        <!-- 下拉多选框 -->
        <div class="custom-multiselect">
          <div class="select-box" onclick="toggleDropdown()">
            <span id="selectBoxText">选择课程</span>
            <span class="select-arrow">▼</span>
          </div>
          <div class="dropdown-content" id="dropdownContent">
            <div id="dropdownItems">
              <!-- 复选框项将在这里动态生成 -->
              <div class="dropdown-item">加载中...</div>
            </div>
            <div class="dropdown-actions">
              <button class="dropdown-btn" onclick="selectAllCourses()">全选</button>
              <button class="dropdown-btn" onclick="clearAllCourses()">清空</button>
            </div>
          </div>
        </div>

        <button onclick="showAddGradeForm()" class="btn-add">添加成绩</button>
      </div>

      <table class="data-table">
        <thead>
        <tr>
          <th>学号</th>
          <th>学生姓名</th>
          <th>课程名称</th>
          <th>成绩</th>
          <th>学期</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody id="gradesTableBody">
        <tr>
          <td colspan="6" class="no-data">加载中...</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- 添加成绩模态框 -->
<div id="addGradeModal" class="modal" style="display: none;">
  <div class="modal-content">
    <h3>添加成绩</h3>
    <form id="addGradeForm">
      <div class="form-group">
        <label>学号:</label>
        <input type="text" name="studentId" required>
      </div>
      <div class="form-group">
        <label>课程名称:</label>
        <input type="text" name="courseName" required>
      </div>
      <div class="form-group">
        <label>成绩:</label>
        <input type="number" name="score" min="0" max="100" step="0.1" required>
      </div>
      <div class="form-group">
        <label>学期:</label>
        <input type="text" name="semester" value="2024-2025-1" required>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn-primary">添加</button>
        <button type="button" onclick="hideModal('addGradeModal')" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</div>
<!-- 编辑成绩模态框 -->
<div id="editGradeModal" class="modal" style="display: none;">
  <div class="modal-content">
    <h3>编辑成绩</h3>
    <form id="editGradeForm">
      <input type="hidden" id="editGradeId" name="id">

      <div class="form-group">
        <label>学号:</label>
        <input type="text" id="editGradeStudentId" disabled style="background: #f5f5f5;">
      </div>

      <div class="form-group">
        <label>课程名称:</label>
        <input type="text" id="editGradeCourseName" disabled style="background: #f5f5f5;">
      </div>

      <div class="form-group">
        <label>成绩:</label>
        <input type="number" id="editGradeScore" name="score" min="0" max="100" step="0.1" required>
      </div>

      <div class="form-group">
        <label>学期:</label>
        <input type="text" id="editGradeSemester" name="semester" required>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-primary">保存修改</button>
        <button type="button" onclick="hideModal('editGradeModal')" class="btn-cancel">取消</button>
      </div>
    </form>
  </div>
</div>

<script>
  // 页面加载完成后执行
  $(document).ready(function() {
    loadUserInfo();
    setupFileUpload();
    loadAllGrades();

    // 表单提交处理
    $('#uploadForm').on('submit', handleFileUpload);
    $('#addGradeForm').on('submit', handleAddGrade);
    $('#editGradeForm').on('submit', handleEditGrade);

    // 点击页面其他地方关闭下拉框
    $(document).on('click', function(e) {
      if (!$(e.target).closest('.custom-multiselect').length) {
        closeDropdown();
      }
    });
  });

  // 存储所有成绩数据，用于筛选
  let allGradesData = [];

  function loadUserInfo() {
    // 从sessionStorage获取用户信息
    const userData = sessionStorage.getItem('userData');
    if (userData) {
      const user = JSON.parse(userData);
      $('#userWelcome').text(`欢迎，${user.name}老师`);
    }
  }

  function showSection(sectionId) {
    $('.teacher-section').hide();
    $('#' + sectionId).show();
    $('.nav-btn').removeClass('active');
    event.target.classList.add('active');
  }

  function setupFileUpload() {
    const fileInput = $('#gradeFile');
    const uploadArea = $('#uploadArea');
    const fileName = $('#fileName');

    // 点击上传区域触发文件选择
    uploadArea.on('click', function() {
      fileInput.click();
    });

    // 文件选择变化
    fileInput.on('change', function() {
      if (this.files.length > 0) {
        fileName.text(`已选择文件: ${this.files[0].name}`);
        uploadArea.css({
          'border-color': '#48bb78',
          'background': '#f0fff4'
        });
      }
    });

    // 拖拽功能
    uploadArea.on('dragover', function(e) {
      e.preventDefault();
      $(this).addClass('dragover');
    });

    uploadArea.on('dragleave', function() {
      $(this).removeClass('dragover');
    });

    uploadArea.on('drop', function(e) {
      e.preventDefault();
      $(this).removeClass('dragover');
      const files = e.originalEvent.dataTransfer.files;
      if (files.length > 0) {
        fileInput[0].files = files;
        fileName.text(`已选择文件: ${files[0].name}`);
        $(this).css({
          'border-color': '#48bb78',
          'background': '#f0fff4'
        });
      }
    });
  }

  async function handleFileUpload(e) {
    e.preventDefault();

    const fileInput = $('#gradeFile')[0];
    if (!fileInput.files.length) {
      alert('请选择要上传的文件');
      return;
    }

    const file = fileInput.files[0];
    const fileName = file.name.toLowerCase();

    // 验证文件类型
    if (!fileName.endsWith('.csv') && !fileName.endsWith('.xlsx')) {
      alert('请上传CSV或Excel文件');
      return;
    }

    try {
      $('#uploadBtn').prop('disabled', true).text('上传中...');

      const formData = new FormData();
      formData.append('file', file);

      const result = await gradeAPI.uploadGrades(formData);

      alert(result.message || '文件上传成功');
      $('#uploadForm')[0].reset();
      $('#fileName').empty();
      $('#uploadArea').css({
        'border-color': '#cbd5e0',
        'background': 'transparent'
      });

      // 刷新成绩列表
      loadAllGrades();

    } catch (error) {
      alert('上传失败: ' + error.message);
    } finally {
      $('#uploadBtn').prop('disabled', false).text('开始导入');
    }
  }

  //下载模版
  function downloadTemplate() {
    try {
      // CSV内容
      const csvData = [
        ['学号', '课程名称', '成绩', '学期'],
        ['2024001', 'Java程序设计', '85.5', '2024-2025-1'],
        ['2024002', 'Java程序设计', '78.0', '2024-2025-1'],
        ['2024003', '数据库原理', '92.0', '2024-2025-1']
      ];

      // 将数组转换为CSV字符串
      let csvContent = '';
      csvData.forEach(row => {
        csvContent += row.map(field => {
          // 处理可能包含逗号或引号的字段
          if (field.includes(',') || field.includes('"') || field.includes('\n')) {
            return '"' + field.replace(/"/g, '""') + '"';
          }
          return field;
        }).join(',') + '\n';
      });

      // 添加UTF-8 BOM头
      const BOM = '\uFEFF';
      const csvWithBOM = BOM + csvContent;

      // 创建Blob并下载
      const blob = new Blob([csvWithBOM], {
        type: 'text/csv;charset=utf-8;'
      });

      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);

      link.setAttribute('href', url);
      link.setAttribute('download', '成绩模板.csv');
      link.style.visibility = 'hidden';

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      // 清理URL对象
      setTimeout(() => {
        URL.revokeObjectURL(url);
      }, 100);

    } catch (error) {
      console.error('下载模板失败:', error);
      alert('下载模板失败，请重试');
    }
  }

  async function loadAllGrades() {
    try {
      const result = await gradeManager.getAllGrades();
      allGradesData = result.data; // 保存所有成绩数据
      renderGradesTable(allGradesData);
      updateCourseDropdown(allGradesData);
    } catch (error) {
      alert('加载成绩列表失败: ' + error.message);
    }
  }

  function renderGradesTable(grades) {
    const tbody = $('#gradesTableBody');

    if (!grades || grades.length === 0) {
      tbody.html('<tr><td colspan="6" class="no-data">暂无成绩数据</td></tr>');
      return;
    }

    tbody.html(grades.map(grade => `
                <tr>
                    <td>${grade.studentId}</td>
                    <td>${grade.studentName || '-'}</td>
                    <td>${grade.courseName}</td>
                    <td>${grade.score}</td>
                    <td>${grade.semester || '-'}</td>
                    <td>
                        <button onclick="editGrade(${grade.id})"  class="btn-edit">编辑</button>
                        <button onclick="deleteGrade(${grade.id})" class="btn-danger">删除</button>
                    </td>
                </tr>
            `).join(''));
  }

  function showAddGradeForm() {
    $('#addGradeModal').show();
  }

  function hideModal(modalId) {
    $('#' + modalId).hide();
  }

  async function handleAddGrade(e) {
    e.preventDefault();

    const formData = {
      studentId: $('input[name="studentId"]').val(),
      courseName: $('input[name="courseName"]').val(),
      score: parseFloat($('input[name="score"]').val()),
      semester: $('input[name="semester"]').val()
    };

    // 验证数据
    if (!formData.studentId || !formData.courseName || isNaN(formData.score)) {
      alert('请填写完整的成绩信息');
      return;
    }

    if (formData.score < 0 || formData.score > 100) {
      alert('成绩必须在0-100之间');
      return;
    }

    try {
      const result = await gradeManager.addGrade(formData);

      if (result.success) {
        alert('成绩添加成功');
        hideModal('addGradeModal');
        $('#addGradeForm')[0].reset();
        loadAllGrades();
      } else {
        alert('添加成绩失败: ' + result.message);
      }

    } catch (error) {
      alert('添加成绩失败: ' + error.message);
      console.error('添加成绩错误:', error);
    }
  }

  // 更新课程下拉框
  function updateCourseDropdown(grades) {
    const dropdownItems = $('#dropdownItems');
    const courses = [...new Set(grades.map(grade => grade.courseName))];

    if (courses.length === 0) {
      dropdownItems.html('<div class="dropdown-item">暂无课程数据</div>');
      return;
    }

    dropdownItems.html(courses.map(course => `
      <div class="dropdown-item">
        <input type="checkbox" value="${course}" class="course-checkbox" onchange="filterGrades()">
        <label>${course}</label>
      </div>
    `).join(''));

    updateSelectBoxText();
  }

  // 切换下拉框显示/隐藏
  function toggleDropdown() {
    const dropdown = $('#dropdownContent');
    const selectBox = $('.select-box');

    if (dropdown.hasClass('show')) {
      closeDropdown();
    } else {
      dropdown.addClass('show');
      selectBox.addClass('open');
    }
  }

  // 关闭下拉框
  function closeDropdown() {
    $('#dropdownContent').removeClass('show');
    $('.select-box').removeClass('open');
  }

  // 更新选择框文本
  function updateSelectBoxText() {
    const selectedCourses = [];
    $('.course-checkbox:checked').each(function() {
      selectedCourses.push($(this).val());
    });

    const selectBoxText = $('#selectBoxText');

    if (selectedCourses.length === 0) {
      selectBoxText.text('选择课程');
    } else if (selectedCourses.length === 1) {
      selectBoxText.text(selectedCourses[0]);
    } else {
      selectBoxText.text(`已选择 ${selectedCourses.length} 门课程`);
    }
  }

  // 筛选成绩数据
  function filterGrades() {
    const selectedCourses = [];
    $('.course-checkbox:checked').each(function() {
      selectedCourses.push($(this).val());
    });

    // 更新选择框文本
    updateSelectBoxText();

    // 如果没有选择任何课程，显示所有数据
    if (selectedCourses.length === 0) {
      renderGradesTable(allGradesData);
      return;
    }

    // 筛选数据
    const filteredGrades = allGradesData.filter(grade =>
            selectedCourses.includes(grade.courseName)
    );

    // 渲染筛选后的数据
    renderGradesTable(filteredGrades);
  }

  // 搜索学生成绩
  function searchStudentGrades() {
    const searchTerm = $('#searchStudentGrade').val().toLowerCase();

    if (!searchTerm) {
      // 如果没有搜索词，显示当前筛选结果
      filterGrades();
      return;
    }

    // 获取当前显示的课程筛选
    const selectedCourses = [];
    $('.course-checkbox:checked').each(function() {
      selectedCourses.push($(this).val());
    });

    // 确定要搜索的数据集
    let dataToSearch = allGradesData;
    if (selectedCourses.length > 0) {
      dataToSearch = allGradesData.filter(grade =>
              selectedCourses.includes(grade.courseName)
      );
    }

    // 搜索
    const searchResults = dataToSearch.filter(grade =>
            grade.studentId.toLowerCase().includes(searchTerm) ||
            (grade.studentName && grade.studentName.toLowerCase().includes(searchTerm))
    );

    // 渲染搜索结果
    renderGradesTable(searchResults);
  }

  // 全选课程
  function selectAllCourses() {
    $('.course-checkbox').prop('checked', true);
    filterGrades();
  }

  // 清空选择
  function clearAllCourses() {
    $('.course-checkbox').prop('checked', false);
    filterGrades();
  }

  async function editGrade(gradeId) {
    try {
      console.log('=== 开始编辑成绩 ===');
      console.log('成绩ID:', gradeId);

      const result = await gradeManager.getGrade(gradeId);
      console.log('API响应:', result);

      if (!result.success) {
        throw new Error(result.message || '获取成绩信息失败');
      }

      const grade = result.data;
      console.log('成绩数据:', grade);

      // 填充编辑表单
      $('#editGradeId').val(grade.id);
      $('#editGradeStudentId').val(grade.studentId);
      $('#editGradeCourseName').val(grade.courseName);
      $('#editGradeScore').val(grade.score);
      $('#editGradeSemester').val(grade.semester);

      // 显示编辑模态框
      $('#editGradeModal').show();
      console.log('=== 编辑表单填充完成 ===');

    } catch (error) {
      console.error('编辑成绩完整错误:', error);
      alert('获取成绩信息失败: ' + error.message);
    }
  }

  // 修改 handleEditGrade 函数
  async function handleEditGrade(e) {
    e.preventDefault();

    const gradeId = $('#editGradeId').val();
    const formData = {
      score: parseFloat($('#editGradeScore').val()),
      semester: $('#editGradeSemester').val()
    };

    console.log('=== 开始更新成绩 ===');
    console.log('成绩ID:', gradeId);
    console.log('表单数据:', formData);

    // 验证数据
    if (isNaN(formData.score) || formData.score < 0 || formData.score > 100) {
      alert('成绩必须在0-100之间');
      return;
    }

    if (!formData.semester.trim()) {
      alert('学期不能为空');
      return;
    }

    try {
      const result = await gradeManager.updateGrade(gradeId, formData);
      console.log('更新响应:', result);

      if (!result.success) {
        throw new Error(result.message || '更新失败');
      }

      alert('成绩更新成功');
      $('#editGradeModal').hide();
      loadAllGrades();

    } catch (error) {
      console.error('更新成绩完整错误:', error);
      alert('更新成绩失败: ' + error.message);
    }
  }

  // 修改 deleteGrade 函数
  async function deleteGrade(gradeId) {
    if (!confirm('确定要删除这条成绩记录吗？')) {
      return;
    }

    try {
      console.log('删除成绩ID:', gradeId);
      const result = await gradeManager.deleteGrade(gradeId);
      console.log('删除响应:', result);

      if (result.success) {
        alert('成绩删除成功');
        loadAllGrades();
      } else {
        alert('删除成绩失败: ' + result.message);
      }

    } catch (error) {
      console.error('删除成绩完整错误:', error);
      alert('删除成绩失败: ' + error.message);
    }
  }

  async function logout() {
    await authManager.logout();
  }
</script>
</body>
</html>