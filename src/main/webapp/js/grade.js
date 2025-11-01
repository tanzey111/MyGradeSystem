class GradeManager {
    constructor() {
        this.grades = [];
        this.baseURL = window.location.origin + window.location.pathname.replace(/[^/]*$/, '');
    }

    // 加载学生成绩
    async loadMyGrades() {
        try {
            showLoading();
            const result = await gradeAPI.getMyGrades();
            this.grades = result.data || [];
            this.renderGrades(this.grades);
            hideLoading();
        } catch (error) {
            hideLoading();
            this.showError('加载成绩失败: ' + error.message);
        }
    }

    // 渲染成绩表格
    renderGrades(grades) {
        const tbody = document.getElementById('gradesTableBody');
        if (!tbody) return;

        if (grades.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="no-data">暂无成绩数据</td></tr>';
            return;
        }

        tbody.innerHTML = grades.map(grade => `
            <tr>
                <td>${this.escapeHtml(grade.courseName)}</td>
                <td>${grade.score}</td>
                <td>${this.escapeHtml(grade.semester || '')}</td>
            </tr>
        `).join('');
    }


    // 工具方法
    escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    showError(message) {
        // 使用Toast或alert显示错误
        alert('错误: ' + message);
    }

    async callAPI(url, options = {}) {
        try {
            // 修复：使用新的变量名fullUrl 避免冲突
            const fullUrl = `${this.baseURL}${url.startsWith('/') ? url : '/' + url}`;
            console.log('API请求URL:', fullUrl);

            const config = {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers,
                },
                credentials: 'include',
                ...options,
            };

            const response = await fetch(fullUrl, config);

            const responseText = await response.text();
            let result;

            try {
                result = JSON.parse(responseText);
            } catch (e) {
                throw new Error(responseText || `HTTP错误: ${response.status}`);
            }

            if (!response.ok) {
                if (result && result.message) {
                    throw new Error(result.message);
                }
                throw new Error(result || `HTTP错误: ${response.status}`);
            }

            return result;

        } catch (error) {
            console.error('API调用错误:', error);
            throw error;
        }
    }


    // 添加单个成绩
    async addGrade(gradeData) {
        try {
            const url = `${this.baseURL}/api/grades`;
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8',
                },
                body: JSON.stringify(gradeData),
                credentials: 'include'
            });

            const responseText = await response.text();
            console.log('原始响应:', responseText);

            let result;
            try {
                result = JSON.parse(responseText);
            } catch (e) {
                console.error('响应不是有效的JSON:', responseText);
                throw new Error('服务器返回了无效的响应格式');
            }

            if (!response.ok) {
                console.error('HTTP错误:', response.status, result);
                throw new Error(result.message || result.error || `HTTP错误: ${response.status}`);
            }

            return result;

        } catch (error) {
            console.error('添加成绩API调用失败:', error);
            throw error;
        }
    }

    // 获取单个成绩信息
    async getGrade(gradeId) {
        return await this.callAPI(`/api/grades/${gradeId}`);
    }

    // 更新成绩
    async updateGrade(gradeId, gradeData) {
        return await this.callAPI(`/api/grades/${gradeId}`, {
            method: 'PUT',
            body: JSON.stringify(gradeData)
        });
    }

    // 删除成绩
    async deleteGrade(gradeId) {
        return await this.callAPI(`/api/grades/${gradeId}`, {
            method: 'DELETE'
        });
    }

    // 获取我的成绩 (学生权限)
    async getMyGrades() {
        return await this.callAPI('/api/grades/my');
    }

    // 获取教师可管理的成绩
    async getGradesByTeacher() {
        return await this.callAPI('/api/grades/all');
    }

    // 检查教师是否有权限管理课程
    async checkCoursePermission(courseName) {
        return await this.callAPI(`/api/teacher/courses/check?courseName=${encodeURIComponent(courseName)}`);
    }

    // 获取教师所教课程列表
    async getTeacherCourses() {
        return await this.callAPI('/api/teacher/courses');
    }

}

const gradeManager = new GradeManager();

// 工具函数
function showLoading() {
    const loading = document.getElementById('loading') || createLoadingElement();
    loading.style.display = 'block';
}

function hideLoading() {
    const loading = document.getElementById('loading');
    if (loading) loading.style.display = 'none';
}

function createLoadingElement() {
    const loading = document.createElement('div');
    loading.id = 'loading';
    loading.innerHTML = '加载中...';
    loading.style.cssText = `
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: rgba(0,0,0,0.8);
        color: white;
        padding: 20px;
        border-radius: 5px;
        z-index: 9999;
    `;
    document.body.appendChild(loading);
    return loading;
}