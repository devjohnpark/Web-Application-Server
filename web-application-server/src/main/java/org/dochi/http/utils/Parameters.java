package org.dochi.http.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.dochi.http.utils.HttpParser.parseQueryString;

// 헤더의 key와 다르게 파라매터의 key는 요청마다 다르다.
// 그리고 헤더와 다르게 파라메터는 API의 중요 데이터이기 때문에 대부분 값을 파싱해서 사용된다.
// 따라서 파라메터는 HashMap으로 저장해서 사용하도록한다.
public class Parameters {

    private final Map<String, String> parameters = new HashMap<String, String>();

    public void addParameter(String key, String value) {
        if (key == null || value == null || key.isEmpty()) { return; }
        parameters.put(key, value);
    }

    public void addRequestParameters(String queryString) {
        String decodedQueryString = urlDecoding(queryString);
        if (decodedQueryString == null || decodedQueryString.isEmpty()) { return; }
        parameters.putAll(parseQueryString(decodedQueryString));
    }

    private String urlDecoding(String queryString) {
        return queryString != null ? URLDecoder.decode(queryString, StandardCharsets.UTF_8) : null;
    }

    public String getValue(String key) { return parameters.get(key); }

    public void recycle() {
        if (parameters.isEmpty()) {
            return;
        }
        parameters.clear();
    }
}
