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

package org.spine3.server.aggregate;

import com.google.common.base.Predicate;
import com.google.protobuf.Message;
import org.spine3.Internal;
import org.spine3.server.internal.CommandHandlerMethod;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The wrapper for a command handler method of an Aggregate.
 *
 * @author Alexander Litus
 */
@Internal
class AggregateCommandHandler extends CommandHandlerMethod {

    static final Predicate<Method> IS_AGGREGATE_COMMAND_HANDLER = new Predicate<Method>() {
        @Override
        public boolean apply(@Nullable Method method) {
            checkNotNull(method);
            return isAggregateCommandHandler(method);
        }
    };

    /**
     * Creates a new instance to wrap {@code method} on {@code target}.
     *
     * @param target object to which the method applies
     * @param method subscriber method
     */
    AggregateCommandHandler(Object target, Method method) {
        super(target, method);
    }

    /**
     * Checks if a method is a command handler of an aggregate root.
     *
     * @param method a method to check
     * @return {@code true} if the method is a command handler, {@code false} otherwise
     */
    static boolean isAggregateCommandHandler(Method method) {
        if (!isAnnotatedCorrectly(method)){
            return false;
        }
        if (!acceptsCorrectParams(method)) {
            return false;
        }
        final boolean returnsMessageOrList = returnsMessageOrList(method);
        return returnsMessageOrList;
    }

    private static boolean returnsMessageOrList(Method method) {
        final Class<?> returnType = method.getReturnType();

        if (Message.class.isAssignableFrom(returnType)) {
            return true;
        }
        //noinspection RedundantIfStatement
        if (List.class.isAssignableFrom(returnType)) {
            return true;
        }
        return false;
    }
}