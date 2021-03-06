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

package org.spine3.server.command;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.protobuf.Message;
import org.spine3.server.procman.ProcessManagerRepository;
import org.spine3.server.reflect.Classes;
import org.spine3.server.reflect.CommandHandlerMethod;
import org.spine3.server.type.CommandClass;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.spine3.server.command.Log.log;

/**
 * The registry of objects dispatching command request to where they are processed.
 *
 * <p>There can be only one dispatcher per command class.
 *
 * @author Alexander Yevsyukov
 */
class DispatcherRegistry {

    private final Map<CommandClass, CommandDispatcher> dispatchers = Maps.newHashMap();

    /**
     * Ensures that the dispatcher forwards at least one command.
     *
     * <p>Note that {@link org.spine3.server.procman.ProcessManager} is allowed to have no commands handled,
     * since its business flow may be designed to handle events only.
     *
     * <p>We pass the {@code dispatcher} and the set as arguments instead of getting the set
     * from the dispatcher because this operation is expensive and
     *
     * @throws IllegalArgumentException if the dispatcher returns empty set of command classes
     * @throws NullPointerException     if the dispatcher returns null set
     */
    private static void checkNotEmpty(CommandDispatcher dispatcher, Set<CommandClass> commandClasses) {

        /**
         * Instances of {@link ProcessManagerRepository} are excluded from the check,
         * as long as {@link org.spine3.server.procman.ProcessManager}s may have no commands handled.
         */
        final boolean criterionSatisfied = !commandClasses.isEmpty()
                || (dispatcher instanceof ProcessManagerRepository);

        checkArgument(criterionSatisfied,
                      "No command classes are forwarded by this dispatcher: %s", dispatcher);
    }

    private static void warnUnregisterNotPossible(CommandClass commandClass,
                                                  CommandDispatcher registeredDispatcher,
                                                  CommandDispatcher dispatcher) {
        log().warn(
            "Another dispatcher {} found when trying to unregister the dispatcher {} for the command class {}." +
                " There can be only one dispatcher per command class. The passed dispatcher cannot be unregistered. " +
                " Please check dispatcher registration for this command class.",
            registeredDispatcher, dispatcher, commandClass, dispatcher);
    }

    void register(CommandDispatcher dispatcher) {
        checkNotNull(dispatcher);
        final Set<CommandClass> commandClasses = dispatcher.getCommandClasses();
        checkNotEmpty(dispatcher, commandClasses);
        checkNotAlreadyRegistered(dispatcher, commandClasses);

        for (CommandClass commandClass : commandClasses) {
            dispatchers.put(commandClass, dispatcher);
        }
    }

    Set<CommandClass> getCommandClasses() {
        return dispatchers.keySet();
    }

    /**
     * Ensures that all of the commands of the passed dispatcher are not
     * already registered for dispatched in this command bus.
     *
     * @throws IllegalArgumentException if at least one command class already has registered dispatcher
     */
    private void checkNotAlreadyRegistered(CommandDispatcher dispatcher, Set<CommandClass> commandClasses) {
        final Set<CommandClass> alreadyRegistered = Sets.newHashSet();
        // Gather command classes from this dispatcher that are registered.
        for (CommandClass commandClass : commandClasses) {
            if (getDispatcher(commandClass) != null) {
                alreadyRegistered.add(commandClass);
            }
        }
        RegistryUtil.checkNotAlreadyRegistered(
            alreadyRegistered,
            dispatcher,
           "Cannot register dispatcher %s for the command class %s which already has registered dispatcher.",
           "Cannot register dispatcher %s for command classes (%s) which already have registered dispatcher(s).");
    }

    void unregister(CommandDispatcher dispatcher) {
        checkNotNull(dispatcher);
        final Set<CommandClass> commandClasses = dispatcher.getCommandClasses();
        checkNotEmpty(dispatcher, commandClasses);

        for (CommandClass commandClass : commandClasses) {
            final CommandDispatcher registeredDispatcher = dispatchers.get(commandClass);
            if (dispatcher.equals(registeredDispatcher)) {
                dispatchers.remove(commandClass);
            } else {
                warnUnregisterNotPossible(commandClass, registeredDispatcher, dispatcher);
            }
        }
    }

    boolean hasDispatcherFor(CommandClass commandClass) {
        final CommandDispatcher dispatcher = getDispatcher(commandClass);
        return dispatcher != null;
    }

    void unregisterAll() {
        dispatchers.clear();
    }

    CommandDispatcher getDispatcher(CommandClass commandClass) {
        return dispatchers.get(commandClass);
    }

    /**
     * Ensures that there are no dispatchers registered for the commands of the passed handler.
     *
     * @param handler the command handler to check
     * @throws IllegalArgumentException if one ore more command classes already have registered dispatchers
     */
    @SuppressWarnings("InstanceMethodNamingConvention")         // prefer longer name here for clarity.
    void checkNoDispatchersRegisteredForCommandsOf(CommandHandler handler) {
        final ImmutableSet<Class<? extends Message>> handledMessageClasses = Classes.getHandledMessageClasses(
                handler.getClass(), CommandHandlerMethod.PREDICATE);

        final Set<CommandClass> alreadyRegistered = Sets.newHashSet();
        for (Class<? extends Message> handledMessageClass : handledMessageClasses) {
            final CommandClass commandClass = CommandClass.of(handledMessageClass);
            if (hasDispatcherFor(commandClass)) {
                alreadyRegistered.add(commandClass);
            }
        }

        RegistryUtil.checkNotAlreadyRegistered(alreadyRegistered, handler,
                                               "Cannot register handler %s for the command class %s which already has registered dispatcher.",
                                               "Cannot register handler %s for command classes (%s) which already have registered dispatcher(s).");
    }
}
