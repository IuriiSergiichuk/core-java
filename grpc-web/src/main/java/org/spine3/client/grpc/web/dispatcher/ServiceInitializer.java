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

package org.spine3.client.grpc.web.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.client.grpc.web.services.RpcService;
import org.spine3.io.IoUtil;
import org.spine3.util.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.lang.String.format;

/**
 * Simple initializer for generated gRPC services.
 * <p/>
 * Uses property files to get information about servies. Registers them for {@link DispatcherServlet}.
 */
/* package */ class ServiceInitializer {
    private static final String DISPATCHABLE_SERVICES_FILE_PATH = "dispatchable_services.list";
    private static final String NEW_STUB_METHOD_NAME = "newStub";

    /* package */ void initializeServices(Dispatcher dispatcher) {
        final List<String> classNames = readResources();
        for (String className : classNames) {
            try {
                @SuppressWarnings("unchecked") // This should always be ok
                final Class<RpcService> aClass = (Class<RpcService>) Class.forName(className);

                final Method newStubMethod = aClass.getMethod(NEW_STUB_METHOD_NAME);
                final RpcService methodResult = (RpcService) newStubMethod.invoke(null);
                dispatcher.registerService(aClass, methodResult);
            } catch (ClassNotFoundException e) {
                error("Could not load class: " + className, e);
            } catch (NoSuchMethodException e) {
                error(format("Class does not have %s method: %s.", NEW_STUB_METHOD_NAME, className), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                error(format("Could execute %s(): %s.", NEW_STUB_METHOD_NAME, className), e);
            }
        }
    }

    /**
     * Reads class names from resource files.
     *
     * @return List of class full names.
     */
    private static List<String> readResources() {
        final Enumeration<URL> resourceUrls =
                Resources.getResourceUrls(DISPATCHABLE_SERVICES_FILE_PATH);
        final List<String> classNames = new ArrayList<>();
        while (resourceUrls.hasMoreElements()) {
            final URL resourceUrl = resourceUrls.nextElement();
            classNames.addAll(readClassNamesFromFile(resourceUrl));
        }
        return classNames;
    }

    @SuppressWarnings({"TypeMayBeWeakened", "NestedAssignment"}) // The return type wasn't supposed to be weakened
    private static List<String> readClassNamesFromFile(URL resourceFileUrl) {
        final List<String> classNames = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = resourceFileUrl.openStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                classNames.add(line);
            }

            reader.close();
        } catch (IOException e) {
            error("Failed to read file.", e);
        } finally {
            IoUtil.closeSilently(inputStream);
        }
        return classNames;
    }

    private static void error(String message, Throwable throwable) {
        final Logger log = LogSingleton.INSTANCE.value;
        if (log.isErrorEnabled()) {
            log.error(message, throwable);
        }
        throw new ServiceInitializationException(message, throwable);
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ServiceInitializer.class);
    }

    private static class ServiceInitializationException extends RuntimeException {

        private static final long serialVersionUID = 8000399024325706117L;

        /* package */ ServiceInitializationException(String message, Throwable rootCause) {
            super(message, rootCause);
        }
    }
}