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
package org.spine3.server.util;

import com.google.common.base.Predicate;
import com.google.protobuf.Timestamp;
import org.spine3.client.CommandRequest;
import org.spine3.protobuf.Timestamps;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for working with {@link CommandRequest}s.
 *
 * @author Mikhail Melnik
 * @author Alexander Yevsyukov
 */
public class CommandRequests {

    public static Predicate<CommandRequest> wereAfter(final Timestamp from) {
        return new Predicate<CommandRequest>() {
            @Override
            public boolean apply(@Nullable CommandRequest request) {
                checkNotNull(request);
                final Timestamp timestamp = getTimestamp(request);
                return Timestamps.isAfter(timestamp, from);
            }
        };
    }

    public static Predicate<CommandRequest> wereWithinPeriod(final Timestamp from, final Timestamp to) {
        return new Predicate<CommandRequest>() {
            @Override
            public boolean apply(@Nullable CommandRequest request) {
                checkNotNull(request);
                final Timestamp timestamp = getTimestamp(request);
                return Timestamps.isBetween(timestamp, from, to);
            }
        };
    }

    private static Timestamp getTimestamp(CommandRequest request) {
        final Timestamp result = request.getContext().getTimestamp();
        return result;
    }

    /**
     * Sorts the command given command request list by command timestamp value.
     *
     * @param commandRequests the command request list to sort
     */
    public static void sort(List<CommandRequest> commandRequests) {
        Collections.sort(commandRequests, new Comparator<CommandRequest>() {
            @Override
            public int compare(CommandRequest o1, CommandRequest o2) {
                final Timestamp timestamp1 = getTimestamp(o1);
                final Timestamp timestamp2 = getTimestamp(o2);
                return Timestamps.compare(timestamp1, timestamp2);
            }
        });
    }

    private CommandRequests() {}
}