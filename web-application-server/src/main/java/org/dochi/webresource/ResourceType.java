package org.dochi.webresource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

// ResourceType -> MediaType
// MediaType: text/html; charset=utf8는 하나의 미디어 타입(media type)
// text/html은 그 타입(type/subtype)
// charset=utf8은 파라미터(parameter)
public enum ResourceType {
    TEXT(".txt", "text/plain"),
    HTML(".html", "text/html"),
    JS(".js", "application/javascript"),
    XML(".xml", "application/xml"),
    CSS(".css", "text/css"),
    ICO(".ico", "image/x-icon"),
    PNG(".png", "image/png"),
    JPEG(".jpeg", "image/jpeg"),
    JPG(".jpg", "image/jpg"),
    JSON(".json", "application/json"),
    URL(null, "application/x-www-form-urlencoded"),
    MULTIPART(null, "multipart/form-data"),
    OCTET_STREAM(null, "application/octet-stream"),
    UNKNOWN(null, null);

    private static final Logger log = LoggerFactory.getLogger(ResourceType.class);
    private final String extension;
    private final String mimeType;

    ResourceType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    // MediaType
    public String getMimeType() {
        return mimeType;
    }

    public String getContentType(String parameter) {
        if (parameter != null && !parameter.isEmpty()) {
            if (this == TEXT || this == HTML || this == JS || this == XML || this == CSS) {
                return mimeType + "; charset=" + parameter;
            } else if (this == MULTIPART) {
                return mimeType + "; boundary=" + parameter;
            }
        }
        return mimeType;
    }

    public boolean isEqualMimeType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        return contentType.split(";")[0].equalsIgnoreCase(mimeType);
    }

    public String getContentTypeParamValue(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return null;
        }
        String[] contentTypeParams = contentType.split(";");
        if (contentTypeParams.length < 2) {
            return null;
        }
        String[] mediaTypeParams = contentTypeParams[1].trim().split("=");
        if (mediaTypeParams.length < 2) {
            return null;
        }
        return mediaTypeParams[1];
    }

    private static String toExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) { // . 이 파일 이름의 중간에 있는 경우
            return fileName.substring(dotIndex); // . 포함한 확장자 반환
        }
        return ""; // 확장자가 없는 경우
    }

    public static ResourceType fromFilePath(Path filePath) {
        if (filePath != null) {
            String extension = toExtension(filePath.getFileName().toString());
            for (ResourceType resourceType : values()) {
                if (extension.equalsIgnoreCase(resourceType.getExtension())) {
                    return resourceType;
                }
            }
        }
        return UNKNOWN;
    }

    public static ResourceType fromMimeType(String mimeType) {
        if (mimeType != null) {
            for (ResourceType resourceType : values()) {
                if (mimeType.equalsIgnoreCase(resourceType.getMimeType())) {
                    return resourceType;
                }
            }
        }
        return UNKNOWN;
    }
}