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

package org.spine3.server.reflect;

import org.junit.Test;
import org.spine3.base.CommandContext;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.command.Assign;
import org.spine3.test.reflect.Project;
import org.spine3.test.reflect.command.CreateProject;
import org.spine3.test.reflect.event.ProjectCreated;

import static org.junit.Assert.assertFalse;

@SuppressWarnings("InstanceMethodNamingConvention")
public class MethodMapShould {

    /** Test aggregate in which methods are scanned. */
    private static final class TestAggregate extends Aggregate<Long, Project, Project.Builder> {

        public TestAggregate(Long id) {
            super(id);
        }

        @Assign
        ProjectCreated handle(CreateProject command, CommandContext context) {
            return ProjectCreated.getDefaultInstance();
        }
    }

    @Test
    public void expose_key_set() {
        final MethodMap methodMap = MethodMap.create(TestAggregate.class, CommandHandlerMethod.factory());
        assertFalse(methodMap.keySet().isEmpty());
    }
}
