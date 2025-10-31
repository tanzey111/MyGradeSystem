<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录 - 学生成绩查询系统</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
    <style>
        /* Toast通知样式 - 适配前后端分离 */
        .toast {
            position: fixed;
            top: 30px;
            right: 30px;
            padding: 16px 24px;
            background: white;
            color: #333;
            border-radius: 8px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
            display: flex;
            align-items: center;
            gap: 12px;
            z-index: 10000;
            transform: translateX(150%);
            transition: transform 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
            border-left: 4px solid #4361ee;
            max-width: 350px;
            min-width: 280px;
        }

        .toast.show {
            transform: translateX(0);
        }

        .toast.success {
            border-left-color: #38a169;
            background: #f0fff4;
        }

        .toast.error {
            border-left-color: #e53e3e;
            background: #fff5f5;
        }

        .toast.info {
            border-left-color: #3182ce;
            background: #ebf8ff;
        }

        .toast-icon {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            flex-shrink: 0;
            font-size: 14px;
        }

        .toast.success .toast-icon {
            background: #38a169;
            color: white;
        }

        .toast.error .toast-icon {
            background: #e53e3e;
            color: white;
        }

        .toast.info .toast-icon {
            background: #3182ce;
            color: white;
        }

        .toast-message {
            flex: 1;
            font-size: 14px;
            line-height: 1.4;
        }

        .toast-close {
            background: none;
            border: none;
            color: #718096;
            cursor: pointer;
            font-size: 18px;
            opacity: 0.7;
            transition: opacity 0.2s;
            padding: 0;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .toast-close:hover {
            opacity: 1;
            color: #4a5568;
        }

        /* 登录页面基础样式 */
        .login-container {
            max-width: 400px;
            margin: 100px auto;
            padding: 2.5rem;
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            text-align: center;
        }

        .login-container h2 {
            margin-bottom: 2rem;
            color: #4a5568;
            font-weight: 300;
        }

        .form-group {
            margin-bottom: 1.5rem;
            text-align: left;
        }

        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
            color: #4a5568;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .btn-login {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 0.75rem 2rem;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 500;
            transition: all 0.3s ease;
            width: 100%;
        }

        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        .btn-login:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
            box-shadow: none;
        }

        .btn-login.loading::after {
            content: '';
            display: inline-block;
            width: 16px;
            height: 16px;
            margin-left: 8px;
            border: 2px solid transparent;
            border-top: 2px solid white;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            vertical-align: middle;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
    </style>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="js/api.js"></script>
    <script src="js/auth.js"></script>
</head>
<body>
<div class="login-container">
    <h2>学生成绩查询系统</h2>

    <form id="loginForm">
        <div class="form-group">
            <label for="username">学号/工号:</label>
            <input type="text" id="username" name="username" required>
        </div>

        <div class="form-group">
            <label for="password">密码:</label>
            <input type="password" id="password" name="password" required>
        </div>

        <div class="form-group">
            <label for="role">身份:</label>
            <select id="role" name="role">
                <option value="student">学生</option>
                <option value="teacher">教师</option>
                <option value="admin">管理员</option>
            </select>
        </div>

        <button type="submit" class="btn-login" id="loginBtn">登录</button>
    </form>
</div>

<script>
    $(document).ready(function() {
        // 表单提交事件
        $('#loginForm').on('submit', async function(e) {
            e.preventDefault();

            const username = $('#username').val().trim();
            const password = $('#password').val();
            const role = $('#role').val();
            const loginBtn = $('#loginBtn');

            // 验证输入
            if (!username) {
                showToast('请输入学号/工号', 'error');
                $('#username').focus();
                return;
            }

            if (!password) {
                showToast('请输入密码', 'error');
                $('#password').focus();
                return;
            }

            try {
                // 显示加载状态
                loginBtn.prop('disabled', true).addClass('loading').text('登录中...');

                // 使用API进行登录
                const result = await authManager.login(username, password, role);

                showToast('登录成功，正在跳转...', 'success');

                // 登录成功后跳转
                setTimeout(() => {
                    switch(role) {
                        case 'student':
                            window.location.href = 'student-dashboard.jsp';
                            break;
                        case 'teacher':
                            window.location.href = 'teacher-dashboard.jsp';
                            break;
                        case 'admin':
                            window.location.href = 'admin-dashboard.jsp';
                            break;
                        default:
                            window.location.href = 'student-dashboard.jsp';
                    }
                }, 1500);

            } catch (error) {
                console.error('登录失败:', error);
                showToast('登录失败: ' + (error.message || '请检查用户名和密码'), 'error');

                // 恢复按钮状态
                loginBtn.prop('disabled', false).removeClass('loading').text('登录');
            }
        });

        // 输入框回车事件
        $('#username, #password').on('keypress', function(e) {
            if (e.which === 13) {
                $('#loginForm').submit();
            }
        });

        // 自动聚焦到用户名输入框
        $('#username').focus();
    });

    // 显示Toast通知函数
    function showToast(message, type = 'info') {
        // 创建唯一的ID
        const toastId = 'toast-' + Date.now();

        // 确定图标
        let icon = 'i';
        if (type === 'success') icon = '✓';
        if (type === 'error') icon = '!';
        if (type === 'info') icon = 'i';

        // 创建Toast元素
        const toast = $(`
            <div id="${toastId}" class="toast ${type}">
                <div class="toast-icon">${icon}</div>
                <div class="toast-message">${message}</div>
                <button class="toast-close">&times;</button>
            </div>
        `);

        // 添加到页面
        $('body').append(toast);

        // 显示动画
        setTimeout(() => {
            toast.addClass('show');
        }, 10);

        // 关闭按钮事件
        toast.find('.toast-close').on('click', function() {
            hideToast(toastId);
        });

        // 30秒后自动消失
        setTimeout(() => {
            if ($('#' + toastId).length) {
                hideToast(toastId);
            }
        }, 3000);

        return toastId;
    }

    // 隐藏Toast函数
    function hideToast(toastId) {
        const toast = $('#' + toastId);
        toast.removeClass('show');
        setTimeout(() => {
            toast.remove();
        }, 400);
    }
</script>
</body>
</html>