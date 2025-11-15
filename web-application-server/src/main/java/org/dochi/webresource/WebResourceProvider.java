package org.dochi.webresource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class WebResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(WebResourceProvider.class);
    private static final String DEFAULT_PAGE = "index.html";
    private final Path rootDirPath;
    private final ResourceLocation resourceLocation;

    private enum ResourceLocation {
        DEFAULT,
        EMBEDDED_JAR,
    }

    public WebResourceProvider(Path rootDirPath) {
        validWebRootDirectory(rootDirPath);
        this.rootDirPath = rootDirPath;
        this.resourceLocation = determineResourceLocation(rootDirPath);
    }

    public Resource getResource(String resourcePath) {
        // absolute path (start with '/'): webapp.resolve(/index.html) -> /index.html
        // relative path (start with '/'): webapp.resolve(index.html) -> webapp/index.html
        return getResourceInternal(rootDirPath.resolve(normalizeFilePath(validateRootDirectoryPath(resourcePath))));
    }

    private Resource getResourceInternal(Path resourcePath) {
        if (resourceLocation == ResourceLocation.DEFAULT) {
            return new Resource(readResourceInProject(resourcePath), ResourceType.fromFilePath(resourcePath).getMimeType());
        }
        return new Resource(readResourceInEmbeddedJar(resourcePath.toString()), ResourceType.fromFilePath(resourcePath).getMimeType());
    }

    private void validWebRootDirectory(Path rootDirPath) {
        if (rootDirPath == null) {
            throw new IllegalArgumentException("Root directory path cannot be null");
        }
        if (rootDirPath.toString().startsWith("/")) {
            throw new IllegalArgumentException("Root directory path cannot be start with /");
        }
    }

    private ResourceLocation determineResourceLocation(Path rootDirPath) {

        if (isRootDirectoryInProject(rootDirPath)) {
            return ResourceLocation.DEFAULT;
        }

        if (isRootDirectoryInEmbeddedJar(
                getClass().getProtectionDomain().getCodeSource().getLocation().getPath(),
                rootDirPath.toString())) {
            return ResourceLocation.EMBEDDED_JAR;
        }

        throw new IllegalArgumentException("Not found root directory for web resource: {}" + rootDirPath);
    }

    private boolean isRootDirectoryInProject(Path rootDirPath) {
        return Files.exists(rootDirPath) && Files.isDirectory(rootDirPath);
    }

    private String normalizeFilePath(String filePath) {
        if (filePath.equals("/")) {
            filePath += DEFAULT_PAGE;
        }
        return filePath.startsWith("/") ? filePath.substring(1) : filePath;
    }

    private byte[] readResourceInEmbeddedJar(String resourcePath) {
        // WebResourceProvider를 로드한 클래스 로더를 가져옴
        // ClassLoader의 getResourceAsStream 메서드는 Classpath에서 지정된 경로의 리소스를 BufferedSocketInputStream 형태로 반환(주로 JAR 파일 내부의 파일을 읽을 때 사용)
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                return in.readAllBytes();
            }
            log.error("Resource not found in jar: {}", resourcePath);
        } catch (IOException e) {
            log.error("Failed to read resource path in jar: {}, exception: {}", resourcePath, e.getMessage());
        }
        return null;
    }

    private byte[] readResourceInProject(Path path) {
        try {
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
            log.error("Resource not found: {}", path.toString());
        } catch (IOException e) {
            log.error("Failed to read resource path: {}, exception: {}", path.toString(), e.getMessage());
        }
        return null;
    }

    private String validateRootDirectoryPath(String resourcePath) {
        if (resourcePath == null) {
            throw new IllegalArgumentException("Resource path cannot be null");
        }
        if (resourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be empty");
        }
        return resourcePath;
    }

    private boolean isRootDirectoryInEmbeddedJar(String jarPath, String rootDirPath) {
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 디렉터리 자체 또는 디렉터리로 시작하는 파일 확인
                if (entryName.equals(rootDirPath + "/") ||
                        entryName.startsWith(rootDirPath + "/")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
