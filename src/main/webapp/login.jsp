<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>登录 - 学生成绩查询系统</title>
    <link rel="stylesheet" type="text/css" href="css/style.css">
    <script src="js/api.js"></script>
    <script src="js/auth.js"></script>
</head>
<body>
<div class="login-container">
    <h2>学生成绩查询系统</h2>

    <form id="loginForm">
        <div class="form-group">
            <label for="username">用户名/学号:</label>
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

        <button type="submit" class="btn-login">登录</button>
    </form>

    <div id="message" class="message" style="display: none;"></div>
</div>

<script>
    document.getElementById('loginForm').addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const role = document.getElementById('role').value;
        const messageDiv = document.getElementById('message');

        try {
            const result = await authManager.login(username, password, role);
            showMessage('登录成功，正在跳转...', 'success');
        } catch (error) {
            showMessage('登录失败: ' + error.message, 'error');
        }
    });

    function showMessage(text, type) {
        const messageDiv = document.getElementById('message');
        messageDiv.textContent = text;
        messageDiv.className = `message ${type}`;
        messageDiv.style.display = 'block';

        if (type == 'success') {
            setTimeout(() => {
                messageDiv.style.display = 'none';
            }, 2000);
        }
    }
</script>
</body>
</html>
