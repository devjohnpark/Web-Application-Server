package org.dochi.http.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ResourceType -> MediaType
// MediaType: text/html; charset=utf8는 하나의 미디어 타입(media type)
// text/html은 그 타입(type/subtype)
// charset=utf8은 파라미터(parameter)

// class로 설계해서 ContentType을 파싱해서 type/subType/charset으로 분리
// request, response에서 둘다 쓸수 있음
public class MediaType {
    private static final Logger log = LoggerFactory.getLogger(MediaType.class);
    private final String type;
    private final String subtype;
    private final String parameterName;
    private final String parameterValue;

    public MediaType(String type, String subtype, String parameterName, String parameterValue) {
        this.type = type;
        this.subtype = subtype;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subtype;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public String getFullType() {
        if (type != null && subtype != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(type).append("/").append(subtype);
            return sb.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append("/").append(subtype);
        if (parameterName != null && parameterValue != null) {
            sb.append("; ").append(parameterName).append("=").append(parameterValue);
        }
        return sb.toString();
    }

    public String getCharset() {
        if (parameterName != null && parameterValue != null && parameterName.equals("charset") && !parameterValue.isEmpty()) {
            return parameterValue;
        }
        return null;
    }

    // 파싱 로직
    // text/html; charset=utf8
    // type, subtype 필수, parameter 선택
    public static MediaType parseMediaType(String mediaType) {
        if (mediaType == null || mediaType.isBlank()) {
            return new MediaType(null, null, null, null);
        }
        String[] parts = mediaType.split(";", 2); // [type/subtype, parameter (optional)]
        String typeSubtypePart = parts[0].trim();

        String[] typeSubtype = typeSubtypePart.split("/");
        if (typeSubtype.length != 2) {
            throw new IllegalStateException("Invalid media type format: missing type or subtype");
        }

        String type = typeSubtype[0].trim();
        String subtype = typeSubtype[1].trim();

        String parameterName = null;
        String parameterValue = null;

        if (parts.length == 2) {
            String parameterPart = parts[1].trim();
            String[] param = parameterPart.split("=", 2);
            if (param.length != 2) {
                throw new IllegalStateException("Invalid parameter format in media type:" + mediaType);
            }
            parameterName = param[0].trim().toLowerCase();
            parameterValue = param[1].trim();
        }

        return new MediaType(type, subtype, parameterName, parameterValue);
    }
}