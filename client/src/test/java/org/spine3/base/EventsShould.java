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

import com.google.common.base.Optional;
import com.google.protobuf.BoolValue;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Message;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import org.junit.Test;
import org.spine3.testdata.TestCommandContextFactory;
import org.spine3.type.TypeName;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.protobuf.util.TimeUtil.getCurrentTime;
import static org.junit.Assert.*;
import static org.spine3.base.Events.*;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.protobuf.Messages.fromAny;
import static org.spine3.protobuf.Messages.toAny;
import static org.spine3.protobuf.Timestamps.minutesAgo;
import static org.spine3.protobuf.Timestamps.secondsAgo;
import static org.spine3.protobuf.Values.*;
import static org.spine3.test.Tests.hasPrivateUtilityConstructor;
import static org.spine3.validate.Validate.isNotDefault;

@SuppressWarnings("InstanceMethodNamingConvention")
public class EventsShould {

    private final EventContext context = newEventContext();

    private final StringValue stringValue = newStringValue(newUuid());
    private final BoolValue boolValue = newBoolValue(true);
    @SuppressWarnings("MagicNumber")
    private final DoubleValue doubleValue = newDoubleValue(10.1);

    @Test
    public void have_private_ctor() {
        assertTrue(hasPrivateUtilityConstructor(Events.class));
    }

    @Test
    public void generate_event_id() {
        final EventId result = generateId();

        assertFalse(result.getUuid()
                          .isEmpty());
    }

    @Test
    public void return_null_from_null_input_in_IsAfter_predicate() {
        assertFalse(new IsAfter(secondsAgo(5)).apply(null));
    }

    @Test
    public void return_null_from_null_input_in_IsBefore_predicate() {
        assertFalse(new IsBefore(secondsAgo(5)).apply(null));
    }

    @Test
    public void return_null_from_null_input_in_IsBetween_predicate() {
        assertFalse(new IsBetween(secondsAgo(5), secondsAgo(1)).apply(null));
    }

    @Test
    public void return_actor_from_EventContext() {
        assertEquals(context.getCommandContext()
                            .getActor(), getActor(context));
    }

    @Test
    public void sort_events_by_time() {
        final Event event1 = createEvent(stringValue, newEventContext(minutesAgo(30)));
        final Event event2 = createEvent(boolValue, newEventContext(minutesAgo(20)));
        final Event event3 = createEvent(doubleValue, newEventContext(secondsAgo(10)));
        final List<Event> sortedEvents = newArrayList(event1, event2, event3);
        final List<Event> eventsToSort = newArrayList(event2, event1, event3);

        sort(eventsToSort);

        assertEquals(sortedEvents, eventsToSort);
    }

    @Test
    public void create_event() {
        createEventTest(stringValue);
        createEventTest(boolValue);
        createEventTest(doubleValue);
    }

    private void createEventTest(Message msg) {
        final Event event = createEvent(msg, context);

        assertEquals(msg, fromAny(event.getMessage()));
        assertEquals(context, event.getContext());
    }

    @Test
    public void create_import_event() {
        final Event event = Events.createImportEvent(stringValue, doubleValue);

        assertEquals(stringValue, fromAny(event.getMessage()));
        assertEquals(doubleValue, fromAny(event.getContext()
                                               .getProducerId()));
    }

    @Test
    public void create_import_event_context() {
        final EventContext context = Events.createImportEventContext(doubleValue);

        assertEquals(doubleValue, fromAny(context.getProducerId()));
        assertTrue(isNotDefault(context.getEventId()));
        assertTrue(isNotDefault(context.getTimestamp()));
    }

    @Test
    public void get_message_from_event() {
        createEventAndAssertReturnedMessageFor(stringValue);
        createEventAndAssertReturnedMessageFor(boolValue);
        createEventAndAssertReturnedMessageFor(doubleValue);
    }

    private void createEventAndAssertReturnedMessageFor(Message msg) {
        final Event event = createEvent(msg, context);

        assertEquals(msg, getMessage(event));
    }

    @Test
    public void get_timestamp_from_event() {
        final Event event = createEvent(stringValue, context);

        assertEquals(context.getTimestamp(), getTimestamp(event));
    }

    @Test
    public void get_producer_from_event_context() {
        final StringValue msg = fromAny(context.getProducerId());

        final String id = getProducer(context);

        assertEquals(msg.getValue(), id);
    }

    @Test
    public void return_true_if_event_enrichment_is_enabled() {
        final Event event = createEvent(stringValue, context);

        assertTrue(isEnrichmentEnabled(event));
    }

    @Test
    public void return_false_if_event_enrichment_is_disabled() {
        final EventContext withDisabledEnrichment = context.toBuilder()
                                                           .setDoNotEnrich(true)
                                                           .build();
        final Event event = createEvent(stringValue, withDisabledEnrichment);

        assertFalse(isEnrichmentEnabled(event));
    }

    @Test
    public void return_all_event_enrichments() {
        final EventContext context = newEventContextWithEnrichment(TypeName.of(stringValue).value(), stringValue);

        final Optional<Enrichments> enrichments = Events.getEnrichments(context);

        assertTrue(enrichments.isPresent());
        assertEquals(context.getEnrichments(), enrichments.get());
    }

    @Test
    public void return_optional_absent_if_no_event_enrichments() {
        assertFalse(Events.getEnrichments(context)
                          .isPresent());
    }

    @Test
    public void return_specific_event_enrichment() {
        final EventContext context = newEventContextWithEnrichment(TypeName.of(stringValue).value(), stringValue);

        final Optional<? extends StringValue> enrichment = Events.getEnrichment(stringValue.getClass(), context);

        assertTrue(enrichment.isPresent());
        assertEquals(stringValue, enrichment.get());
    }

    private static EventContext newEventContextWithEnrichment(String key, Message enrichment) {
        final Enrichments.Builder enrichments = Enrichments.newBuilder();
        enrichments.getMutableMap()
                   .put(key, toAny(enrichment));
        final EventContext context = newEventContext().toBuilder()
                                                      .setEnrichments(enrichments.build())
                                                      .build();
        return context;
    }

    private static EventContext newEventContext() {
        return newEventContext(getCurrentTime());
    }

    private static EventContext newEventContext(Timestamp time) {
        final EventId eventId = generateId();
        final StringValue producerId = newStringValue(newUuid());
        final CommandContext cmdContext = TestCommandContextFactory.createCommandContext();
        final EventContext.Builder builder = EventContext.newBuilder()
                                                         .setEventId(eventId)
                                                         .setProducerId(toAny(producerId))
                                                         .setTimestamp(time)
                                                         .setCommandContext(cmdContext);
        return builder.build();
    }
}
