package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is a wrapper for {@code Enum} values, allowing for cases where the corresponding enum constant cannot be
 * parsed, while still retaining the original serialized string information.
 * <p>
 * In most implementations, the {@link #asEnum()} method can be used to retrieve the enum value as normal. For cases
 * where the originally returned value could not be parsed as an enum constant (resulting in a null enum value), the
 * {@link #asString()} value will return the raw serialized value received from the game client.
 *
 * @param <E> the enum class
 *
 * @see #asEnum()
 */
@JsonAdapter(EnumValue.DeserializerFactory.class)
public final class EnumValue<E extends Enum<E>> {
    
    private final E enumVal;
    private final String stringVal;


    public EnumValue(E enumVal, String stringVal) {
        if (stringVal == null)
            throw new IllegalArgumentException("The raw string value cannot be null.");

        this.enumVal = enumVal;
        this.stringVal = stringVal;
    }
    
    
    /**
     * Returns the parsed enum value, or null in cases where the corresponding enum constant could not be parsed. If
     * the game client did not send a null value, and the value returned from this method is null, then the
     * {@link #asString()} method will return the serialized string value.
     *
     * @return the parsed enum value, or null if not resolved
     */
    public E asEnum() {
        return enumVal;
    }

    /**
     * Returns an optional containing the parsed enum value, or empty if it could not be resolved into an enum instance.
     *
     * @return the parsed enum value, or empty if not resolved
     */
    public Optional<E> asOptional() {
        return Optional.ofNullable(enumVal);
    }

    /**
     * Returns the raw string value sent by the game client. This method should be preferred for logging or storing
     * game state data, as it will always contain the correct value sent by the game client.
     * @return the raw value sent by the game client
     */
    public String asString() {
        return stringVal;
    }
    
    /**
     * Returns whether the enum value could be resolved or not. If this value is false, {@link #asEnum()} will return a
     * null value and {@link #asString()} should be used instead.
     * @return true if the value is resolved
     */
    public boolean isResolved() {
        return enumVal != null;
    }

    /**
     * If the value is resolved, then execute the given function.
     * @param consumer the consumer function to execute
     */
    public void ifResolved(Consumer<? super E> consumer) {
        if (isResolved()) consumer.accept(asEnum());
    }


    /**
     * A case-insensitive test for whether the given {@code value} String matches the raw string value.
     * @param value the string value to compare against
     * @return true if {@code value} is equal to the raw string value
     */
    public boolean valueEquals(String value) {
        return stringVal.equalsIgnoreCase(value);
    }

    
    /**
     * Returns a string value of the contained object. If the enum can be resolved, then the {@link Enum#toString()}
     * value of the enum will be returned — if not, then the raw string value will be used instead.
     *
     * @return a string representation of this value
     */
    @Override
    public String toString() {
        return isResolved() ? asEnum().toString() : asString();
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumValue<?> that = (EnumValue<?>)o;
        return enumVal == that.enumVal &&
                Objects.equals(stringVal, that.stringVal);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enumVal, stringVal);
    }
    
    
    /**
     * Returns an {@link EnumValue} object containing the raw string, and value if resolved.
     * @param strVal the raw string value
     * @param clazz  the parsed object/enum class type
     * @param gson   the {@link Gson} object used to parse the value
     * @param <T>    the parsed object/enum type
     * @return an {@link EnumValue} containing the object
     */
    public static <T extends Enum<T>> EnumValue<T> of(String strVal, Class<T> clazz, Gson gson) {
        return of(strVal, gson.getAdapter(clazz));
    }
    
    private static <T extends Enum<T>> EnumValue<T> of(String strVal, TypeAdapter<T> adapter) {
        return new EnumValue<>(adapter.fromJsonTree(new JsonPrimitive(strVal)), strVal);
    }
    
    
    static class DeserializerFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<? super T> rawType = type.getRawType();
            
            if (rawType.isAssignableFrom(EnumValue.class)) {
                Type enumType = ((ParameterizedType)type.getType()).getActualTypeArguments()[0];
                TypeAdapter<?> enumAdapter = gson.getAdapter(TypeToken.get(enumType));
                
                @SuppressWarnings({"unchecked", "rawtypes"})
                TypeAdapter<T> result = new Deserializer(enumAdapter);
                return result;
            }
            return null;
        }
    
        static class Deserializer<E extends Enum<E>> extends TypeAdapter<EnumValue<E>> {
            TypeAdapter<E> enumAdapter;
        
            Deserializer(TypeAdapter<E> enumAdapter) {
                this.enumAdapter = enumAdapter;
            }
        
        
            @Override
            public EnumValue<E> read(JsonReader in) throws IOException {
                return of(in.nextString(), enumAdapter);
            }
        
            @Override
            public void write(JsonWriter out, EnumValue<E> value) throws IOException {
                if (value.isResolved()) {
                    enumAdapter.write(out, value.asEnum());
                } else {
                    out.value(value.asString());
                }
            }
        }
    }
    
}
