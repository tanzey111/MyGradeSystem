package com.gradesystem.controller;

import com.gradesystem.dao.DatabaseUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import static com.gradesystem.dao.DatabaseUtil.updatePasswordsToMD5;

/**
 * 应用启动监听器
 * 在Web应用启动时自动初始化数据库
 */
@WebListener
public class ApplicationStartupListener implements ServletContextListener {

    private static final String INITIALIZED_ATTRIBUTE = "databaseInitialized";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("学生成绩查询系统启动中...");

        try {
            // 测试数据库连接
            if (DatabaseUtil.testConnection()) {
                System.out.println("数据库连接测试成功");

                // 检查是否已经初始化过（避免重复初始化）
                Boolean initialized = (Boolean) sce.getServletContext().getAttribute(INITIALIZED_ATTRIBUTE);
                if (initialized == null || !initialized) {
                    // 执行数据库初始化
                    DatabaseUtil.initializeDatabase();
                    updatePasswordsToMD5();

                    // 标记为已初始化
                    sce.getServletContext().setAttribute(INITIALIZED_ATTRIBUTE, true);
                    System.out.println("数据库初始化完成");
                } else {
                    System.out.println("数据库已经初始化过，跳过初始化过程");
                }
            } else {
                System.err.println("数据库连接测试失败，请检查数据库配置");
            }
        } catch (Exception e) {
            System.err.println("系统启动失败: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("学生成绩查询系统启动完成");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("学生成绩查询系统关闭");
    }
}