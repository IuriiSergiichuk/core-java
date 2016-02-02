/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Throwables.propagate;

/**
 * Utility class working with I/O: streams etc.
 *
 * @author Alexander Litus
 */
public class IoUtil {

    private static final String ERROR_MESSAGE = "Exception while closing:";

    private IoUtil() {}

    /**
     * Closes passed closeables one by one.
     *
     * <p>Logs each {@link IOException} if it occurs.
     */
    @SuppressWarnings("ConstantConditions"/*check for null is ok*/)
    public static void close(Closeable... closeables) {
        try {
            for (Closeable c : closeables) {
                if (c != null) {
                    c.close();
                }
            }
        } catch (IOException e) {
            if (log().isErrorEnabled()) {
                log().error(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Closes passed closeables one by one.
     *
     * <p>Logs each {@link IOException} if it occurs.
     */
    @SuppressWarnings("ConstantConditions"/*check for null is ok*/)
    public static <T extends Closeable> void close(Iterable<T> closeables) {
        try {
            for (T c : closeables) {
                if (c != null) {
                    c.close();
                }
            }
        } catch (IOException e) {
            if (log().isErrorEnabled()) {
                log().error(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Closes passed closeables one by one.
     *
     * <p>Logs each {@link Exception} if it occurs.
     */
    @SuppressWarnings("ConstantConditions"/*check for null is ok*/)
    public static void closeAll(AutoCloseable... closeables) {
        try {
            for (AutoCloseable c : closeables) {
                if (c != null) {
                    c.close();
                }
            }
        } catch (Exception e) {
            if (log().isErrorEnabled()) {
                log().error(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Closes passed auto-closeables one by one.
     *
     * <p>Logs each {@link Exception} if it occurs.
     */
    @SuppressWarnings("ConstantConditions"/*check for null is ok*/)
    public static <T extends AutoCloseable> void closeAll(Iterable<T> closeables) {
        try {
            for (T c : closeables) {
                if (c != null) {
                    c.close();
                }
            }
        } catch (Exception e) {
            if (log().isErrorEnabled()) {
                log().error(ERROR_MESSAGE, e);
            }
        }
    }

    /**
     * Flushes passed streams one by one.
     *
     * <p>Logs each {@link IOException} if it occurs.
     */
    public static void flushAll(@Nullable Flushable... flushables) {
        try {
            flush(flushables);
        } catch (IOException e) {
            if (log().isErrorEnabled()) {
                log().error("Exception while flushing stream", e);
            }
        }
    }

    /**
     * Flushes streams in turn.
     *
     * @throws RuntimeException if {@link IOException} occurs
     */
    public static void tryToFlush(@Nullable Flushable... flushables) {
        try {
            flush(flushables);
        } catch (IOException e) {
            propagate(e);
        }
    }

    @SuppressWarnings("ConstantConditions"/*check for null is ok*/)
    private static void flush(@Nullable Flushable[] flushables) throws IOException {
        if (flushables == null) {
            return;
        }
        for (Flushable f : flushables) {
            if (f != null) {
                f.flush();
            }
        }
    }

    /**
     * Flushes and closes output streams in turn silently.
     *
     * <p>Logs each {@link IOException} if it occurs.
     */
    public static void flushAndCloseAll(@Nullable OutputStream... streams) {
        if (streams == null) {
            return;
        }
        flushAll(streams);
        close(streams);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(IoUtil.class);
    }
}
