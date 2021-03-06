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

package org.spine3.protobuf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Any;
import com.google.protobuf.Api;
import com.google.protobuf.BoolValue;
import com.google.protobuf.BytesValue;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.Duration;
import com.google.protobuf.Empty;
import com.google.protobuf.EnumValue;
import com.google.protobuf.Field;
import com.google.protobuf.FieldMask;
import com.google.protobuf.FloatValue;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.Internal.EnumLite;
import com.google.protobuf.ListValue;
import com.google.protobuf.Message;
import com.google.protobuf.Method;
import com.google.protobuf.Mixin;
import com.google.protobuf.NullValue;
import com.google.protobuf.Option;
import com.google.protobuf.SourceContext;
import com.google.protobuf.StringValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Syntax;
import com.google.protobuf.Timestamp;
import com.google.protobuf.Type;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spine3.Internal;
import org.spine3.protobuf.error.UnknownTypeException;
import org.spine3.type.ClassName;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static org.spine3.io.IoUtil.loadAllProperties;

/**
 * A map which contains all Protobuf types known to the application.
 *
 * @author Mikhail Mikhaylov
 * @author Alexander Yevsyukov
 * @author Alexander Litus
 */
@Internal
public class KnownTypes {

    /**
     * File, containing Protobuf messages' typeUrls and their appropriate class names.
     *
     * <p>Is generated by Gradle during build process.
     */
    private static final String PROPS_FILE_PATH = "known_types.properties";

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static final String DESCRIPTOR_GETTER_NAME = "getDescriptor";

    private static final char PACKAGE_SEPARATOR = '.';

    /**
     * A map from Protobuf type URL to Java class name.
     *
     * <p>For example, for a key {@code type.spine3.org/spine.base.EventId},
     * there will be the value {@code org.spine3.base.EventId}.
     */
    private static final BiMap<TypeUrl, ClassName> knownTypes = Builder.build();

    /**
     * A map from Protobuf type name to type URL.
     *
     * <p>For example, for a key {@code spine.base.EventId},
     * there will be the value {@code type.spine3.org/spine.base.EventId}.
     *
     * @see TypeUrl
     */
    private static final ImmutableMap<String, TypeUrl> typeNameToUrlMap = buildTypeToUrlMap(knownTypes);

    private KnownTypes() {
    }

    /** Retrieves Protobuf type URLs known to the application. */
    public static ImmutableSet<TypeUrl> getTypeUrls() {
        final Set<TypeUrl> result = knownTypes.keySet();
        return ImmutableSet.copyOf(result);
    }

    /** Retrieves names of Java classes generated for Protobuf types known to the application. */
    @VisibleForTesting
    public static ImmutableSet<ClassName> getJavaClasses() {
        final Set<ClassName> result = knownTypes.values();
        return ImmutableSet.copyOf(result);
    }

    /**
     * Retrieves a Java class name generated for the Protobuf type by its type url
     * to be used to parse {@link Message} from {@link Any}.
     *
     * @param typeUrl {@link Any} type url
     * @return Java class name
     * @throws UnknownTypeException if there is no such type known to the application
     */
    public static ClassName getClassName(TypeUrl typeUrl) throws UnknownTypeException {
        if (!knownTypes.containsKey(typeUrl)) {
            throw new UnknownTypeException(typeUrl.getTypeName());
        }
        final ClassName result = knownTypes.get(typeUrl);
        return result;
    }

    /**
     * Returns the Protobuf name for the class with the given name.
     *
     * @param className the name of the Java class for which to get Protobuf type
     * @return a Protobuf type name
     * @throws IllegalStateException if there is no Protobuf type for the specified class
     */
    public static TypeUrl getTypeUrl(ClassName className) {
        final TypeUrl result = knownTypes.inverse()
                                         .get(className);
        if (result == null) {
            throw new IllegalStateException("No Protobuf type URL found for the Java class " + className);
        }
        return result;
    }

    /** Returns a Protobuf type URL by Protobuf type name. */
    @Nullable
    public static TypeUrl getTypeUrl(String typeName) {
        final TypeUrl typeUrl = typeNameToUrlMap.get(typeName);
        return typeUrl;
    }

    /**
     * Retrieves all the types that belong to the given package or it's subpackages.
     *
     * @param packageName protobuf-style package
     * @return set of {@link TypeUrl TypeUrls} for types that belong to the given package
     */
    public static Set<TypeUrl> getTypesFromPackage(final String packageName) {
        final Collection<TypeUrl> knownTypeUrls = knownTypes.keySet();
        final Collection<TypeUrl> resultCollection = Collections2.filter(knownTypeUrls, new Predicate<TypeUrl>() {
            @Override
            public boolean apply(@Nullable TypeUrl input) {
                if (input == null) {
                    return false;
                }

                final String typeName = input.getTypeName();
                final boolean inPackage = typeName.startsWith(packageName)
                        && typeName.charAt(packageName.length()) == PACKAGE_SEPARATOR;
                return inPackage;
            }
        });

        final Set<TypeUrl> resultSet = ImmutableSet.copyOf(resultCollection);
        return resultSet;
    }

    /**
     * Retrieve {@link Descriptors proto descriptor} from the type name.
     *
     * @param typeName <b>valid</b> name of the desired type
     * @return {@link Descriptors proto descriptor} for given type
     * @see TypeName
     * @throws IllegalArgumentException if the name does not correspond to any known type
     */
    public static Descriptors.Descriptor getDescriptorForType(String typeName) {
        final TypeUrl typeUrl = getTypeUrl(typeName);
        checkArgument(typeUrl != null, "Given type name is invalid");

        final ClassName className = getClassName(typeUrl);
        final Class<?> clazz;
        try {
            clazz = Class.forName(className.value());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        final Descriptors.Descriptor descriptor;
        try {
            final java.lang.reflect.Method descriptorGetter = clazz.getDeclaredMethod(DESCRIPTOR_GETTER_NAME);
            descriptor = (Descriptors.Descriptor) descriptorGetter.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Type " + typeName + " is not a protobuf Message", e);
        }
        return descriptor;
    }

    private static ImmutableMap<String, TypeUrl> buildTypeToUrlMap(BiMap<TypeUrl, ClassName> knownTypes) {
        final ImmutableMap.Builder<String, TypeUrl> builder = ImmutableMap.builder();
        for (TypeUrl typeUrl : knownTypes.keySet()) {
            builder.put(typeUrl.getTypeName(), typeUrl);
        }
        return builder.build();
    }

    /** The helper class for building internal immutable typeUrl-to-JavaClass map. */
    private static class Builder {

        private final Map<TypeUrl, ClassName> resultMap = newHashMap();

        private static ImmutableBiMap<TypeUrl, ClassName> build() {
            final Builder builder = new Builder()
                    .addStandardProtobufTypes()
                    .loadNamesFromProperties();
            final ImmutableBiMap<TypeUrl, ClassName> result = ImmutableBiMap.copyOf(builder.resultMap);
            return result;
        }

        private Builder loadNamesFromProperties() {
            final Set<Properties> propertiesSet = loadAllProperties(PROPS_FILE_PATH);
            for (Properties properties : propertiesSet) {
                putProperties(properties);
            }
            return this;
        }

        private void putProperties(Properties properties) {
            final Set<String> typeUrls = properties.stringPropertyNames();
            for (String typeUrlStr : typeUrls) {
                final TypeUrl typeUrl = TypeUrl.of(typeUrlStr);
                final ClassName className = ClassName.of(properties.getProperty(typeUrlStr));
                put(typeUrl, className);
            }
        }

        /**
         * Returns classes from the {@code com.google.protobuf} package that need to be present
         * in the result map.
         *
         * <p>This method needs to be updated with introduction of new Google Protobuf types
         * after they are used in the framework.
         */
        @SuppressWarnings("OverlyLongMethod")
        // OK as there are many types in Protobuf and we want to keep this code in one place.
        private Builder addStandardProtobufTypes() {
            // Types from `any.proto`.
            put(Any.class);

            // Types from `api.proto`
            put(Api.class);
            put(Method.class);
            put(Mixin.class);

            // Types from `descriptor.proto`
            put(DescriptorProtos.FileDescriptorSet.class);
            put(DescriptorProtos.FileDescriptorProto.class);
            put(DescriptorProtos.DescriptorProto.class);
            // Inner types of `DescriptorProto`
            put(DescriptorProtos.DescriptorProto.ExtensionRange.class);
            put(DescriptorProtos.DescriptorProto.ReservedRange.class);

            put(DescriptorProtos.FieldDescriptorProto.class);
            putEnum(DescriptorProtos.FieldDescriptorProto.Type.getDescriptor(),
                    DescriptorProtos.FieldDescriptorProto.Type.class);
            putEnum(DescriptorProtos.FieldDescriptorProto.Label.getDescriptor(),
                    DescriptorProtos.FieldDescriptorProto.Label.class);

            put(DescriptorProtos.OneofDescriptorProto.class);
            put(DescriptorProtos.EnumDescriptorProto.class);
            put(DescriptorProtos.EnumValueDescriptorProto.class);
            put(DescriptorProtos.ServiceDescriptorProto.class);
            put(DescriptorProtos.MethodDescriptorProto.class);
            put(DescriptorProtos.FileOptions.class);
            putEnum(DescriptorProtos.FileOptions.OptimizeMode.getDescriptor(),
                    DescriptorProtos.FileOptions.OptimizeMode.class);
            put(DescriptorProtos.MessageOptions.class);
            put(DescriptorProtos.FieldOptions.class);
            putEnum(DescriptorProtos.FieldOptions.CType.getDescriptor(),
                    DescriptorProtos.FieldOptions.CType.class);
            putEnum(DescriptorProtos.FieldOptions.JSType.getDescriptor(),
                    DescriptorProtos.FieldOptions.JSType.class);
            put(DescriptorProtos.EnumOptions.class);
            put(DescriptorProtos.EnumValueOptions.class);
            put(DescriptorProtos.ServiceOptions.class);
            put(DescriptorProtos.MethodOptions.class);
            put(DescriptorProtos.UninterpretedOption.class);
            put(DescriptorProtos.SourceCodeInfo.class);
            // Inner types of `SourceCodeInfo`.
            put(DescriptorProtos.SourceCodeInfo.Location.class);
            put(DescriptorProtos.GeneratedCodeInfo.class);
            // Inner types of `GeneratedCodeInfo`.
            put(DescriptorProtos.GeneratedCodeInfo.Annotation.class);

            // Types from `duration.proto`.
            put(Duration.class);

            // Types from `empty.proto`.
            put(Empty.class);

            // Types from `field_mask.proto`.
            put(FieldMask.class);

            // Types from `source_context.proto`.
            put(SourceContext.class);

            // Types from `struct.proto`.
            put(Struct.class);
            put(Value.class);
            putEnum(NullValue.getDescriptor(), NullValue.class);
            put(ListValue.class);

            // Types from `timestamp.proto`.
            put(Timestamp.class);

            // Types from `type.proto`.
            put(Type.class);
            put(Field.class);
            putEnum(Field.Kind.getDescriptor(), Field.Kind.class);
            putEnum(Field.Cardinality.getDescriptor(), Field.Cardinality.class);
            put(com.google.protobuf.Enum.class);
            put(EnumValue.class);
            put(Option.class);
            putEnum(Syntax.getDescriptor(), Syntax.class);

            // Types from `wrappers.proto`.
            put(DoubleValue.class);
            put(FloatValue.class);
            put(Int64Value.class);
            put(UInt64Value.class);
            put(Int32Value.class);
            put(UInt32Value.class);
            put(BoolValue.class);
            put(StringValue.class);
            put(BytesValue.class);

            return this;
        }

        private void put(Class<? extends GeneratedMessageV3> clazz) {
            final TypeUrl typeUrl = TypeUrl.of(clazz);
            final ClassName className = ClassName.of(clazz);
            put(typeUrl, className);
        }

        private void putEnum(EnumDescriptor desc, Class<? extends EnumLite> enumClass) {
            final TypeUrl typeUrl = TypeUrl.from(desc);
            final ClassName className = ClassName.of(enumClass);
            put(typeUrl, className);
        }

        private void put(TypeUrl typeUrl, ClassName className) {
            if (resultMap.containsKey(typeUrl)) {
                log().warn("Duplicate key in the {} map: {}. " +
                                   "It may be caused by the `task.descriptorSetOptions.includeImports` option " +
                                   "set to `true` in the `build.gradle`.", KnownTypes.class.getName(), typeUrl);
                return;
            }
            resultMap.put(typeUrl, className);
        }

        private static Logger log() {
            return LogSingleton.INSTANCE.value;
        }

        private enum LogSingleton {
            INSTANCE;
            @SuppressWarnings("NonSerializableFieldInSerializableClass")
            private final Logger value = LoggerFactory.getLogger(KnownTypes.class);
        }
    }
}
