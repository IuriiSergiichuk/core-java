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

package org.spine3.server.event;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.protobuf.Timestamp;
import org.spine3.base.Event;
import org.spine3.base.Events;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The predicate for filtering {@code Event} instances by
 * {@link EventStreamQuery}.
 *
 * @author Alexander Yevsyukov
 * @author Dmytro Dashenkov
 */
public class MatchesStreamQuery implements Predicate<Event> {

    private final EventStreamQuery query;
    private final Predicate<Event> timePredicate;

    @SuppressWarnings("IfMayBeConditional")
    public MatchesStreamQuery(EventStreamQuery query) {
        this.query = query;
        final Timestamp after = query.getAfter();
        final Timestamp before = query.getBefore();
        final boolean afterSpecified = query.hasAfter();
        final boolean beforeSpecified = query.hasBefore();
        //noinspection IfStatementWithTooManyBranches
        if (afterSpecified && !beforeSpecified) {
            this.timePredicate = new Events.IsAfter(after);
        } else if (!afterSpecified && beforeSpecified) {
            this.timePredicate = new Events.IsBefore(before);
        } else if (afterSpecified /* && beforeSpecified is true here too */) {
            this.timePredicate = new Events.IsBetween(after, before);
        } else { // No timestamps specified.
            this.timePredicate = Predicates.alwaysTrue();
        }
    }

    @Override
    public boolean apply(@Nullable Event input) {
        if (!timePredicate.apply(input)) {
            return false;
        }
        final List<EventFilter> filterList = query.getFilterList();
        if (filterList.isEmpty()) {
            return true; // The time range matches, and no filters specified.
        }
        // Check if one of the filters matches. If so, the event matches.
        for (EventFilter filter : filterList) {
            final Predicate<Event> filterPredicate = new MatchFilter(filter);
            if (filterPredicate.apply(input)) {
                return true;
            }
        }
        return false;
    }
}
