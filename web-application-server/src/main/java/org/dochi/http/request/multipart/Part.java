package org.dochi.http.request.multipart;

import org.dochi.webresource.ResourceType;

import java.io.IOException;
import java.util.UUID;


// MultipartFileManager 예외 다루기
public class Part {
    private static final MultipartFileManager multipartFileManager = MultipartFileManager.getInstance();
    private byte[] content = null;
    private final String contentType;
    private final String fileName;
    private final UUID uuid;

    public Part() {
        this(null, null);
    }

    public Part(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
        this.fileName = null;
        this.uuid = null;
    }

    public Part(byte[] content, String contentType, String fileName, UUID uuid) throws IOException {
        multipartFileManager.uploadFile(fileName, uuid, content);
        this.contentType = contentType;
        this.fileName = fileName;
        this.uuid = uuid;
    }

    public String getFileName() { return fileName; }

    // charset 등 parameter name, value 손 좀 봐야함
    public String getContentType() {
        if (content != null && content.length != 0 && contentType == null) {
            return ResourceType.TEXT.getContentType("UTF-8");
        }
        return contentType;
    }

    public boolean isFile() {
        return fileName != null && uuid != null;
    }

//    public byte[] getContent() throws HttpStatusException, IOException {
//        if (isFile()) {
//            try {
//                return multipartFileManager.downloadFile(fileName, uuid);
//            } catch (FileNotFoundException e) {
//                throw new HttpStatusException(HttpStatus.NOT_FOUND, e.getMessage());
//            }
//        }
//        return content;
//    }
//
//    public void removeFile() throws HttpStatusException, IOException {
//        if (isFile()) {
//            try {
//                multipartFileManager.deleteFile(fileName, uuid);
//            } catch (FileNotFoundException e) {
//                throw new HttpStatusException(HttpStatus.NOT_FOUND, e.getMessage());
//            }
//        }
//    }

    public byte[] getContent() throws IOException {
        if (isFile()) {
            return multipartFileManager.downloadFile(fileName, uuid);
        }
        return content;
    }

    public void removeFile() throws IOException {
        if (isFile()) {
            multipartFileManager.deleteFile(fileName, uuid);
        }
    }
}