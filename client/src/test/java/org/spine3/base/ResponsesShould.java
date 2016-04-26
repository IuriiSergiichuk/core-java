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

package org.spine3.base;

import org.junit.Test;
import org.spine3.validate.options.ConstraintViolation;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.spine3.protobuf.Messages.toAny;
import static org.spine3.test.Tests.hasPrivateUtilityConstructor;

@SuppressWarnings("InstanceMethodNamingConvention")
public class ResponsesShould {

    private static final Response RESPONSE_UNSUPPORTED_COMMAND = Response.newBuilder()
            .setError(Error.newBuilder()
                           .setCode(CommandValidationError.UNSUPPORTED_COMMAND.getNumber()))
            .build();

    private static final Response RESPONSE_INVALID_COMMAND = newInvalidCommandResponse();

    @Test
    public void have_private_constructor() {
        assertTrue(hasPrivateUtilityConstructor(Responses.class));
    }

    @Test
    public void return_OK_response() {
        checkNotNull(Responses.ok());
    }

    @Test
    public void recognize_OK_response() {
        assertTrue(Responses.isOk(Responses.ok()));
    }

    @Test
    public void return_false_if_not_OK_response() {
        assertFalse(Responses.isOk(RESPONSE_UNSUPPORTED_COMMAND));
    }

    @Test
    public void recognize_UNSUPPORTED_COMMAND_response() {
        assertTrue(Responses.isUnsupportedCommand(RESPONSE_UNSUPPORTED_COMMAND));
    }

    @Test
    public void return_false_if_not_UNSUPPORTED_COMMAND_response() {
        assertFalse(Responses.isUnsupportedCommand(Responses.ok()));
    }

    @Test
    public void recognize_INVALID_COMMAND_response() {
        assertTrue(Responses.isInvalidCommand(RESPONSE_INVALID_COMMAND));
    }

    @Test
    public void return_false_if_not_INVALID_COMMAND_response() {
        assertFalse(Responses.isInvalidCommand(Responses.ok()));
    }

    private static Response newInvalidCommandResponse() {
        final List<ConstraintViolation> violations = newArrayList(ConstraintViolation.getDefaultInstance());
        final ValidationFailure failureInstance = ValidationFailure.newBuilder()
                                                                   .addAllConstraintViolation(violations)
                                                                   .build();
        final Failure.Builder failure = Failure.newBuilder()
                                               .setInstance(toAny(failureInstance));
        final Response response = Response.newBuilder()
                                          .setFailure(failure)
                                          .build();
        return response;
    }
}