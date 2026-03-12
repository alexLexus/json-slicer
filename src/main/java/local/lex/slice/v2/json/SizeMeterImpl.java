package local.lex.slice.v2.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class SizeMeterImpl implements SizeMeter {

    public static class CountingOutputStream extends OutputStream {
        private final AtomicLong count = new AtomicLong(0);

        @Override
        public void write(int b) throws IOException {
            count.incrementAndGet();
        }

        @Override
        public void write(byte[] b) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            count.addAndGet(b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || off + len > b.length) {
                throw new IndexOutOfBoundsException();
            }
            count.addAndGet(len);
        }

        public long getCount() {
            return count.get();
        }

        public void reset() {
            count.set(0);
        }

        @Override
        public void close() throws IOException {
            // noop
        }

        @Override
        public void flush() throws IOException {
            // noop
        }
    }

    private final ObjectMapper mapper;

    @Override
    public long sizeOf(Object o) throws Exception {
        CountingOutputStream cos = new CountingOutputStream();
        mapper.writeValue(cos, o);

        return cos.getCount();
    }
}
