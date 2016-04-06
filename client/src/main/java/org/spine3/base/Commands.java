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

import com.google.common.base.Predicate;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.protobuf.EntityPackagesMap;
import org.spine3.protobuf.Messages;
import org.spine3.protobuf.Timestamps;
import org.spine3.time.ZoneOffset;
import org.spine3.type.TypeName;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.protobuf.util.TimeUtil.getCurrentTime;

/**
 * Client-side utilities for working with commands.
 *
 * @author Alexander Yevsyukov
 */
public class Commands {

    /**
     * A substring which the {@code .proto} file containing commands must have in its name.
     */
    public static final String COMMANDS_FILE_SUBSTRING = "commands";

    private static final char PROTO_FILE_SEPARATOR = '/';

    private Commands() {}

    /**
     * Creates a new {@link CommandId} based on random UUID.
     *
     * @return new command ID
     */
    public static CommandId generateId() {
        final String value = UUID.randomUUID().toString();
        return CommandId.newBuilder().setUuid(value).build();
    }

    /**
     * Creates new command context with the current time
     * @param userId the actor id
     * @param zoneOffset the offset of the timezone in which the user works
     */
    public static CommandContext createContext(UserId userId, ZoneOffset zoneOffset) {
        final CommandId commandId = generateId();
        final CommandContext.Builder result = CommandContext.newBuilder()
                .setActor(userId)
                .setTimestamp(getCurrentTime())
                .setCommandId(commandId)
                .setZoneOffset(zoneOffset);
        return result.build();
    }

    /**
     * Creates a command instance with the given {@code message} and the {@code context}.
     *
     * @param message the domain model message to send in the command
     * @param context the context of the command
     * @return a new command request
     */
    public static Command create(Message message, CommandContext context) {
        final Command.Builder request = Command.newBuilder()
                .setMessage(Messages.toAny(message))
                .setContext(context);
        return request.build();
    }

    /**
     * Extracts the message from the passed {@code Command} instance.
     */
    public static Message getMessage(Command command) {
        final Message result = Messages.fromAny(command.getMessage());
        return result;
    }

    /**
     * Creates a predicate for filtering commands created after the passed timestamp.
     */
    public static Predicate<Command> wereAfter(final Timestamp from) {
        return new Predicate<Command>() {
            @Override
            public boolean apply(@Nullable Command request) {
                checkNotNull(request);
                final Timestamp timestamp = getTimestamp(request);
                return Timestamps.isAfter(timestamp, from);
            }
        };
    }

    /**
     * Creates a predicate for filtering commands created withing given timerange.
     */
    public static Predicate<Command> wereWithinPeriod(final Timestamp from, final Timestamp to) {
        return new Predicate<Command>() {
            @Override
            public boolean apply(@Nullable Command request) {
                checkNotNull(request);
                final Timestamp timestamp = getTimestamp(request);
                return Timestamps.isBetween(timestamp, from, to);
            }
        };
    }

    private static Timestamp getTimestamp(Command request) {
        final Timestamp result = request.getContext().getTimestamp();
        return result;
    }

    /**
     * Sorts the command given command request list by command timestamp value.
     *
     * @param commands the command list to sort
     */
    public static void sort(List<Command> commands) {
        Collections.sort(commands, new Comparator<Command>() {
            @Override
            public int compare(Command o1, Command o2) {
                final Timestamp timestamp1 = getTimestamp(o1);
                final Timestamp timestamp2 = getTimestamp(o2);
                return Timestamps.compare(timestamp1, timestamp2);
            }
        });
    }

    /**
     * Creates a formatted string with type and ID of the passed command.
     *
     * <p>The {@code format} string must have two {@code %s} format specifiers.
     * The first specifier is for command type name. The second is for command ID.
     *
     * @param format the format string with two parameters
     * @param command the command to log
     * @return formatted string
     */
    public static String formatCommandTypeAndId(String format, Command command) {
        final CommandId commandId = command.getContext().getCommandId();
        final Message commandMessage = getMessage(command);

        return formatMessageTypeAndId(format, commandMessage, commandId);
    }

    /**
     * Creates a formatted string with type of the command message and command ID.
     *
     * <p>The {@code format} string must have two {@code %s} format specifiers.
     * The first specifier is for message type name. The second is for command ID.
     *
     * @param format the format string
     * @param commandId the ID of the command
     * @return formatted string
     */
    public static String formatMessageTypeAndId(String format, Message commandMessage, CommandId commandId) {
        checkNotNull(format);
        checkArgument(!format.isEmpty());

        final TypeName commandType = TypeName.of(commandMessage);
        final String id = commandId.getUuid();
        final String result = String.format(format, commandType, id);
        return result;
    }

    /**
     * Checks if the file is for commands.
     *
     * @param file a descriptor of a {@code .proto} file to check
     * @return {@code true} if the file name contains {@link #COMMANDS_FILE_SUBSTRING} substring, {@code false} otherwise
     */
    public static boolean isCommandsFile(FileDescriptor file) {
        final String fqn = file.getName();
        final int startIndexOfFileName = fqn.lastIndexOf(PROTO_FILE_SEPARATOR) + 1;
        final String fileName = fqn.substring(startIndexOfFileName);
        final boolean isCommandsFile = fileName.contains(COMMANDS_FILE_SUBSTRING);
        return isCommandsFile;
    }

    /**
     * Checks if the file belongs to an entity.
     *
     * <p>See {@link EntityPackagesMap} for more info.
     *
     * @param file a descriptor of a {@code .proto} file to check
     * @return {@code true} if the file belongs to an entity, {@code false} otherwise
     */
    public static boolean isEntityFile(FileDescriptor file) {
        final String protoPackage = file.getPackage();
        final boolean isCommandForEntity = EntityPackagesMap.contains(protoPackage);
        return isCommandForEntity;
    }
}
