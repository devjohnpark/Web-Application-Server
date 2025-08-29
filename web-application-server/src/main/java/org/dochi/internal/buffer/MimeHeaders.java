package org.dochi.internal.buffer;

public class MimeHeaders {
    private static final int DEFAULT_HEADER_FIELD_COUNT = 8;
    private int len = 0;
    private int count = 0;

    private MimeHeaderField[] headers;
    
    public MimeHeaders() {
        initHeaders(DEFAULT_HEADER_FIELD_COUNT);
    }

    public MimeHeaders(int length) {
        initHeaders(length);
    }

    private void initHeaders(int len) {
        this.headers = new MimeHeaderField[len];
        // 배열의 각 요소 초기화
        for (int i = 0; i < this.headers.length; i++) {
            this.headers[i] = new MimeHeaderField();
        }
        this.len = len;
    }

    public void recycle() {
        for(int i = 0; i < this.count; ++i) {
            this.headers[i].recycle();
        }
        this.count = 0;
    }

    public MimeHeaderField createHeader() {
        if (this.count >= this.len) {
            int newLength = this.count * 2;
            if (this.len > 0 && newLength > this.len) {
                this.len = newLength;
            }
            MimeHeaderField[] tmp = new MimeHeaderField[len];
            System.arraycopy(this.headers, 0, tmp, 0, count);
            for (int i = count; i < len; i++) {
                tmp[i] = new MimeHeaderField();
            }
            this.headers = tmp;
        }
        return headers[count++];
    }

    public int size() {
        return this.count;
    }

    // 기존: 모든 헤더 필드 String으로 변환 -> HashMap<String, String> 저장 (모든 헤더의 문자 인코딩 비용과 GC 부하 발생) -> String key로 검색 -> O(1)로 데이터 접근
    // 변경: 버퍼에서 byte 단위로만 헤더 필드의 key, value의 범위만 파싱 -> String key로 검색 -> 최대 O(N) -> 찾으면 버퍼를 범위를 참조 저장 -> O(1)로 데이터 접근

    // 주안 사항
    // 헤더 수가 적은 경우(보통 10~30개 수준)는 배열 순회가 해시보다 빠를 수도 있다.
    // 버퍼(byte[])에서 헤더는 연손적으로 메모리상에 저장되기 때문에 캐시히트가 높다. (JIT 최적화 좋음)
    // 반면, 해시 맵은 내부적으로 포인터 배열을 사용하고 해시 충돌이 발생했을 경우 Red-Black Tree로 변환해서 log(n)으로 처리 (해시 연산과 해시 충돌에서 오버헤드 발생)
    // 포인터 배열을 사용하므로 메모리가 연속적으로 저장되지 않아서 캐시 히트가 낮을수 있다. (JIT 최적화 낮음)
    // 수많은 클라이언트의 동시 요청을 처리할때 GC까지 고려해서 성능을 비교해야한다.
    public MessageBytes getValue(String name) {
        for(int i = 0; i < this.count; i++) {
            if (this.headers[i].getName().equalsIgnoreCase(name)) {
                return this.headers[i].getValue();
            }
        }
        return null;
    }

    public String getHeader(String name) {
        MessageBytes byteValue = this.getValue(name);
        return byteValue != null ? byteValue.toString() : null;
    }
}
