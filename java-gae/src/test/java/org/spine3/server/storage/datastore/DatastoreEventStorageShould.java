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

package org.spine3.server.storage.datastore;

import org.junit.After;
import org.spine3.TypeName;
import org.spine3.server.storage.EventStorageShould;
import org.spine3.server.storage.EventStoreRecord;

@SuppressWarnings({"InstanceMethodNamingConvention", "MethodMayBeStatic"})
public class DatastoreEventStorageShould extends EventStorageShould {

    /* TODO:2015.09.30:alexander.litus: start Local Datastore Server automatically and not ignore tests.
     * Reported an issue here:
     * https://code.google.com/p/google-cloud-platform/issues/detail?id=10&thanks=10&ts=1443682670
     */

    private static final TypeName TYPE_NAME = TypeName.of(EventStoreRecord.getDescriptor());
    private static final LocalDatastoreManager<EventStoreRecord> DATASTORE_MANAGER = LocalDatastoreManager.newInstance(TYPE_NAME);
    private static final DatastoreEventStorage STORAGE = DatastoreEventStorage.newInstance(DATASTORE_MANAGER);

    public DatastoreEventStorageShould() {
        super(STORAGE);
    }

    @After
    public void tearDownTest() {
        DATASTORE_MANAGER.clear();
    }
}
