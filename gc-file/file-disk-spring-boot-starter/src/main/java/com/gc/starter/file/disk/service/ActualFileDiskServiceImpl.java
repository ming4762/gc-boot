package com.gc.starter.file.disk.service;

import com.gc.common.base.utils.security.Md5Utils;
import com.gc.file.common.common.ActualFileServiceRegisterName;
import com.gc.file.common.constants.ActualFileServiceConstants;
import com.gc.file.common.pojo.bo.DiskFilePathBO;
import com.gc.file.common.properties.SmartFileProperties;
import com.gc.file.common.service.ActualFileService;
import lombok.SneakyThrows;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author shizhongming
 * 2020/11/5 10:54 下午
 */
public class ActualFileDiskServiceImpl implements ActualFileService {

    private final String basePath;

    public ActualFileDiskServiceImpl(SmartFileProperties properties) {
        this.basePath = properties.getBasePath();
    }

    /**
     * 保存文件
     * @param file 文件
     * @param filename 文件名
     * @return 文件id
     */
    @Override
    public @NonNull String save(@NonNull File file, String filename) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return this.save(inputStream, StringUtils.isEmpty(filename) ? file.getName() : filename);
        }
    }

    /**
     * 保存文件
     * @param inputStream 文件流
     * @param filename 文件名
     * @return 文件ID
     */
    @SneakyThrows
    @Override
    public @NonNull String save(@NonNull InputStream inputStream, String filename) {
        try (
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            IOUtils.copy(inputStream, outputStream);
            // 计算md5
            final String md5 = Md5Utils.md5(new ByteArrayInputStream(outputStream.toByteArray()));
            final DiskFilePathBO diskFilePath = new DiskFilePathBO(this.basePath, md5, filename);
            // 获取文件路径
            final Path folderPath = Paths.get(diskFilePath.getFolderPath());
            if (Files.notExists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            final String filePath = diskFilePath.getFilePath();
            final Path inPath = Paths.get(filePath);
            Files.copy(new ByteArrayInputStream(outputStream.toByteArray()), inPath);
            return diskFilePath.getFileId();
        }
    }

    /**
     * 删除文件
     * @param id 文件ID
     */
    @SneakyThrows
    @Override
    public void delete(@NonNull String id) {
        final String filePath = DiskFilePathBO.createById(id, this.basePath).getFilePath();
        final Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }

    /**
     * 批量删除文件
     * @param fileIdList 文件ID
     */
    @Override
    public void batchDelete(@NonNull List<String> fileIdList) {
        for (String fileId : fileIdList) {
            this.delete(fileId);
        }
    }

    /**
     * 下载文件
     * @param id 文件id
     * @return 文件流
     */
    @Override
    public InputStream download(@NonNull String id) throws FileNotFoundException {
        final String filePath = DiskFilePathBO.createById(id, this.basePath).getFilePath();
        final File file = new File(filePath);
        return new FileInputStream(file);
    }

    /**
     * 下载文件
     * @param id 文件ID
     * @param outputStream 输出流，文件信息会写入输出流
     */
    @SneakyThrows
    @Override
    public void download(@NonNull String id, @NonNull OutputStream outputStream) {
        final String filePath = DiskFilePathBO.createById(id, this.basePath).getFilePath();
        final File file = new File(filePath);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    /**
     * 获取文件的绝对路径
     * @param id 文件ID
     * @return 文件绝对路径
     */
    @Override
    public String getAbsolutePath(@NonNull String id) {
        return DiskFilePathBO.createById(id, this.basePath).getFilePath();
    }

    /**
     * 获取注册名字
     * @return 注册名字
     */
    @Override
    public ActualFileServiceRegisterName getRegisterName() {
        return ActualFileServiceRegisterName.builder()
                .dbName(ActualFileServiceConstants.DISK.name())
                .beanName(ActualFileServiceConstants.DISK.getServiceName())
                .build();
    }
}
