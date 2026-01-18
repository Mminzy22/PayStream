package com.paystream.payment.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachedBodyHttpServletRequestTest {

    @Mock private HttpServletRequest request;
    @Mock private ServletInputStream servletInputStream;

    private static final String TEST_BODY = "{\"test\":\"data\",\"number\":123}";
    private static final byte[] TEST_BODY_BYTES = TEST_BODY.getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(TEST_BODY_BYTES);
        when(request.getInputStream()).thenReturn(servletInputStream);
        when(servletInputStream.read(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(
                        invocation -> {
                            byte[] buffer = invocation.getArgument(0);
                            int offset = invocation.getArgument(1);
                            int length = invocation.getArgument(2);
                            return byteArrayInputStream.read(buffer, offset, length);
                        });
        when(servletInputStream.read()).thenAnswer(invocation -> byteArrayInputStream.read());
    }

    @Test
    void constructor_shouldCacheBody() throws IOException {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BODY_BYTES);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));

        // when
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        // then
        assertNotNull(cachedRequest);
        assertEquals(TEST_BODY, cachedRequest.getCachedBody());
        assertArrayEquals(TEST_BODY_BYTES, cachedRequest.getCachedBodyAsBytes());
    }

    @Test
    void getInputStream_shouldReturnNewStreamEachTime() throws IOException {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BODY_BYTES);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        // when
        ServletInputStream stream1 = cachedRequest.getInputStream();
        ServletInputStream stream2 = cachedRequest.getInputStream();

        // then
        assertNotNull(stream1);
        assertNotNull(stream2);
        assertNotSame(stream1, stream2);
    }

    @Test
    void getReader_shouldReturnBufferedReader() throws IOException {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BODY_BYTES);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        // when
        BufferedReader reader = cachedRequest.getReader();

        // then
        assertNotNull(reader);
        String line = reader.readLine();
        assertTrue(line.contains("test"));
    }

    @Test
    void getCachedBody_shouldReturnString() throws IOException {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BODY_BYTES);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        // when
        String body = cachedRequest.getCachedBody();

        // then
        assertEquals(TEST_BODY, body);
    }

    @Test
    void getCachedBodyAsBytes_shouldReturnBytes() throws IOException {
        // given
        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BODY_BYTES);
        when(request.getInputStream()).thenReturn(new MockServletInputStream(inputStream));
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);

        // when
        byte[] bytes = cachedRequest.getCachedBodyAsBytes();

        // then
        assertArrayEquals(TEST_BODY_BYTES, bytes);
    }

    // Mock ServletInputStream implementation
    private static class MockServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream inputStream;

        public MockServletInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener listener) {
            throw new UnsupportedOperationException();
        }
    }
}
