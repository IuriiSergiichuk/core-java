/*
 * Copyright 2015, TeamDev Ltd. All rights reserved.
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

package org.spine3.server.aggregate;

import com.google.protobuf.Message;
import org.spine3.base.EventContext;
import org.spine3.protobuf.MessageField;
import org.spine3.protobuf.Messages;
import org.spine3.server.EntityId;
import org.spine3.server.aggregate.error.MissingAggregateIdException;

import static org.spine3.util.Identifiers.ID_PROPERTY_SUFFIX;

/**
 * Value object for aggregate IDs.
 *
 * @param <I> the type of aggregate IDs
 * @author Alexander Yevsyukov
 */
public final class AggregateId<I> extends EntityId<I> {

    private AggregateId(I value) {
        super(value);
    }

    /**
     * Creates a new non-null id of an aggregate root.
     *
     * @param value id value
     * @return new instance
     */
    public static <I> AggregateId<I> of(I value) {
        return new AggregateId<>(value);
    }

    @SuppressWarnings("TypeMayBeWeakened") // We want already built instances at this level of API.
    public static AggregateId<? extends Message> of(EventContext value) {
        final Message message = Messages.fromAny(value.getAggregateId());
        final AggregateId<Message> result = new AggregateId<>(message);
        return result;
    }

    /**
     * Obtains an aggregate id from the passed command instance.
     *
     * <p>The id value must be the first field of the proto message. Its name must end with "id".
     *
     * @param command the command to get id from
     * @return value of the id
     */
    public static AggregateId getAggregateId(Message command) {
        final Object value = FIELD.getValue(command);
        return of(value);
    }

    /**
     * Accessor object for aggregate ID fields in commands.
     *
     * <p>An aggregate ID must be the first field declared in a message and its
     * name must end with {@code "id"} suffix.
     */
    private static final MessageField FIELD = new MessageField(0) {

        @Override
        protected RuntimeException createUnavailableFieldException(Message message, String fieldName) {
            return new MissingAggregateIdException(message.getClass().getName(), fieldName);
        }

        @Override
        protected boolean isFieldAvailable(Message message) {
            final String fieldName = MessageField.getFieldName(message, getIndex());
            final boolean result = fieldName.endsWith(ID_PROPERTY_SUFFIX);
            return result;
        }
    };
}
