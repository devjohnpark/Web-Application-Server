package org.dochi.http.request.multipart;

import java.io.*;
import java.util.UUID;

public class MultipartFileManager {
    private static final MultipartFileManager INSTANCE = new MultipartFileManager();
    private final String rootPath = "multipart-tmp-file";

    // 클래스의 static 멤버에 처음 접근할 때 호출 된다. 즉, 런타임에 getInstance() 정적 메서드가 처음 호출될 때 생성자 코드 실행을 한다.
    private MultipartFileManager() {
        createRootDirectory();
    }

    public static MultipartFileManager getInstance() {
        return INSTANCE;
    }

    private void createRootDirectory() {
        File directory = new File(rootPath);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new IllegalStateException("Can't create directory: " + directory.getAbsolutePath());
            }
        }
    }

    public void uploadFile(String fileName, UUID uuid, byte[] content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(getFilePath(fileName, uuid))) {
            fos.write(content);
            fos.flush();
        } catch (IOException e) {
            throw new IOException("Failed to upload file: " + fileName, e);
        }
    }

    public byte[] downloadFile(String fileName, UUID uuid) throws IOException {
        File file = new File(getFilePath(fileName, uuid));

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            throw new IOException("Failed to download file: " + fileName, e);
        }
    }

    public void deleteFile(String fileName, UUID uuid) throws IOException {
        File file = new File(getFilePath(fileName, uuid));

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + fileName);
        }
    }

    private String getFilePath(String fileName, UUID uuid) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is null or empty");
        }
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid file name: " + fileName);
        }
        if (uuid == null) {
            throw new IllegalArgumentException("uuid is null");
        }
        return rootPath + "/" + fileName + "_" + uuid.toString() + ".bin";
    }
}
