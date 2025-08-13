package org.dochi.webresource;

import java.io.IOException;
import java.nio.file.Path;

public interface ResourceProvider {
    Resource getResource(String resourcePath);
    SplitFileResource getSplitResource(String resourcePath);
    void close();
}
