class GradeSystemAPI {
    constructor() {
        this.baseURL = window.location.origin + window.location.pathname.replace(/[^/]*$/, '');
    }

    // 通用API调用方法
    async callAPI(endpoint, options = {}) {
        const url = `${this.baseURL}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json; charset=UTF-8',
            },
            credentials: 'same-origin',
            ...options
        };

        try {
            const response = await fetch(url, config);
            const result = await response.json();

            if (!result.success) {
                throw new Error(result.error || result.message || '请求失败');
            }

            return result;
        } catch (error) {
            console.error('API调用错误:', error);
            throw error;
        }
    }

    // 认证相关API
    async login(username, password, role) {
        return this.callAPI('api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password, role })
        });
    }

    async logout() {
        return this.callAPI('api/auth/logout', {
            method: 'POST'
        });
    }

    // 成绩相关API
    async getMyGrades() {
        return this.callAPI('api/grades/my');
    }

    async getAllGrades() {
        return this.callAPI('api/grades/all');
    }

    async uploadGrades(formData) {
        // 文件上传特殊处理
        const response = await fetch(`${this.baseURL}api/upload/grades`, {
            method: 'POST',
            body: formData,
            credentials: 'same-origin'
        });

        const result = await response.json();
        if (!result.success) {
            throw new Error(result.error || '上传失败');
        }

        return result;
    }
}

// 创建全局API实例
const gradeAPI = new GradeSystemAPI();