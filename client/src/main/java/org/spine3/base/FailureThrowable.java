/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package org.spine3.base;

import com.google.common.base.Throwables;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Timestamp;
import org.spine3.protobuf.AnyPacker;

import static org.spine3.protobuf.Timestamps.getCurrentTime;

/**
 * Abstract base for throwable business failures.
 *
 * @author Alexander Yevsyukov
 */
public abstract class FailureThrowable extends Throwable {

    private static final long serialVersionUID = 0L;
    // We accept GeneratedMessage (instead of Message) because generated messages implement Serializable.
    private final GeneratedMessageV3 failure;
    private final Timestamp timestamp;

    protected FailureThrowable(GeneratedMessageV3 failure) {
        super();
        this.failure = failure;
        this.timestamp = getCurrentTime();
    }

    public GeneratedMessageV3 getFailure() {
        return failure;
    }

    /** Returns timestamp of the instance creation. */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /** Converts this instance into {@link Failure} message. */
    public Failure toMessage() {
        final Failure.Builder builder = Failure.newBuilder()
                                               .setInstance(AnyPacker.pack(this.failure))
                                               .setStacktrace(Throwables.getStackTraceAsString(this))
                                               .setTimestamp(this.timestamp);
        return builder.build();
    }
}
