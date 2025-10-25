package com.gradesystem.controller;

import com.gradesystem.service.GradeService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/upload/grades")
public class UploadApiServlet extends BaseApiServlet {
    private GradeService gradeService = new GradeService();

    // 上传配置
    private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE = 1024 * 1024 * 10;    // 10MB
    private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 15; // 15MB


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查用户权限
        String userRole = getCurrentUserRole(request);
        String teacherId = getCurrentUserId(request);

        if (!"teacher".equals(userRole) && !"admin".equals(userRole)) {
            sendError(response, "需要教师或管理员权限");
            return;
        }

        // 检查是否为multipart/form-data上传
        if (!ServletFileUpload.isMultipartContent(request)) {
            sendError(response, "表单必须包含文件上传");
            return;
        }

        // 配置上传参数
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(MAX_FILE_SIZE);
        upload.setSizeMax(MAX_REQUEST_SIZE);

        // 创建上传目录
        String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        try {
            // 解析请求内容
            List<FileItem> formItems = upload.parseRequest(request);
            Map<String, Object> result = new HashMap<>();
            String fileName = "";

            if (formItems != null && !formItems.isEmpty()) {
                for (FileItem item : formItems) {
                    // 处理文件字段
                    if (!item.isFormField() && "file".equals(item.getFieldName())) {
                        fileName = new File(item.getName()).getName();
                        String filePath = uploadPath + File.separator + System.currentTimeMillis() + "_" + fileName;
                        File storeFile = new File(filePath);

                        // 保存文件到服务器
                        item.write(storeFile);

                        // 根据文件类型调用不同的导入方法，传入teacherId
                        if (fileName.toLowerCase().endsWith(".csv")) {
                            result = gradeService.importGradesFromCSV(filePath, teacherId);
                        } else if (fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls")) {
                            result = gradeService.importGradesFromExcel(filePath, teacherId);
                        } else {
                            sendError(response, "不支持的文件格式: " + fileName + "，仅支持 CSV 和 Excel 文件");
                            return;
                        }

                        // 删除临时文件
                        storeFile.delete();
                        break;
                    }
                }
            }

            result.put("fileName", fileName);
            sendSuccess(response, result);

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "文件上传失败: " + e.getMessage());
        }
    }
}