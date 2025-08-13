//package org.dochi.http.response;
//
//import org.dochi.http.request.data.HttpVersion;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class StatusLineTest {
//    private StatusLine statusLine;
//
//    @BeforeEach
//    void setUp() {
//        statusLine = new StatusLine(HttpVersion.HTTP_1_1, HttpStatus.OK);
//    }
//
//    @Test
//    void setVersion() {
//        HttpVersion newVersion = HttpVersion.HTTP_2_0;
//
//        statusLine.setVersion(newVersion);
//
//        assertEquals(newVersion, statusLine.getVersion());
//    }
//
//    @Test
//    void setStatus() {
//        HttpStatus newStatus = HttpStatus.NOT_FOUND;
//
//        statusLine.setStatus(newStatus);
//
//        assertEquals(newStatus, statusLine.getStatus());
//    }
//
//
//    @Test
//    void setStatus_null() {
//        HttpStatus originalStatus = statusLine.getStatus();
//
//        statusLine.setStatus(null);
//
//        assertEquals(originalStatus, statusLine.getStatus());
//    }
//
//    @Test
//    void setVersion_null() {
//        HttpVersion originalVersion = statusLine.getVersion();
//
//        statusLine.setVersion(null);
//
//        assertEquals(originalVersion, statusLine.getVersion());
//    }
//
//    @Test
//    void statusLineToString() {
//        HttpVersion version = HttpVersion.HTTP_1_1;
//        HttpStatus status = HttpStatus.OK;
//        StatusLine sl = new StatusLine(version, status);
//
//        String statusLineStr = sl.toString();
//
//        assertEquals(String.format("%s %d %s\r\n", version.getVersion(), status.getCode(), status.getMessage()), statusLineStr);
//    }
//}