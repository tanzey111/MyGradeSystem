class GradeManager {
    constructor() {
        this.grades = [];
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

    showSuccess(message) {
        alert('成功: ' + message);
    }

    async callAPI(url, options = {}) {
        try {
            // 确保URL以斜杠开头
            if (!url.startsWith('/')) {
                url = '/' + url;
            }

            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers,
                },
                ...options,
            });

            // 检查响应状态
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `HTTP错误: ${response.status}`);
            }

            const result = await response.json();
            return result;

        } catch (error) {
            console.error('API调用错误:', error);
            throw error;
        }
    }

    // 获取所有成绩 (教师权限)
    async getAllGrades() {
        return await this.callAPI('/api/grades/all');
    }

    // 添加单个成绩
    async addGrade(gradeData) {
        return await this.callAPI('/api/grades', {
            method: 'POST',
            body: JSON.stringify(gradeData)
        });
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

    async callAPI(url, options = {}) {
        try {
            // 确保URL以斜杠开头
            if (!url.startsWith('/')) {
                url = '/' + url;
            }

            const response = await fetch(url, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers,
                },
                ...options,
            });

            const responseText = await response.text();
            let result;

            try {
                result = JSON.parse(responseText);
            } catch (e) {
                // 如果响应不是JSON，直接抛出错误文本
                throw new Error(responseText || `HTTP错误: ${response.status}`);
            }

            // 检查响应状态
            if (!response.ok) {
                // 如果后端返回了结构化的错误信息，直接抛出
                if (result && result.message) {
                    throw new Error(JSON.stringify(result));
                }
                throw new Error(result || `HTTP错误: ${response.status}`);
            }

            return result;

        } catch (error) {
            console.error('API调用错误:', error);
            throw error;
        }
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