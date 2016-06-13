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

import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import org.junit.Test;
import com.google.protobuf.StringValue;
import static org.junit.Assert.*;
import static org.spine3.protobuf.Messages.fromAny;
import static org.spine3.protobuf.Values.newStringValue;
import static org.spine3.test.Tests.hasPrivateUtilityConstructor;
/**
 * @author Andrey Lavrov
 */
public class MismatchShould {

    private static final String REQUESTED = "requested";
    private static final String EXPECTED = "expected";
    private static final String ACTUAL = "ACTUAL";
    private static final int VERSION = 0;
    private static final String DEFAULT_VALUE = "";
    public static final double DELTA = 0.01;

    @Test
    public void has_private_constructor() {
        assertTrue(hasPrivateUtilityConstructor(Mismatch.class));
    }

    @Test
    public void set_default_expected_value_if_it_was_passed_as_null() {
        final ValueMismatch mismatch = Mismatch.of(null, ACTUAL, REQUESTED, VERSION);
        final String expected = mismatch.getExpected()
                                        .toString();

        assertEquals(DEFAULT_VALUE, expected);
    }

    @Test
    public void set_default_actual_value_if_it_was_passed_as_null() {
        final ValueMismatch mismatch = Mismatch.of(EXPECTED, null, REQUESTED, VERSION);
        final String actual = mismatch.getActual()
                                      .toString();

        assertEquals(DEFAULT_VALUE, actual);
    }

    @Test
    public void return_mismatch_object_with_string_values() {
        final ValueMismatch mismatch = Mismatch.of(EXPECTED, ACTUAL, REQUESTED, VERSION);
        final StringValue expected = fromAny(mismatch.getExpected());
        final StringValue actual = fromAny(mismatch.getActual());
        final StringValue requested = fromAny(mismatch.getRequested());

        assertEquals(EXPECTED, expected.getValue());
        assertEquals(ACTUAL, actual.getValue());
        assertEquals(REQUESTED, requested.getValue());
    }

    @Test
    public void return_mismatch_object_with_int32_values() {
        final int value0 = 0;
        final int value1 = 1;
        final int value2 = 2;
        final ValueMismatch mismatch = Mismatch.of(value0, value1, value2, VERSION);
        final Int32Value expected = fromAny(mismatch.getExpected());
        final Int32Value actual = fromAny(mismatch.getActual());
        final Int32Value requested = fromAny(mismatch.getRequested());

        assertEquals(value0, expected.getValue());
        assertEquals(value1, actual.getValue());
        assertEquals(value2, requested.getValue());
    }

    @Test
    public void return_mismatch_object_with_int64_values() {
        final long value0 = 0L;
        final long value1 = 1L;
        final long value2 = 2L;
        final ValueMismatch mismatch = Mismatch.of(value0, value1, value2,  VERSION);
        final Int64Value expected = fromAny(mismatch.getExpected());
        final Int64Value actual = fromAny(mismatch.getActual());
        final Int64Value requested = fromAny(mismatch.getRequested());

        assertEquals(value0, expected.getValue());
        assertEquals(value1, actual.getValue());
        assertEquals(value2, requested.getValue());
    }

    @Test
    public void return_mismatch_object_with_float_values() {
        final float value0 = 0.0F;
        final float value1 = 1.0F;
        final float value2 = 2.0F;
        final ValueMismatch mismatch = Mismatch.of(value0, value1, value2, VERSION);
        final FloatValue expected = fromAny(mismatch.getExpected());
        final FloatValue actual = fromAny(mismatch.getActual());
        final FloatValue requested = fromAny(mismatch.getRequested());

        assertEquals(value0, expected.getValue(), DELTA);
        assertEquals(value1, actual.getValue(), DELTA);
        assertEquals(value2, requested.getValue(), DELTA);
    }

    @Test
    public void return_mismatch_object_with_double_values() {
        final double value0 = 0.1;
        final double value1 = 0.1;
        final double value2 = 0.1;
        final ValueMismatch mismatch = Mismatch.of(value0, value1, value2, VERSION);
        final DoubleValue expected = fromAny(mismatch.getExpected());
        final DoubleValue actual = fromAny(mismatch.getActual());
        final DoubleValue requested = fromAny(mismatch.getRequested());

        assertEquals(value0, expected.getValue(), DELTA);
        assertEquals(value1, actual.getValue(), DELTA);
        assertEquals(value2, requested.getValue(), DELTA);
    }

    @Test
    public void return_mismatch_object_with_boolean_values() {
        final boolean value0 = true;
        final boolean value1 = true;
        final boolean value2 = true;
        final ValueMismatch mismatch = Mismatch.of(value0, value1, value2, VERSION);
        final BoolValue expected = fromAny(mismatch.getExpected());
        final BoolValue actual = fromAny(mismatch.getActual());
        final BoolValue requested = fromAny(mismatch.getRequested());

        assertEquals(value0, expected.getValue());
        assertEquals(value1, actual.getValue());
        assertEquals(value2, requested.getValue());
    }

    @Test
    public void set_default_expected_value_if_it_was_passed_as_null_message_overload() {
        final ValueMismatch mismatch = Mismatch.of(null, newStringValue(ACTUAL), newStringValue(REQUESTED), VERSION);
        final String expected = mismatch.getExpected()
                                        .toString();

        assertEquals(DEFAULT_VALUE, expected);
    }

    @Test
    public void set_default_actual_value_if_it_was_passed_as_null_message_overload() {
        final ValueMismatch mismatch = Mismatch.of(newStringValue(EXPECTED), null, newStringValue(REQUESTED), VERSION);
        final String actual = mismatch.getActual()
                                      .toString();

        assertEquals(DEFAULT_VALUE, actual);
    }

    @Test
    public void return_mismatch_object_with_message_values() {
        final ValueMismatch mismatch = Mismatch.of(newStringValue(EXPECTED), newStringValue(ACTUAL), newStringValue(REQUESTED), VERSION);
        final StringValue expected = fromAny(mismatch.getExpected());
        final StringValue actual = fromAny(mismatch.getActual());
        final StringValue requested = fromAny(mismatch.getRequested());

        assertEquals(EXPECTED, expected.getValue());
        assertEquals(ACTUAL, actual.getValue());
        assertEquals(REQUESTED, requested.getValue());
    }
}
