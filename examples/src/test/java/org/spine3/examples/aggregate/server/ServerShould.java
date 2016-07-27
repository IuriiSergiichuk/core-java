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

package org.spine3.examples.aggregate.server;

import org.junit.Test;
import org.spine3.examples.aggregate.ClientApp;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.io.IOException;

import static com.google.common.base.Throwables.propagate;

@SuppressWarnings("InstanceMethodNamingConvention")
public class ServerShould {

    @Test
    public void run_on_in_memory_storage() throws Exception {
        final Server[] serverRef = new Server[1];

        final Thread serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Server server = new Server(InMemoryStorageFactory.getInstance());
                serverRef[0] = server;
                try {
                    server.start();
                    server.awaitTermination();
                } catch (IOException e) {
                    throw propagate(e);
                }
            }
        });

        serverThread.start();

        //noinspection ZeroLengthArrayAllocation
        ClientApp.main(new String[0]);

        serverRef[0].shutdown();
    }
}
