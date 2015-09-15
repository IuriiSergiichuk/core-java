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
package org.spine3.sample.server;

import com.google.protobuf.Message;

import java.util.List;

/**
 * Test file system based implementation of the {@link Message} repository.
 */
public class FileSystemStorage<M extends Message> extends BaseStorage<M> {

    public FileSystemStorage(Class<M> messageClass) {
        super(messageClass);
    }

    @Override
    protected List<M> read(Class<M> messageClass, Message parentId) {
        final List<M> result = FileSystemHelper.read(messageClass, parentId);
        return result;
    }

    @Override
    protected List<M> readAll(Class<M> messageClass) {
        final List<M> result = FileSystemHelper.readAll(messageClass);
        return result;
    }

    @Override
    protected void save(M message) {
        FileSystemHelper.write(message);
    }
}
