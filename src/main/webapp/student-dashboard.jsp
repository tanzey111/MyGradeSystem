<%-- WebContent/student-dashboard.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>学生后台 - 学生成绩查询系统</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="js/api.js"></script>
    <script src="js/auth.js"></script>
    <script src="js/grade.js"></script>
    <style>
        .time-status {
            padding: 15px;
            border-radius: 8px;
            margin: 10px 0;
            text-align: center;
        }
        .time-status.available {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .time-status.not-available {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .time-status.not-set {
            background: #fff3cd;
            color: #856404;
            border: 1px solid #ffeaa7;
        }
        .time-details {
            margin-top: 10px;
            font-size: 14px;
        }

        /* 确保模态框有正确的显示状态 */
        .modal {
            display: none; /* 默认隐藏 */
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
        }

        .modal-content {
            background-color: #fefefe;
            margin: 5% auto;
            padding: 20px;
            border-radius: 8px;
            width: 80%;
            max-width: 500px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        /* 当模态框需要显示时 */
        .modal.show {
            display: flex !important;
            align-items: center;
            justify-content: center;
        }
    </style>
</head>
<body>
<div class="header">
    <h1>学生成绩查询系统 - 学生后台</h1>
    <div class="user-info">
        <span id="userWelcome">学生</span> |
        <a href="#" onclick="logout()">退出登录</a>
    </div>
</div>

<div class="container">
    <!-- 欢迎信息和学生基本信息 -->
    <div class="card">
        <div class="welcome-header">
            <h2>欢迎回来，<span id="welcomeName">同学</span>！</h2>
            <div class="welcome-subtitle">以下是您的个人信息和最新动态</div>
        </div>

        <div class="student-profile">
            <div class="profile-main">
                <div class="profile-avatar">👨‍🎓</div>
                <div class="profile-details">
                    <div class="profile-name" id="profileName">-</div>
                    <div class="profile-id">学号: <span id="profileId">-</span></div>
                    <div class="profile-class">班级: <span id="profileClass">-</span></div>
                </div>
            </div>
        </div> <!-- 闭合 student-profile -->
    </div> <!-- 闭合 card -->

    <!-- 快速操作卡片 -->
    <div class="quick-actions">
        <div class="action-card" onclick="navigateTo('grade-query.jsp')">
            <div class="action-icon">📊</div>
            <div class="action-content">
                <h3>成绩查询</h3>
            </div>
        </div>

        <div class="action-card" onclick="viewGradeStats()">
            <div class="action-icon">📈</div>
            <div class="action-content">
                <h3>成绩统计</h3>
            </div>
        </div>

        <div class="action-card" onclick="checkQueryPeriod()">
            <div class="action-icon">⏰</div>
            <div class="action-content">
                <h3>查询时间</h3>
            </div>
        </div>

        <div class="action-card" onclick="showChangePasswordModal()">
            <div class="action-icon">🔒</div>
            <div class="action-content">
                <h3>修改密码</h3>
            </div>
        </div>
    </div>
</div> <!-- 闭合 container -->

<!-- 成绩统计模态框 -->
<div id="gradeStatsModal" class="modal">
    <div class="modal-content">
        <h3>成绩统计</h3>
        <div id="statsContent">
            <div class="loading-state">
                <div class="loading-spinner"></div>
                加载中...
            </div>
        </div>
        <div class="form-actions">
            <button type="button" onclick="hideModal('gradeStatsModal')" class="btn-cancel">关闭</button>
        </div>
    </div>
</div>

<!-- 查询时间详情模态框 -->
<div id="queryPeriodModal" class="modal">
    <div class="modal-content">
        <h3>📅 成绩查询时间详情</h3>
        <div id="queryPeriodContent">
            <div class="loading-state">
                <div class="loading-spinner"></div>
                加载中...
            </div>
        </div>
        <div class="form-actions">
            <button type="button" onclick="hideModal('queryPeriodModal')" class="btn-cancel">关闭</button>
        </div>
    </div>
</div>
<!-- 修改密码模态框 -->
<div id="changePasswordModal" class="modal">
    <div class="modal-content">
        <h3>修改密码</h3>
        <form id="changePasswordForm">
            <div class="form-group">
                <label>当前密码:</label>
                <input type="password" id="oldPassword" name="oldPassword" required>
            </div>
            <div class="form-group">
                <label>新密码:</label>
                <input type="password" id="newPassword" name="newPassword" required minlength="6">
                <small style="color: #666;">密码长度至少6位</small>
            </div>
            <div class="form-group">
                <label>确认新密码:</label>
                <input type="password" id="confirmPassword" name="confirmPassword" required minlength="6">
            </div>
            <div class="form-actions">
                <button type="submit" class="btn-primary">修改密码</button>
                <button type="button" onclick="hideModal('changePasswordModal')" class="btn-cancel">取消</button>
            </div>
        </form>
    </div>
</div>

<script>
    // 修复模态框显示/隐藏功能
    function initializeModals() {
        // 使用 jQuery 正确隐藏模态框，但保留显示能力
        $('.modal').hide();

        // 确保模态框的显示属性是可控制的
        $('.modal').css({
            'display': 'none',
            'visibility': 'visible'  // 确保可见性是可控制的
        });
    }

    // 在 DOM 加载完成后初始化
    document.addEventListener('DOMContentLoaded', function() {
        initializeModals();
    });

    // 页面加载完成后执行
    $(document).ready(function() {
        initializeModals();
        loadStudentInfo();
        loadQueryPeriodInfo();
    });

    // 增强的显示模态框函数
    function showModal(modalId) {
        // 先隐藏所有模态框
        $('.modal').hide();

        // 然后显示指定的模态框
        const modal = $('#' + modalId);
        modal.css({
            'display': 'flex',
            'visibility': 'visible',
            'opacity': '1'
        });
        modal.show();

        // 额外的保险：延迟再次确认显示
        setTimeout(() => {
            modal.css('display', 'flex').show();
        }, 10);
    }

    // 增强的隐藏模态框函数
    function hideModal(modalId) {
        const modal = $('#' + modalId);
        modal.hide();
        modal.css('display', 'none');
    }

    // 修改各个显示模态框的函数
    function showChangePasswordModal() {
        $('#changePasswordForm')[0].reset();
        showModal('changePasswordModal');
    }

    // 加载学生信息
    function loadStudentInfo() {
        try {
            const userData = sessionStorage.getItem('userData');
            if (userData) {
                const user = JSON.parse(userData);

                // 更新欢迎信息
                $('#userWelcome').text(`欢迎，${user.name} 同学`);
                $('#welcomeName').text(user.name);

                // 更新个人信息卡片
                $('#profileName').text(user.name);
                $('#profileId').text(user.id);
                $('#profileClass').text(user.class || '未设置');
            }
        } catch (error) {
            console.error('加载学生信息失败:', error);
            showError('加载学生信息失败');
        }
    }

    // 加载查询时间段信息
    async function loadQueryPeriodInfo() {
        try {
            // 这里可以调用API获取查询时间段配置
            // 暂时使用模拟数据
            $('#queryPeriodInfo').text('具体时间请关注系统通知');
        } catch (error) {
            console.error('加载查询时间信息失败:', error);
        }
    }

    // 查看成绩统计
    async function viewGradeStats() {
        try {
            const result = await gradeAPI.getMyGrades();
            const grades = result.data || [];

            if (grades.length === 0) {
                $('#statsContent').html('<p>暂无成绩数据</p>');
            } else {
                const stats = calculateGradeStats(grades);
                let html = `<div class="stats-grid">
                        <div class="stat-item">
                            <div class="stat-value">${stats.totalCourses}</div>
                            <div class="stat-label">总课程数</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">${stats.averageScore.toFixed(1)}</div>
                            <div class="stat-label">平均分</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">${stats.highestScore}</div>
                            <div class="stat-label">最高分</div>
                        </div>
                        <div class="stat-item">
                            <div class="stat-value">${stats.lowestScore}</div>
                            <div class="stat-label">最低分</div>
                        </div>
                    </div>`;

                $('#statsContent').html(html);
            }

            showModal('gradeStatsModal');
        } catch (error) {
            console.error('加载成绩统计失败:', error);
            $('#statsContent').html('<p>加载统计信息失败</p>');
            showModal('gradeStatsModal');
        }
    }

    // 检查查询时间段
    async function checkQueryPeriod() {
        try {
            $('#queryPeriodContent').html(
                '<div class="loading-state">' +
                '<div class="loading-spinner"></div>' +
                '加载中...' +
                '</div>'
            );

            showModal('queryPeriodModal');

            // 调用新的系统API端点
            const response = await fetch('./api/system/config', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include' // 确保包含会话cookie
            });

            console.log('API响应状态:', response.status);

            const responseText = await response.text();
            console.log('原始响应:', responseText);

            let result;
            try {
                result = JSON.parse(responseText);
            } catch (parseError) {
                console.error('JSON解析错误:', parseError);
                throw new Error('服务器返回了无效的JSON格式: ' + responseText.substring(0, 100));
            }

            if (!response.ok) {
                throw new Error(`HTTP错误 ${response.status}: ${result.message || '请求失败'}`);
            }

            if (!result.success) {
                throw new Error(result.message || '获取配置失败');
            }

            const config = result.data;
            console.log('系统配置:', config);

            let content = '';
            const currentTime = new Date().getTime();

            // 如果未启用时间限制或者配置为空
            if (!config || !config.is_active) {
                content =
                    '<div class="time-status available" style="text-align: center;">' +
                    '<div style="font-size: 48px; margin-bottom: 10px;">✅</div>' +
                    '<h3>成绩查询无时间限制</h3>' +
                    '<p>您可以随时查询您的成绩信息</p>' +
                    '</div>';
            } else {
                const startTime = config.start_time;
                const endTime = config.end_time;

                const formatTime = (timestamp) => {
                    if (!timestamp || timestamp === 0) return '未设置';
                    return new Date(timestamp).toLocaleString('zh-CN', {
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                    });
                };

                // 检查是否在时间范围内
                const isWithinTime = (!startTime || currentTime >= startTime) &&
                    (!endTime || currentTime <= endTime);

                if (isWithinTime) {
                    let timeDetails = '';
                    if (startTime && startTime > 0) {
                        timeDetails += '<p><strong>开始时间:</strong> ' + formatTime(startTime) + '</p>';
                    }
                    if (endTime && endTime > 0) {
                        timeDetails += '<p><strong>结束时间:</strong> ' + formatTime(endTime) + '</p>';
                    }

                    content =
                        '<div class="time-status available" style="text-align: center;">' +
                        '<div style="font-size: 48px; margin-bottom: 10px;">✅</div>' +
                        '<h3>当前在查询时间内</h3>' +
                        (timeDetails ? '<div style="margin: 15px 0;">' + timeDetails + '</div>' : '') +
                        '<p>您现在可以正常查询成绩</p>' +
                        '</div>';
                } else if (startTime && startTime > 0 && currentTime < startTime) {
                    content =
                        '<div class="time-status not-available" style="text-align: center;">' +
                        '<div style="font-size: 48px; margin-bottom: 10px;">⏳</div>' +
                        '<h3>查询尚未开始</h3>' +
                        '<div style="margin: 15px 0;">' +
                        '<p><strong>开始时间:</strong> ' + formatTime(startTime) + '</p>' +
                        '<p><strong>剩余时间:</strong> ' + calculateTimeRemaining(startTime) + '</p>' +
                        '</div>' +
                        '<p>请在指定时间后查询成绩</p>' +
                        '</div>';
                } else if (endTime && endTime > 0 && currentTime > endTime) {
                    content =
                        '<div class="time-status not-available" style="text-align: center;">' +
                        '<div style="font-size: 48px; margin-bottom: 10px;">❌</div>' +
                        '<h3>查询已结束</h3>' +
                        '<div style="margin: 15px 0;">' +
                        '<p><strong>结束时间:</strong> ' + formatTime(endTime) + '</p>' +
                        '</div>' +
                        '<p>成绩查询时间已过，请联系管理员</p>' +
                        '</div>';
                } else {
                    // 处理其他情况（比如配置异常）
                    content =
                        '<div class="time-status not-set" style="text-align: center;">' +
                        '<div style="font-size: 48px; margin-bottom: 10px;">❓</div>' +
                        '<h3>时间配置异常</h3>' +
                        '<p>请联系系统管理员检查时间配置</p>' +
                        '</div>';
                }
            }

            $('#queryPeriodContent').html(content);

        } catch (error) {
            console.error('获取查询时间配置失败:', error);
            $('#queryPeriodContent').html(
                '<div class="time-status not-set" style="text-align: center;">' +
                '<div style="font-size: 48px; margin-bottom: 10px;">⚠️</div>' +
                '<h3>无法获取时间配置</h3>' +
                '<p>请稍后重试或联系系统管理员</p>' +
                '<p style="color: #666; font-size: 12px; margin-top: 10px;">错误: ' + (error.message || '未知错误') + '</p>' +
                '</div>'
            );
        }
    }

    // 计算剩余时间
    function calculateTimeRemaining(targetTime) {
        const now = new Date().getTime();
        const diff = targetTime - now;

        if (diff <= 0) return '已开始';

        const days = Math.floor(diff / (1000 * 60 * 60 * 24));
        const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));

        if (days > 0) {
            return days + '天' + hours + '小时';
        } else if (hours > 0) {
            return hours + '小时' + minutes + '分钟';
        } else {
            return minutes + '分钟';
        }
    }

    // 计算成绩统计
    function calculateGradeStats(grades) {
        let totalCourses = grades.length;
        let totalScore = 0;
        let highestScore = 0;
        let lowestScore = 100;

        grades.forEach(grade => {
            const score = grade.score;
            totalScore += score;

            if (score > highestScore) highestScore = score;
            if (score < lowestScore) lowestScore = score;
        });

        return {
            totalCourses,
            averageScore: totalCourses > 0 ? totalScore / totalCourses : 0,
            highestScore,
            lowestScore
        };
    }

    // 页面导航
    function navigateTo(page) {
        window.location.href = page;
    }

    // 工具函数
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // 显示错误消息
    function showError(message) {
        alert('错误: ' + message);
    }

    // 处理修改密码表单提交
    $('#changePasswordForm').on('submit', async function(e) {
        e.preventDefault();

        const oldPassword = $('#oldPassword').val();
        const newPassword = $('#newPassword').val();
        const confirmPassword = $('#confirmPassword').val();

        if (newPassword !== confirmPassword) {
            alert('新密码和确认密码不一致');
            return;
        }

        if (newPassword.length < 6) {
            alert('密码长度至少6位');
            return;
        }

        try {
            const result = await gradeAPI.callAPI('api/student/change-password', {
                method: 'POST',
                body: JSON.stringify({
                    oldPassword: oldPassword,
                    newPassword: newPassword
                })
            });

            alert('密码修改成功');
            hideModal('changePasswordModal');

        } catch (error) {
            alert('密码修改失败: ' + error.message);
        }
    });

    // 退出登录
    async function logout() {
        await authManager.logout();
    }
</script>
</body>
</html>