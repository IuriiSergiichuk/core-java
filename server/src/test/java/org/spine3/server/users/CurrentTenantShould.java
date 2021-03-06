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

package org.spine3.server.users;

import org.junit.Test;
import org.spine3.test.Tests;
import org.spine3.users.TenantId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.spine3.test.Tests.hasPrivateParameterlessCtor;

@SuppressWarnings("InstanceMethodNamingConvention")
public class CurrentTenantShould {

    @Test
    public void have_private_constructor() {
        assertTrue(hasPrivateParameterlessCtor(CurrentTenant.class));
    }

    @Test(expected = NullPointerException.class)
    public void reject_null_value() {
        CurrentTenant.set(Tests.<TenantId>nullRef());
    }

    @Test(expected = IllegalArgumentException.class)
    public void reject_default_value() {
        CurrentTenant.set(TenantId.getDefaultInstance());
    }

    @Test
    public void keep_set_value() {
        final TenantId expected = TenantId.newBuilder().setValue(getClass().getSimpleName()).build();

        CurrentTenant.set(expected);
        assertEquals(expected, CurrentTenant.get());
    }

    @Test
    public void clear_set_value() {
        final TenantId value = TenantId.newBuilder().setValue(getClass().getSimpleName()).build();
        CurrentTenant.set(value);

        CurrentTenant.clear();

        assertNull(CurrentTenant.get());
    }
}
