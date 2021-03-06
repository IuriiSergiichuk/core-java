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

package org.spine3.server.event.enrich;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.protobuf.Internal;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.annotations.EventAnnotationsProto;
import org.spine3.base.EventContext;
import org.spine3.protobuf.Messages;

import java.util.List;

import static com.google.protobuf.Descriptors.Descriptor;
import static com.google.protobuf.Descriptors.FieldDescriptor;
import static java.lang.String.format;

/**
 * Performs validation analyzing which of fields annotated in the enrichment message
 * can be initialized with the translation functions supplied in the parent enricher.
 *
 * <p>As long as the new enrichment functions may be appended to the parent enricher at runtime,
 * the validation result will vary for the same enricher depending on its actual state.
 *
 * @author Alexander Yevsyukov
 */
class ReferenceValidator {

    /** The separator used in Protobuf fully-qualified names. */
    private static final String PROTO_FQN_SEPARATOR = ".";

    /** The reference to the event context used in the `by` field option. */
    private static final String CONTEXT_REFERENCE = "context";

    private final EventEnricher enricher;
    private final Descriptor eventDescriptor;
    private final Descriptor enrichmentDescriptor;

    ReferenceValidator(EventEnricher enricher,
            Class<? extends Message> eventClass,
            Class<? extends Message> enrichmentClass) {
        this.enricher = enricher;
        this.eventDescriptor = Internal.getDefaultInstance(eventClass)
                                       .getDescriptorForType();
        this.enrichmentDescriptor = Internal.getDefaultInstance(enrichmentClass)
                                            .getDescriptorForType();
    }

    /**
     * Returns those fields and functions, that may be used for the enrichment at the moment.
     *
     * @return a {@code ValidationResult} data transfer object, containing the valid fields and functions.
     */
    ValidationResult validate() {
        final ImmutableList.Builder<EnrichmentFunction<?, ?>> functions = ImmutableList.builder();
        final ImmutableMultimap.Builder<FieldDescriptor, FieldDescriptor> fields = ImmutableMultimap.builder();
        for (FieldDescriptor enrichmentField : enrichmentDescriptor.getFields()) {
            final FieldDescriptor sourceField = findSourceField(enrichmentField);
            final Optional<EnrichmentFunction<?, ?>> function = getEnrichmentFunction(sourceField, enrichmentField);
            if (function.isPresent()) {
                functions.add(function.get());
                fields.put(sourceField, enrichmentField);
            }
        }
        final ImmutableMultimap<FieldDescriptor, FieldDescriptor> fieldMap = fields.build();
        final ImmutableList<EnrichmentFunction<?, ?>> functionList = functions.build();
        final ValidationResult result = new ValidationResult(functionList, fieldMap);
        return result;
    }

    /** Searches for the event/context field with the name parsed from the enrichment field `by` option. */
    private FieldDescriptor findSourceField(FieldDescriptor enrichmentField) {
        final String fieldName = enrichmentField.getOptions()
                                                .getExtension(EventAnnotationsProto.by);
        checkSourceFieldName(fieldName, enrichmentField);
        final Descriptor srcMessage = getSrcMessage(fieldName);
        final FieldDescriptor field = findField(fieldName, srcMessage);
        if (field == null) {
            throw noFieldException(fieldName, srcMessage, enrichmentField);
        }
        return field;
    }

    private static FieldDescriptor findField(String fieldNameFull, Descriptor srcMessage) {
        if (fieldNameFull.contains(PROTO_FQN_SEPARATOR)) { // is event field FQN or context field
            final int firstCharIndex = fieldNameFull.lastIndexOf(PROTO_FQN_SEPARATOR) + 1;
            final String fieldName = fieldNameFull.substring(firstCharIndex);
            return srcMessage.findFieldByName(fieldName);
        } else {
            return srcMessage.findFieldByName(fieldNameFull);
        }
    }

    /**
     * Returns an event descriptor or context descriptor
     * if the field name contains {@link ReferenceValidator#CONTEXT_REFERENCE}.
     */
    private Descriptor getSrcMessage(String fieldName) {
        final Descriptor msg = fieldName.contains(CONTEXT_REFERENCE)
                               ? EventContext.getDescriptor()
                               : eventDescriptor;
        return msg;
    }

    private Optional<EnrichmentFunction<?, ?>> getEnrichmentFunction(FieldDescriptor srcField,
                                                                     FieldDescriptor targetField) {
        final Class<?> sourceFieldClass = Messages.getFieldClass(srcField);
        final Class<?> targetFieldClass = Messages.getFieldClass(targetField);
        final Optional<EnrichmentFunction<?, ?>> func = enricher.functionFor(sourceFieldClass, targetFieldClass);
        if (!func.isPresent()) {
            logNoFunction(sourceFieldClass, targetFieldClass);
        }
        return func;
    }

    /** Checks if the source field name (from event or context) is not empty. */
    private static void checkSourceFieldName(String srcFieldName, FieldDescriptor enrichmentField) {
        if (srcFieldName.isEmpty()) {
            final String msg = format("There is no `by` option for the enrichment field `%s`",
                                      enrichmentField.getFullName());
            throw new IllegalStateException(msg);
        }
    }

    private static IllegalStateException noFieldException(
            String eventFieldName,
            Descriptor srcMessage,
            FieldDescriptor enrichmentField) {
        final String msg = format(
                "No field `%s` in the message `%s` found. " +
                "The field is referenced in the option of the enrichment field `%s`.",
                eventFieldName,
                srcMessage.getFullName(),
                enrichmentField.getFullName());
        throw new IllegalStateException(msg);
    }

    private static void logNoFunction(Class<?> sourceFieldClass, Class<?> targetFieldClass) {
        // Using `DEBUG` level to avoid polluting the `stderr`.
        if (log().isDebugEnabled()) {
            log().debug("There is no enrichment function for translating {} into {}",
                        sourceFieldClass, targetFieldClass);
        }
    }

    /**
     * A wrapper DTO for the validation result.
     */
    static class ValidationResult {
        private final ImmutableList<EnrichmentFunction<?, ?>> functions;
        private final ImmutableMultimap<FieldDescriptor, FieldDescriptor> fieldMap;

        private ValidationResult(ImmutableList<EnrichmentFunction<?, ?>> functions,
                                 ImmutableMultimap<FieldDescriptor, FieldDescriptor> fieldMap) {
            this.functions = functions;
            this.fieldMap = fieldMap;
        }

        /**
         * Returns the validated list of {@code EnrichmentFunction}s that may be used for the conversion
         * in scope of the validated {@code EventEnricher}.
         */
        @SuppressWarnings("ReturnOfCollectionOrArrayField")     // OK, since an `ImmutableList` is returned.
        List<EnrichmentFunction<?, ?>> getFunctions() {
            return functions;
        }

        /**
         * Returns a map from source event/context field to target enrichment field descriptors,
         * which is valid in scope of the target {@code EventEnricher}.
         */
        ImmutableMultimap<FieldDescriptor, FieldDescriptor> getFieldMap() {
            return fieldMap;
        }
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(ReferenceValidator.class);
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }
}
