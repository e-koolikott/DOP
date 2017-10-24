package ee.hm.dop.utils.io;

import ee.hm.dop.utils.DOPFileUtils;
import ee.hm.dop.utils.exceptions.MaxFileSizeExceededException;

import java.io.IOException;
import java.io.InputStream;

public class LimitedSizeInputStream extends InputStream {

    private InputStream source;
    private long maxSize;
    private long totalRead;

    public LimitedSizeInputStream(long maxSize, InputStream source) {
        this.source = source;
        this.maxSize = maxSize;
        this.totalRead = 0;
    }

    @Override
    public int read() throws IOException {
        if (totalRead >= (maxSize * DOPFileUtils.MB_TO_B_MULTIPLIER)) {
            throw new MaxFileSizeExceededException();
        }

        int read = source.read();

        if (read > -1) {
            totalRead++;
        }

        return read;
    }

    @Override
    public void close() throws IOException {
        source.close();
        super.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        source.reset();
        totalRead = 0;
    }
}
