<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>教师管理后台 - 学生成绩查询系统</title>
  <link rel="stylesheet" type="text/css" href="css/tech_style.css">
  <link rel="stylesheet" type="text/css" href="css/style.css">
  <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
  <script src="js/api.js"></script>
  <script src="js/auth.js"></script>
  <script src="js/grade.js"></script>
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
    <button class="nav-btn" onclick="showSection('system-config')">系统配置</button>
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
                <li><strong>姓名</strong> - 学生姓名</li>
                <li><strong>课程名称</strong> - 课程完整名称</li>
                <li><strong>成绩</strong> - 分数 (0-100)</li>
                <li><strong>学期</strong> - 如: 2024-2025-1 (可选)</li>
              </ul>
            </div>
          </div>
          <div class="form-actions">
            <button type="button" onclick="downloadTemplate()" class="btn-primary">下载CSV模板</button>
            <button type="submit" class="btn-upload" id="uploadBtn">开始导入</button>
          </div>
        </form>

      </div>

      <div id="importResultSection" class="import-result-container" style="display: none;">
        <div class="import-result-header">
          <h3>📊 导入结果</h3>
          <span id="importResultMessage" style="font-weight: 500;"></span>
        </div>

        <div class="import-summary">
          <div class="stats-grid">
            <div class="stat-card success">
              <div class="stat-number" id="statTotalCount">0</div>
              <div class="stat-label">总记录数</div>
            </div>
            <div class="stat-card success">
              <div class="stat-number" id="statSuccessInsert">0</div>
              <div class="stat-label">成功新增</div>
            </div>
            <div class="stat-card success">
              <div class="stat-number" id="statSuccessUpdate">0</div>
              <div class="stat-label">成功更新</div>
            </div>
            <div class="stat-card warning">
              <div class="stat-number" id="statDuplicate">0</div>
              <div class="stat-label">重复跳过</div>
            </div>
            <div class="stat-card info">
              <div class="stat-number" id="statAutoCreated">0</div>
              <div class="stat-label">自动创建学生</div>
            </div>
            <div class="stat-card danger">
              <div class="stat-number" id="statNameMismatch">0</div>
              <div class="stat-label">姓名不匹配</div>
            </div>
            <div class="stat-card danger">
              <div class="stat-number" id="statPermissionErrors">0</div>
              <div class="stat-label">权限错误</div>
            </div>
            <div class="stat-card danger">
              <div class="stat-number" id="statEnrollmentErrors">0</div>
              <div class="stat-label">选课验证错误</div>
            </div>
            <div class="stat-card danger">
              <div class="stat-number" id="statErrors">0</div>
              <div class="stat-label">总错误数</div>
            </div>
          </div>

          <div id="successMessage" class="success-message" style="display: none;">
            <h4>导入成功！</h4>
            <p>所有数据都已成功处理，没有发现任何问题。</p>
          </div>
        </div>

        <div id="errorSections" class="error-sections" style="display: none;">
          <div class="error-section validation">
            <h4>❌ 数据验证错误 <span class="badge" id="validationErrorCount">0</span></h4>
            <div class="error-list" id="validationErrors">
              <div class="empty-state">暂无验证错误</div>
            </div>
          </div>

          <div class="error-section name-mismatch">
            <h4>⚠️ 姓名不匹配 <span class="badge" id="nameMismatchErrorCount">0</span></h4>
            <div class="error-list" id="nameMismatchErrors">
              <div class="empty-state">暂无姓名不匹配错误</div>
            </div>
          </div>

          <div class="error-section duplicate">
            <h4>🔄 重复数据 <span class="badge" id="duplicateErrorCount">0</span></h4>
            <div class="error-list" id="duplicateErrors">
              <div class="empty-state">暂无重复数据</div>
            </div>
          </div>

          <div class="error-section permission">
            <h4>🚫 权限错误 <span class="badge" id="permissionErrorCount">0</span></h4>
            <div class="error-list" id="permissionErrors">
              <div class="empty-state">暂无权限错误</div>
            </div>
          </div>

          <div class="error-section enrollment">
            <h4>📚 选课验证错误 <span class="badge" id="enrollmentErrorCount">0</span></h4>
            <div class="error-list" id="enrollmentErrors">
              <div class="empty-state">暂无选课验证错误</div>
            </div>
          </div>

          <div class="error-section system">
            <h4>💻 系统错误 <span class="badge" id="systemErrorCount">0</span></h4>
            <div class="error-list" id="systemErrors">
              <div class="empty-state">暂无系统错误</div>
            </div>
          </div>
        </div>

        <div class="import-result-actions">
          <button onclick="hideImportResult()" class="btn-cancel">关闭结果</button>
          <button onclick="clearImportResult()" class="btn-danger">清除结果</button>
        </div>
      </div>

      <!-- 重新显示按钮 -->
      <div id="reShowResultBtn" style="display: none; margin-top: 1rem; text-align: center;">
        <button onclick="showLastImportResult()" class="btn-primary">📊 重新显示导入结果</button>
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

        <!-- 课程下拉多选框 -->
        <div class="custom-multiselect">
          <div class="select-box" onclick="toggleDropdown('course')">
            <span id="selectBoxTextCourse">选择课程</span>
            <span class="select-arrow">▼</span>
          </div>
          <div class="dropdown-content" id="dropdownContentCourse">
            <div id="dropdownItemsCourse">
              <!-- 复选框项将在这里动态生成 -->
              <div class="dropdown-item">加载中...</div>
            </div>
            <div class="dropdown-actions">
              <button class="dropdown-btn" onclick="selectAllCourses()">全选</button>
              <button class="dropdown-btn" onclick="clearAllCourses()">清空</button>
            </div>
          </div>
        </div>

        <!-- 新增：学期下拉多选框 -->
        <div class="custom-multiselect">
          <div class="select-box" onclick="toggleDropdown('semester')">
            <span id="selectBoxTextSemester">选择学期</span>
            <span class="select-arrow">▼</span>
          </div>
          <div class="dropdown-content" id="dropdownContentSemester">
            <div id="dropdownItemsSemester">
              <!-- 复选框项将在这里动态生成 -->
              <div class="dropdown-item">加载中...</div>
            </div>
            <div class="dropdown-actions">
              <button class="dropdown-btn" onclick="selectAllSemesters()">全选</button>
              <button class="dropdown-btn" onclick="clearAllSemesters()">清空</button>
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

  <!-- 系统配置部分 -->
  <div id="system-config" class="teacher-section" style="display: none;">
    <div class="card">
      <h2 style="margin-bottom: 5px;">系统配置</h2>
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
        <label>姓名:</label>
        <input type="text" name="studentName" required>
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
        <label>姓名:</label>
        <input type="text" id="editGradeStudentName" disabled style="background: #f5f5f5;">
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
  // 全局变量
  let allGradesData = [];
  let lastImportResult = null;
  let teacherCourses = [];

  // 页面加载完成后执行
  $(document).ready(function() {
    console.log("页面加载完成，初始化开始...");
    loadUserInfo();
    setupFileUpload();
    loadAllGrades();
    loadSystemConfig();
    loadTeacherCourses();

    // 表单提交处理
    $('#uploadForm').on('submit', function(e) {
      e.preventDefault();
      handleFileUpload(e);
    });
    $('#addGradeForm').on('submit', function(e) {
      e.preventDefault();
      handleAddGrade(e);
    });
    $('#editGradeForm').on('submit', function(e) {
      e.preventDefault();
      handleEditGrade(e);
    });
    $('#timeConfigForm').on('submit', function(e) {
      e.preventDefault();
      handleSaveConfig(e);
    });

    // 监听表单变化，实时更新状态
    $('#startTime, #endTime, #isActive').on('change', updateConfigStatus);

    // 点击页面其他地方关闭下拉框
    $(document).on('click', function(e) {
      if (!$(e.target).closest('.custom-multiselect').length) {
        closeAllDropdowns();
      }
    });

    console.log("初始化完成");
  });

  // 基本页面功能
  function loadUserInfo() {
    try {
      const userData = sessionStorage.getItem('userData');
      if (userData) {
        const user = JSON.parse(userData);
        $('#userWelcome').text(`欢迎，${user.name}老师`);
      }
    } catch (error) {
      console.error('加载用户信息失败:', error);
    }
  }

  function showSection(sectionId) {
    $('.teacher-section').hide();
    $('#' + sectionId).show();
    $('.nav-btn').removeClass('active');
    $(event.target).addClass('active');
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

  // 显示/隐藏加载状态
  function showUploadLoading() {
    const uploadBtn = document.getElementById('uploadBtn');
    if (uploadBtn) {
      uploadBtn.innerHTML = '<span class="loading-spinner"></span> 上传中...';
      uploadBtn.disabled = true;
    }
  }

  function hideUploadLoading() {
    const uploadBtn = document.getElementById('uploadBtn');
    if (uploadBtn) {
      uploadBtn.innerHTML = '开始导入';
      uploadBtn.disabled = false;
    }
  }

  // 在 handleFileUpload 函数中加强错误处理
  async function handleFileUpload(e) {
    console.log("开始处理文件上传");
    e.preventDefault();

    showUploadLoading();
    const fileInput = $('#gradeFile')[0];
    if (!fileInput || !fileInput.files.length) {
      alert('请选择要上传的文件');
      hideUploadLoading();
      return;
    }

    const file = fileInput.files[0];
    const fileName = file.name.toLowerCase();

    // 验证文件类型
    if (!fileName.endsWith('.csv') && !fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
      alert('请上传CSV或Excel文件');
      hideUploadLoading();
      return;
    }

    try {
      $('#uploadBtn').prop('disabled', true).text('上传中...');

      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch('api/upload/grades', {
        method: 'POST',
        body: formData,
        credentials: 'same-origin'
      });

      const result = await response.json();
      console.log('上传响应:', result);

      if (!result.success) {
        throw new Error(result.error || result.message || '上传失败');
      }

      // 在页面上显示导入结果
      showImportResult(result.data || result);

      // 重置表单
      $('#uploadForm')[0].reset();
      $('#fileName').empty();
      $('#uploadArea').css({
        'border-color': '#cbd5e0',
        'background': 'transparent'
      });

      // 刷新成绩列表
      loadAllGrades();

    } catch (error) {
      console.error('上传错误详情:', error);
      let errorMessage = '上传失败';
      let detailedErrors = [];

      try {
        const errorData = JSON.parse(error.message);
        if (errorData.message) {
          errorMessage = errorData.message;
        }
        if (errorData.errors) {
          detailedErrors = Array.isArray(errorData.errors) ? errorData.errors : [errorData.errors];
        } else if (errorData.data && errorData.data.allErrors) {
          detailedErrors = errorData.data.allErrors;
        }
      } catch (e) {
        // 如果不是JSON，直接使用错误消息
        errorMessage = error.message;
      }

      // 显示详细的错误信息
      if (detailedErrors.length > 0) {
        errorMessage += '\n\n错误详情:\n' + detailedErrors.slice(0, 10).join('\n');
        if (detailedErrors.length > 10) {
          errorMessage += `\n... 还有 ${detailedErrors.length - 10} 个错误`;
        }
      }

      showErrorModal('上传失败', errorMessage);
    } finally {
      $('#uploadBtn').prop('disabled', false).text('开始导入');
      hideUploadLoading();
    }
  }

  // 下载模板
  function downloadTemplate() {
    try {
      // CSV内容
      const csvData = [
        ['学号', '姓名', '课程名称', '成绩', '学期'],
        ['2024001', '张三', 'Java程序设计', '85.5', '2024-2025-1'],
        ['2024002', '李四', 'Java程序设计', '78.0', '2024-2025-1'],
        ['2024003', '王五', '数据库原理', '92.0', '2024-2025-1']
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

  // 添加获取教师课程的函数
  async function loadTeacherCourses() {
    try {
      const result = await gradeManager.getTeacherCourses();
      teacherCourses = result.data || [];
      console.log('教师课程列表:', teacherCourses);
    } catch (error) {
      console.error('获取教师课程失败:', error);
    }
  }

  // 成绩管理功能
  async function loadAllGrades() {
    try {
      const result = await gradeManager.getGradesByTeacher();
      allGradesData = result.data || [];
      renderGradesTable(allGradesData);
      updateCourseDropdown(allGradesData);
      updateSemesterDropdown(allGradesData); // 新增：更新学期下拉框
    } catch (error) {
      console.error('加载成绩列表失败:', error);
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
            <td>${grade.studentId || '-'}</td>
            <td>${grade.studentName || '-'}</td>
            <td>${grade.courseName || '-'}</td>
            <td>${grade.score || '-'}</td>
            <td>${grade.semester || '-'}</td>
            <td>
                <button onclick="editGrade(${grade.id})" class="btn-edit">编辑</button>
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
      studentName: $('input[name="studentName"]').val(),
      courseName: $('input[name="courseName"]').val(),
      score: parseFloat($('input[name="score"]').val()),
      semester: $('input[name="semester"]').val()
    };

    console.log('准备添加成绩:', formData);

    // 验证数据
    if (!formData.studentId || !formData.studentName || !formData.courseName || isNaN(formData.score)) {
      alert('请填写完整的成绩信息');
      return;
    }

    if (formData.score < 0 || formData.score > 100) {
      alert('成绩必须在0-100之间');
      return;
    }

    // 检查教师是否有权限管理该课程
    if (!teacherCourses.includes(formData.courseName)) {
      alert('您没有权限管理该课程的成绩，请确认课程名称是否正确。');
      return;
    }

    try {
      console.log('开始调用API添加成绩...');
      const result = await gradeManager.addGrade(formData);
      console.log('API响应结果:', result);

      if (result.success) {
        alert('成绩添加成功');
        hideModal('addGradeModal');
        $('#addGradeForm')[0].reset();
        loadAllGrades();
      } else {
        const errorMsg = result.message || result.error || '未知错误';
        alert('添加成绩失败: ' + errorMsg);
        console.error('添加成绩失败详情:', result);
      }

    } catch (error) {
      console.error('添加成绩完整错误信息:', error);
      let errorMessage = '未知错误';
      try {
        const errorData = JSON.parse(error.message);
        errorMessage = errorData.message || errorData.error || error.message;
      } catch (e) {
        errorMessage = error.message || '请求失败，请检查网络连接';
      }
      alert('添加成绩失败: ' + errorMessage);
    }
  }

  // 课程下拉框功能
  function updateCourseDropdown(grades) {
    const dropdownItems = $('#dropdownItemsCourse');

    // 从成绩数据中提取课程列表
    const courses = [...new Set(grades.map(grade => grade.courseName).filter(Boolean))];

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

    updateSelectBoxText('course');
  }

  // 新增：学期下拉框功能
  function updateSemesterDropdown(grades) {
    const dropdownItems = $('#dropdownItemsSemester');

    // 从成绩数据中提取学期列表
    const semesters = [...new Set(grades.map(grade => grade.semester).filter(Boolean))];

    // 按学期倒序排列（最新的学期在前面）
    semesters.sort((a, b) => b.localeCompare(a));

    if (semesters.length === 0) {
      dropdownItems.html('<div class="dropdown-item">暂无学期数据</div>');
      return;
    }

    dropdownItems.html(semesters.map(semester => `
        <div class="dropdown-item">
            <input type="checkbox" value="${semester}" class="semester-checkbox" onchange="filterGrades()">
            <label>${semester}</label>
        </div>
    `).join(''));

    updateSelectBoxText('semester');
  }

  // 修改：支持不同类型的下拉框
  function toggleDropdown(type) {
    const dropdown = $(`#dropdownContent${type.charAt(0).toUpperCase() + type.slice(1)}`);
    const selectBox = $(`.select-box:has(#selectBoxText${type.charAt(0).toUpperCase() + type.slice(1)})`);

    // 关闭其他下拉框
    closeAllDropdowns();

    if (dropdown.hasClass('show')) {
      dropdown.removeClass('show');
      selectBox.removeClass('open');
    } else {
      dropdown.addClass('show');
      selectBox.addClass('open');
    }
  }

  function closeAllDropdowns() {
    $('.dropdown-content').removeClass('show');
    $('.select-box').removeClass('open');
  }

  function closeDropdown() {
    closeAllDropdowns();
  }

  // 修改：支持不同类型的下拉框文本更新
  function updateSelectBoxText(type) {
    const selectedItems = [];
    $(`.${type}-checkbox:checked`).each(function() {
      selectedItems.push($(this).val());
    });

    const selectBoxText = $(`#selectBoxText${type.charAt(0).toUpperCase() + type.slice(1)}`);
    const typeName = type === 'course' ? '课程' : '学期';

    if (selectedItems.length === 0) {
      selectBoxText.text(`选择${typeName}`);
    } else if (selectedItems.length === 1) {
      selectBoxText.text(selectedItems[0]);
    } else {
      selectBoxText.text(`已选择 ${selectedItems.length} 个${typeName}`);
    }
  }

  // 修改：同时根据课程和学期筛选
  function filterGrades() {
    const selectedCourses = [];
    $('.course-checkbox:checked').each(function() {
      selectedCourses.push($(this).val());
    });

    const selectedSemesters = [];
    $('.semester-checkbox:checked').each(function() {
      selectedSemesters.push($(this).val());
    });

    // 更新选择框文本
    updateSelectBoxText('course');
    updateSelectBoxText('semester');

    // 如果没有选择任何课程和学期，显示所有数据
    if (selectedCourses.length === 0 && selectedSemesters.length === 0) {
      renderGradesTable(allGradesData);
      return;
    }

    // 筛选数据
    const filteredGrades = allGradesData.filter(grade => {
      const courseMatch = selectedCourses.length === 0 || selectedCourses.includes(grade.courseName);
      const semesterMatch = selectedSemesters.length === 0 || selectedSemesters.includes(grade.semester);
      return courseMatch && semesterMatch;
    });

    // 渲染筛选后的数据
    renderGradesTable(filteredGrades);
  }

  function searchStudentGrades() {
    const searchTerm = $('#searchStudentGrade').val().toLowerCase();

    if (!searchTerm) {
      // 如果没有搜索词，显示当前筛选结果
      filterGrades();
      return;
    }

    // 获取当前筛选条件
    const selectedCourses = [];
    $('.course-checkbox:checked').each(function() {
      selectedCourses.push($(this).val());
    });

    const selectedSemesters = [];
    $('.semester-checkbox:checked').each(function() {
      selectedSemesters.push($(this).val());
    });

    // 在筛选后的基础上搜索
    let searchBase = allGradesData;
    if (selectedCourses.length > 0 || selectedSemesters.length > 0) {
      searchBase = allGradesData.filter(grade => {
        const courseMatch = selectedCourses.length === 0 || selectedCourses.includes(grade.courseName);
        const semesterMatch = selectedSemesters.length === 0 || selectedSemesters.includes(grade.semester);
        return courseMatch && semesterMatch;
      });
    }

    // 执行搜索
    const searchResults = searchBase.filter(grade =>
            grade.studentId.toLowerCase().includes(searchTerm) ||
            (grade.studentName && grade.studentName.toLowerCase().includes(searchTerm))
    );

    // 渲染搜索结果
    renderGradesTable(searchResults);
  }

  // 课程全选/清空
  function selectAllCourses() {
    $('.course-checkbox').prop('checked', true);
    filterGrades();
  }

  function clearAllCourses() {
    $('.course-checkbox').prop('checked', false);
    filterGrades();
  }

  // 新增：学期全选/清空
  function selectAllSemesters() {
    $('.semester-checkbox').prop('checked', true);
    filterGrades();
  }

  function clearAllSemesters() {
    $('.semester-checkbox').prop('checked', false);
    filterGrades();
  }

  // 编辑和删除成绩
  async function editGrade(gradeId) {
    try {
      console.log('开始编辑成绩，ID:', gradeId);

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
      $('#editGradeStudentName').val(grade.studentName);
      $('#editGradeCourseName').val(grade.courseName);
      $('#editGradeScore').val(grade.score);
      $('#editGradeSemester').val(grade.semester);

      // 显示编辑模态框
      $('#editGradeModal').show();
      console.log('编辑表单填充完成');

    } catch (error) {
      console.error('编辑成绩完整错误:', error);
      alert('获取成绩信息失败: ' + error.message);
    }
  }

  async function handleEditGrade(e) {
    e.preventDefault();

    const gradeId = $('#editGradeId').val();
    const formData = {
      score: parseFloat($('#editGradeScore').val()),
      semester: $('#editGradeSemester').val()
    };

    console.log('开始更新成绩，ID:', gradeId);
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

  // 系统配置功能
  async function loadSystemConfig() {
    try {
      const result = await gradeAPI.callAPI('api/teacher/system/config');
      const config = result.data;

      // 设置表单值 - 处理时间戳
      if (config.start_time && config.start_time > 0) {
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
      const result = await gradeAPI.callAPI('api/teacher/system/config', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      alert('系统配置保存成功');
      updateConfigStatus();

    } catch (error) {
      alert('保存配置失败: ' + error.message);
    }
  }

  async function clearTimeRestrictions() {
    if (!confirm('确定要清除所有时间限制吗？\n\n清除后，学生将可以随时查询成绩。')) {
      return;
    }

    try {
      const result = await gradeAPI.callAPI('api/teacher/system/config', {
        method: 'POST',
        body: JSON.stringify({
          startTime: null,
          endTime: null,
          isActive: false
        })
      });

      alert('时间限制已成功清除！');
      await loadSystemConfig();
      updateConfigStatus();

    } catch (error) {
      alert('清除时间限制失败: ' + error.message);
      console.error('清除时间限制错误:', error);
    }
  }

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

      // 安全地显示日期
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

  // 导入结果显示功能
  function showImportResult(result) {
    console.log("显示导入结果:", result);
    lastImportResult = result;

    const resultSection = document.getElementById('importResultSection');
    const reShowBtn = document.getElementById('reShowResultBtn');
    const errorSections = document.getElementById('errorSections');
    const successMessage = document.getElementById('successMessage');
    const resultMessage = document.getElementById('importResultMessage');

    if (!resultSection) {
      console.error('找不到导入结果容器');
      return;
    }

    // 更新统计数字
    document.getElementById('statTotalCount').textContent = result.totalCount || 0;
    document.getElementById('statSuccessInsert').textContent = result.successInsertCount || 0;
    document.getElementById('statSuccessUpdate').textContent = result.successUpdateCount || 0;
    document.getElementById('statDuplicate').textContent = result.duplicateCount || 0;
    document.getElementById('statAutoCreated').textContent = result.autoCreatedCount || 0;
    document.getElementById('statNameMismatch').textContent = result.nameMismatchCount || 0;
    document.getElementById('statPermissionErrors').textContent = result.permissionErrorCount || 0;
    document.getElementById('statEnrollmentErrors').textContent = result.enrollmentErrorCount || 0;

    const totalErrors = (result.allErrors ? result.allErrors.length : 0);
    document.getElementById('statErrors').textContent = totalErrors;

    // 设置结果消息
    if (resultMessage && result.message) {
      resultMessage.textContent = result.message;
    }

    // 检查是否有错误
    const hasErrors = result.hasErrors && result.allErrors && result.allErrors.length > 0;

    if (hasErrors) {
      errorSections.style.display = 'block';
      successMessage.style.display = 'none';

      // 更新各类错误
      updateErrorSection('validation', result.validationErrors, result.validationErrorCount);
      updateErrorSection('nameMismatch', result.nameMismatchErrors, result.nameMismatchCount);
      updateErrorSection('duplicate', result.duplicateErrors, result.duplicateCount);
      updateErrorSection('permission', result.permissionErrors, result.permissionErrorCount);
      updateErrorSection('enrollment', result.enrollmentErrors, result.enrollmentErrorCount);
      updateErrorSection('system', result.systemErrors, result.systemErrorCount);

      // 隐藏没有错误的分类
      hideEmptyErrorSections(result);
    } else {
      errorSections.style.display = 'none';
      successMessage.style.display = 'block';
    }

    resultSection.style.display = 'block';
    if (reShowBtn) {
      reShowBtn.style.display = 'none';
    }

    // 滚动到结果区域
    setTimeout(() => {
      resultSection.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  }

  function updateErrorSection(type, errors, count) {
    const errorCountElement = document.getElementById(type + 'ErrorCount');
    const errorListElement = document.getElementById(type + 'Errors');

    if (errorCountElement) {
      errorCountElement.textContent = count || 0;
    }

    if (errorListElement) {
      if (errors && errors.length > 0) {
        errorListElement.innerHTML = errors.map((error, index) =>
                `<div class="error-item">${error}</div>`
        ).join('');
      } else {
        errorListElement.innerHTML = '<div class="empty-state">暂无此类错误</div>';
      }
    }
  }

  function hideEmptyErrorSections(result) {
    const sections = [
      { type: 'validation', count: result.validationErrorCount },
      { type: 'nameMismatch', count: result.nameMismatchCount },
      { type: 'duplicate', count: result.duplicateCount },
      { type: 'permission', count: result.permissionErrorCount },
      { type: 'enrollment', count: result.enrollmentErrorCount },
      { type: 'system', count: result.systemErrorCount }
    ];

    sections.forEach(section => {
      const element = document.querySelector(`.error-section.${section.type}`);
      if (element) {
        if (!section.count || section.count === 0) {
          element.style.display = 'none';
        } else {
          element.style.display = 'block';
        }
      }
    });
  }

  function hideImportResult() {
    document.getElementById('importResultSection').style.display = 'none';
    document.getElementById('reShowResultBtn').style.display = 'block';
  }

  function showLastImportResult() {
    if (lastImportResult) {
      showImportResult(lastImportResult);
    } else {
      alert('没有可显示的导入结果');
    }
  }

  function clearImportResult() {
    document.getElementById('importResultSection').style.display = 'none';
    document.getElementById('reShowResultBtn').style.display = 'none';
    lastImportResult = null;
  }

  // 错误处理
  function showErrorModal(title, message) {
    const modal = document.createElement('div');
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
    `;

    const modalContent = document.createElement('div');
    modalContent.style.cssText = `
        background: white;
        padding: 25px;
        border-radius: 10px;
        max-width: 500px;
        box-shadow: 0 5px 15px rgba(0,0,0,0.3);
        text-align: center;
    `;

    // 安全处理 message 参数
    let messageHTML = '';
    if (message) {
      const messageStr = String(message);
      try {
        messageHTML = messageStr.split('\n').map(line => `<p style="margin: 5px 0;">${line}</p>`).join('');
      } catch (e) {
        messageHTML = `<p style="margin: 5px 0;">${messageStr}</p>`;
      }
    } else {
      messageHTML = '<p style="margin: 5px 0;">未知错误</p>';
    }

    modalContent.innerHTML = `
        <div style="color: red; font-size: 24px; margin-bottom: 15px;">❌</div>
        <h3 style="color: red; margin-bottom: 15px;">${title}</h3>
        <div style="margin-bottom: 20px; color: #333; text-align: left; background: #fff5f5; padding: 15px; border-radius: 5px;">
            ${messageHTML}
        </div>
        <button onclick="this.closest('.error-modal').remove()"
                style="padding: 10px 20px; background: #dc3545; color: white; border: none; border-radius: 5px; cursor: pointer;">
            关闭
        </button>
    `;

    modal.classList.add('error-modal');
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // 点击背景关闭
    modal.addEventListener('click', function(e) {
      if (e.target === modal) {
        modal.remove();
      }
    });
  }

  async function logout() {
    try {
      await authManager.logout();
    } catch (error) {
      console.error('退出登录失败:', error);
    }
  }
</script>
</body>
</html>