package org.dochi.http.api;

import org.dochi.http.exception.HttpStatusException;
import org.dochi.http.request.data.RequestHeadersGetter;
import org.dochi.http.request.data.RequestMetadataGetter;
import org.dochi.http.request.data.RequestParametersGetter;
import org.dochi.http.request.multipart.Part;

import java.io.IOException;
import java.io.InputStream;

public interface HttpApiRequest extends RequestMetadataGetter, RequestHeadersGetter, RequestParametersGetter {
    byte[] getAllPayload() throws IOException;
    String getAllPayloadAsString() throws IOException;
    Part getPart(String partName) throws IOException, HttpStatusException;
    InputStream getInputStream() throws IOException;
}
