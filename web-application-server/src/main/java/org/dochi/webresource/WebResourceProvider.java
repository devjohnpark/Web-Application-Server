package org.dochi.webresource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class WebResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(WebResourceProvider.class);
    private static final String DEFAULT_PAGE = "index.html";
    private final Path rootDirPath;
    private final boolean isJar;

    public WebResourceProvider(Path rootDirPath) {
        URL url = getClassLocation();
        this.isJar = isJar(url);
        this.rootDirPath = validWebRootDirectory(rootDirPath, url);
        log.debug("WebResourceProvider is created");
    }

    public Resource getResource(String resourcePath) {
        // absolute path (start with '/'): webapp.resolve(/index.html) -> /index.html
        // relative path (start with '/'): webapp.resolve(index.html) -> webapp/index.html
        return getResourceInternal(rootDirPath.resolve(normalizeFilePath(validateResourcePath(resourcePath))));
    }

    private boolean isValidatedWebappDirectory(Path rootDirPath) {
        return Files.exists(rootDirPath) && Files.isDirectory(rootDirPath);
    }

    private Path validWebRootDirectory(Path rootDirPath, URL url) {
        if (!isValidatedWebRootDirectory(rootDirPath, url)) {
            throw new IllegalArgumentException("Webapp directory is not valid");
        }
        return rootDirPath;
    }

    private URL getClassLocation() {
        /*
        WebResourceProvider.class: Get metadata of WebResourceProvider class
        getProtectionDomain(): Get class's protection domain info
        getCodeSource(): Get source info of class load
        getLocation(): soruce location convert to url
         */
        return getClass().getProtectionDomain().getCodeSource().getLocation();
    }

    private boolean isJar(URL url) {
        return url.getPath().endsWith(".jar");
    }

    private Resource getResourceInternal(Path resourcePath) {
        if (this.isJar) {
            return new Resource(readResourceInJar(resourcePath.toString()), ResourceType.fromFilePath(resourcePath).getMimeType());
        }
        return new Resource(readResource(resourcePath), ResourceType.fromFilePath(resourcePath).getMimeType());
    }

    private String normalizeFilePath(String filePath) {
        if (filePath.equals("/")) {
            filePath += DEFAULT_PAGE;
        }
        return filePath.startsWith("/") ? filePath.substring(1) : filePath;
    }

    private byte[] readResourceInJar(String resourcePath) {
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

    private byte[] readResource(Path path) {
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

    private String validateResourcePath(String resourcePath) {
        if (resourcePath == null) {
            throw new IllegalArgumentException("Resource path cannot be null");
        }
        if (resourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be empty");
        }
        return resourcePath;
    }

    private boolean isValidatedWebRootDirectory(Path rootDirPath, URL url) {
        if (rootDirPath == null) {
            throw new IllegalArgumentException("Root directory path cannot be null");
        }
        if (rootDirPath.toString().startsWith("/")) {
            throw new IllegalArgumentException("Root directory path cannot be start with /");
        }
        try {
            if (this.isJar) {
                // JAR 파일(압축 파일) 내에서 루트트디렉터리 검증
                return isValidatedWebappDirectoryInJar(url.getPath(), rootDirPath.toString());
            }
            // 일반 파일 시스템에서 루 디렉터리 검증
            return isValidatedWebappDirectory(rootDirPath);
        } catch (Exception e) {
            log.error("Failed to check if webapp directory is valid", e);
            return false;
        }
    }

    private boolean isValidatedWebappDirectoryInJar(String jarPath, String rootDirPath) throws IOException {
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring(5);
        }

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            boolean hasWebappDir = false;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.equals(rootDirPath + "/")) {
                    hasWebappDir = true;
                }
            }

            return hasWebappDir;
        }
    }
}

