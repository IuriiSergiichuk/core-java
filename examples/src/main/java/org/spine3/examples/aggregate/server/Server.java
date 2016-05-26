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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.server.BoundedContext;
import org.spine3.server.ClientService;
import org.spine3.server.event.EventSubscriber;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.memory.InMemoryStorageFactory;

import java.io.IOException;

import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * Sample gRPC server implementation.
 *
 * @author Mikhail Melnik
 * @author Alexander Litus
 */
public class Server {

    private final ClientService clientService;
    private final BoundedContext boundedContext;

    public Server(StorageFactory storageFactory) {
        // Create a bounded context.
        this.boundedContext = BoundedContext.newBuilder()
                                                            .setStorageFactory(storageFactory)
                                                            .build();
        // Create and register a repository with the bounded context.
        final OrderRepository repository = new OrderRepository(boundedContext);
        boundedContext.register(repository);

        // Subscribe an event subscriber in the bounded context.
        final EventSubscriber eventLogger = new EventLogger();
        boundedContext.getEventBus().subscribe(eventLogger);

        // Create a client service with this bounded context.
        this.clientService = ClientService.newBuilder()
                                          .addBoundedContext(boundedContext)
                                          .setPort(DEFAULT_CLIENT_SERVICE_PORT)
                                          .build();
    }

    public void start() throws IOException {
        clientService.start();
    }

    public void awaitTermination() throws InterruptedException {
        clientService.awaitTermination();
    }

    public void shutdown() throws Exception {
        clientService.shutdown();
        boundedContext.close();
    }

    /**
     * The entry point of the server application.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final Server server = new Server(InMemoryStorageFactory.getInstance());
        server.start();
        log().info("Server started, listening to commands on the port " + DEFAULT_CLIENT_SERVICE_PORT);
        server.awaitTermination();
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(Server.class);
    }
}
