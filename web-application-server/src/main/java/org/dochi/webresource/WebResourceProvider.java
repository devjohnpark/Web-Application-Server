package org.dochi.webresource;

import org.dochi.webserver.lifecycle.Lifecycle;
import org.dochi.webserver.lifecycle.LifecycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class WebResourceProvider implements ResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(WebResourceProvider.class);
    private static final String DEFAULT_PAGE = "index.html";
    private final Path rootDirPath;
    private final FileSystem jarFileSystem;
    private final boolean isJar;

    // 1. AbstractApiHandler is singleton class
    // 2. Worker threads can access AbstractApiHandler's child object
    // 3. Worker threads can access WebResourceProvider's isClosed instance variable
    // 4. So, it needs prevention synchronization issue
    private final AtomicBoolean isJarFileSystemClosed = new AtomicBoolean(false);

    public WebResourceProvider(Path rootDirPath) {
        /*
        WebResourceProvider.class: Get metadata of WebResourceProvider class
        getProtectionDomain(): Get class's protection domain info
        getCodeSource(): Get source info of class load
        getLocation(): soruce location convert to url
         */

        URL url = getClassLocation();
        this.isJar = isJar(url);
        this.jarFileSystem = createJarFileSystem(url);
        this.rootDirPath = validWebRootDirectory(rootDirPath, url);
        log.debug("WebResourceProvider is created");
    }

    @Override
    public Resource getResource(String resourcePath) {
        // absolute path (start with '/'): webapp.resolve(/index.html) -> /index.html
        // relative path (start with '/'): webapp.resolve(index.html) -> webapp/index.html
        return getResourceInternal(rootDirPath.resolve(normalizeFilePath(validateResourcePath(resourcePath))));
    }

    @Override
    public SplitFileResource getSplitResource(String resourcePath) {
        return doGetSplitResource(getResourceFileSystemPath(resourcePath));
    }

    @Override
    public void close() {
        if (this.jarFileSystem != null && this.isJarFileSystemClosed.compareAndSet(false, true)) {
            try {
                this.jarFileSystem.close();
                log.debug("WebResourceProvider is Closed");
            } catch (IOException e) {
                log.error("Failed to close jar file system", e);
            }
        }
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
        return getClass().getProtectionDomain().getCodeSource().getLocation();
    }

    private boolean isJar(URL url) {
        return url.getPath().endsWith(".jar");
    }

    private FileSystem createJarFileSystem(URL url) {
        if (url.getPath().endsWith(".jar")) {
            try {
                // JAR 파일의 위치를 URI로 반환: jar:file:/path/to/build/libs/app.jar!/ -> JAR 파일을 열고 내부 리소스에 접근할 수 있는 FileSystem의 진입점
                // URL.toURI(): URL을 URI로 변환
                // "jar:" URI 스키마(Scheme)로, JAR 파일을 나타내서 FileSystems.newFileSystem()은 jar: 스키마를 인식하여 압축 파일인 JAR 포맷을 마운트해서 파일 시스템이 읽을수 있도록한다. (일반 파일 URI(file:)와 구분하며, JAR 내부 리소스 접근을 명시)
                // "!": JAR 파일과 그 안의 리소스 트리를 분리하는 표식
                // !/ 이후 경로를 JAR 내부 루트로 설정
                // jar:file:/path/to/app.jar!/webapp/index.html → webapp/index.html은 JAR 내부 경로
                return FileSystems.newFileSystem(new URI("jar:" + url.toURI().toString() + "!/"), Collections.emptyMap());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Wrong jar file URI: " + url, e);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create jar file system", e);
            }
        }
        return null;
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

//    private byte[] readResourceInJarUsingFileSystem(String resourcePath) {
//        try  {
//            Path path = getResourceFileSystemPath(resourcePath);
//            if (Files.exists(path)) {
//                // Files API: FileSystem을 통해 JAR 내부를 파일 시스템처럼 읽음 (jar:file:/.../app.jar!/webapp/index.html)
//                return Files.readAllBytes(path);
//            }
//            log.error("Resource not found: {}", resourcePath);
//        } catch (IOException e) {
//            log.error("Failed to read resource path: {}, exception: {}", resourcePath, e.getMessage());
//        }
//        return null;
//    }

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

    private SplitFileResource doGetSplitResource(Path resourcePath) {
        try {
            InputStream in = getInputStream(resourcePath);
            if (in != null) {
                // File system API를 사용해서 파일 사이즈 가져오기
                return new SplitFileResource(in, Files.size(resourcePath), ResourceType.fromFilePath(resourcePath).getMimeType());
            }
        } catch (IOException e) {
            log.error("Failed to find resource, path: {}, exception: {}", resourcePath, e.getMessage());
        }
        return new SplitFileResource();
    }

    private Path getResourceFileSystemPath(String resourcePath) {
        Path path = rootDirPath.resolve(normalizeFilePath(validateResourcePath(resourcePath)));
        if (jarFileSystem != null) {
            // jarFileSystem.getPath("webapp/index.html") → jar:file:/.../app.jar!/webapp/index.html
            return jarFileSystem.getPath(path.toString());
        }
        return path;
    }

    // 각 파일마다 하나의 입력 스트림을 생성하는 것이 효율적이지 안나?
    // 장점: 메모리 자원 적게 씀, 입력 스트림 생성시간 단축
    // 단점: 동시에 여러 클라이언트가 InputStream으로 읽으면 blocking 발생해서 읽기 지연 -> 응답 지연
    // 결론: 성능이 중요한 웹서버는 메모리를 더 쓰러라도 처리 속도가 중요하므로 클라이언트간에 동일한 입력스트림을 공유하지 않는다.
    private InputStream getInputStream(Path resourcePath) throws IOException {
        if (jarFileSystem != null) {
            return WebResourceProvider.class.getClassLoader().getResourceAsStream(resourcePath.toString());
        }
        return Files.exists(resourcePath) ? Files.newInputStream(resourcePath) : null;
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

//    public boolean isValidatedWebRootDirectory(String rootDirPath, URL url) {
//        if (rootDirPath == null) {
//            throw new IllegalArgumentException("Root directory path cannot be null");
//        }
//        try {
//            if (this.isJar) {
//                // JAR 파일(압축 파일) 내에서 webapp 디렉터리 검증
//                return isValidatedWebappDirectoryInJar(url.getPath(), rootDirPath);
//            }
//            // 일반 파일 시스템에서 webapp 디렉터리 검증
//            return isValidatedWebappDirectory(url, rootDirPath);
//        } catch (Exception e) {
//            log.error("Failed to check if webapp directory is valid", e);
//            return false;
//        }
//    }

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


//    private boolean isValidatedWebappDirectory(URL url, String rootDirPath) throws URISyntaxException {
//        // 현재 클래스 위치 기준으로 webapp 디렉터리 확인
//        File baseDir = new File(url.toURI());
//
//        // 클래스 파일인 경우 상위 디렉터리로 이동 (classes 디렉터리)
//        if (!baseDir.isDirectory()) {
//            baseDir = baseDir.getParentFile();
//        }
//
//        // 프로젝트 루트 디렉터리 찾기
//        while (baseDir != null && !baseDir.getName().equals("http-web-server")) {
//            baseDir = baseDir.getParentFile();
//        }
//
//        if (baseDir == null) {
//            return false;
//        }
//
//        // webapp 디렉터리 확인
//        File webappDir = new File(baseDir, rootDirPath);
//        return webappDir.exists() && webappDir.isDirectory();
//    }

}

