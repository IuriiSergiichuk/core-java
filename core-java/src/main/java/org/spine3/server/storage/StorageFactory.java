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

package org.spine3.server.storage;

import com.google.protobuf.Message;
import org.spine3.server.Entity;
import org.spine3.server.aggregate.Aggregate;

/**
 * A factory for creating storages used by repositories, {@link org.spine3.server.CommandStore}, and
 * {@link org.spine3.server.EventStore}.
 *
 * @author Alexander Yevsyukov
 */
public interface StorageFactory {

    /**
     * Creates a new {@link CommandStorage} instance.
     */
    CommandStorage createCommandStorage();

    /**
     * Creates a new {@link EventStorage} instance.
     */
    EventStorage createEventStorage();

    /**
     * Creates a new {@link AggregateStorage} instance.
     */
    <I> AggregateStorage<I> createAggregateStorage(Class<? extends Aggregate<I, ?>> aggregateClass);

    /**
     * Creates a new {@link EntityStorage} instance.
     */
    <I, M extends Message> EntityStorage<I, M> createEntityStorage(Class<? extends Entity<I, M>> entityClass);

    /**
     * Sets up storages if needed.
     */
    void setUp();

    /**
     * Perform operations required after storages usage (e.g. release resources, clear file system etc) if needed.
     */
    void tearDown();
}