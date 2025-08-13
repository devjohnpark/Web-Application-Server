package org.dochi.http.request.multipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.util.HttpParser.parseContentDisposition;

public class MultipartParameters {
    private static final Logger log = LoggerFactory.getLogger(MultiPartProcessor.class);
    private static final String NAME_PARAMETER_KEY = "name";
    private static final String FILENAME_PARAMETER_KEY = "filename";
    private final Map<String, String> parameters = new HashMap<String, String>();


    public void addContentDispositionParameters(String contentDispositionHeaderValue) {
        // Content-Disposition 헤더 이름 일치하는지 확인
        if (contentDispositionHeaderValue == null || contentDispositionHeaderValue.equals("Content-Disposition")) { return; }
        parameters.putAll(parseContentDisposition(contentDispositionHeaderValue));
    }

//    public void addParameter(String contentDispositionHeaderValue) {
//        // Content-Disposition 헤더 이름 일치하는지 확인
//        if (contentDispositionHeaderValue == null || contentDispositionHeaderValue.isEmpty()) { return; }
//        parameters.putAll(parseContentDisposition(contentDispositionHeaderValue));
//    }

    public String getParameter(String name) {
        if (name.equals(NAME_PARAMETER_KEY) || name.equals(FILENAME_PARAMETER_KEY)) {
            // Content-Disposition 헤더의 파라매터만(name, filename)
            return getContentDispositionParameters(name);
        }
        return parameters.get(name);
    }

    private String getContentDispositionParameters(String name) {
        String value = parameters.get(name);
        if (value != null) {
            return value.replace("\"", "");
        }
        return null;
    }

//    // Content-Disposition 헤더의 파라매터만(name, filename)
//    public String getParameter(String key) {
//        String value = parameters.get(key);
//        if (value != null) {
//            return value.replace("\"", "");
//        }
//        return null;
//    }

    public String getNameParamValue() { return getParameter(NAME_PARAMETER_KEY); }

    public String getFileNameParamValue() { return getParameter(FILENAME_PARAMETER_KEY); }

    public void recycle() {
        if (!parameters.isEmpty()) {
            parameters.clear();
        }
    }
}
