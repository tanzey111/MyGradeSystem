class AuthManager {
    constructor() {
        this.currentUser = null;
        this.init();
    }

    init() {
        // 检查登录状态
        this.checkAuthStatus();
    }

    async login(username, password, role) {
        try {
            const result = await gradeAPI.login(username, password, role);
            this.currentUser = result.data;

            // 将用户数据存储到sessionStorage
            sessionStorage.setItem('userData', JSON.stringify(result.data));

            // 根据角色跳转
            if (role === 'student') {
                window.location.href = 'student-dashboard.jsp';
            } else if (role === 'teacher') {
                window.location.href = 'teacher-dashboard.jsp';
            }  else if (role === 'admin') {
                window.location.href = 'admin-dashboard.jsp';
            }

            return result;
        } catch (error) {
            throw error;
        }
    }

    async logout() {
        try {
            await gradeAPI.logout();
            this.currentUser = null;

            // 清除 sessionStorage
            sessionStorage.removeItem('userData');
            sessionStorage.removeItem('isLoggedIn');
            sessionStorage.removeItem('userRole');

            window.location.href = 'login.jsp';
        } catch (error) {
            console.error('退出登录失败:', error);
        }
    }

    checkAuthStatus() {
        // 可以从sessionStorage或cookie检查登录状态
        const userData = sessionStorage.getItem('userData');
        if (userData) {
            this.currentUser = JSON.parse(userData);
        }
    }

    isAuthenticated() {
        return this.currentUser !== null;
    }

    isTeacher() {
        return this.currentUser && this.currentUser.role === 'teacher';
    }

    isStudent() {
        return this.currentUser && this.currentUser.role === 'student';
    }

    isAdmin() {
        return this.currentUser && this.currentUser.role === 'admin';
    }
}

const authManager = new AuthManager();