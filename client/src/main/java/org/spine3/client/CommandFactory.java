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

package org.spine3.client;

import com.google.protobuf.Message;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.base.Commands;
import org.spine3.base.UserId;
import org.spine3.time.ZoneOffset;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spine3.validate.Validate.checkNotEmptyOrBlank;

/**
 * The factory to generate new {@link Command} instances.
 *
 * @author Alexander Yevsyukov
 */
public class CommandFactory {

    private final UserId actor;
    private final ZoneOffset zoneOffset;
    private final String boundedContextName;

    protected CommandFactory(UserId actor, ZoneOffset zoneOffset, String boundedContextName) {
        this.actor = checkNotNull(actor);
        this.zoneOffset = checkNotNull(zoneOffset);
        this.boundedContextName = checkNotEmptyOrBlank(boundedContextName, "Bounded context name");
    }

    /**
     * Creates new instance of the factory for the user working at the timezone
     * of the passed offset.
     *
     * @param actor the ID of the user generating commands
     * @param zoneOffset the offset of the timezone the user works in
     * @param boundedContextName a name of the current bounded context
     * @return a new factory instance
     */
    public static CommandFactory newInstance(UserId actor, ZoneOffset zoneOffset, String boundedContextName) {
        return new CommandFactory(actor, zoneOffset, boundedContextName);
    }

    /**
     * Creates new factory with the same user and bounded context name and new time zone offset.
     *
     * @param zoneOffset the offset of the time zone
     * @return new command factory at new time zone
     */
    public CommandFactory switchTimezone(ZoneOffset zoneOffset) {
        return newInstance(getActor(), zoneOffset, getBoundedContextName());
    }

    public UserId getActor() {
        return actor;
    }

    public ZoneOffset getZoneOffset() {
        return zoneOffset;
    }

    public String getBoundedContextName() {
        return boundedContextName;
    }

    /**
     * Creates new {@code Command} with the passed message.
     *
     * <p>The command contains a {@code CommandContext} instance with the current time.
     *
     * @param message the command message
     * @return new command instance
     */
    public Command create(Message message) {
        checkNotNull(message);
        final CommandContext context = Commands.createContext(getActor(), getZoneOffset(), getBoundedContextName());
        final Command result = Commands.create(message, context);
        return result;
    }
}
