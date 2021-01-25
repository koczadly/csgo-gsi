package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import uk.oczadly.karl.csgsi.internal.Util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class is a wrapper for {@code Enum} values, allowing for cases where the corresponding enum constant cannot be
 * parsed, while still retaining the original serialized string information.
 *
 * In most implementations, the {@link #get()} method can be used to retrieve the enum value as normal. For cases
 * where the originally returned value could not be parsed as an enum constant (resulting in a null enum value), the
 * {@link #getString()} value will return the raw serialized value received from the game client.
 *
 * @param <E> the enum class
 *
 * @see #get()
 */
@JsonAdapter(EnumValue.DeserializerFactory.class)
public class EnumValue<E> {
    
    private final E enumVal;
    private final String rawVal;
    
    public EnumValue(E enumVal, String rawVal) {
        if (rawVal == null)
            throw new IllegalArgumentException("The raw string value cannot be null.");
        
        this.enumVal = enumVal;
        this.rawVal = rawVal;
    }
    
    
    /**
     * Returns the parsed enum value, or null in cases where the corresponding enum constant could not be parsed. If
     * the game client did not send a null value, and the value returned from this method is null, then the
     * {@link #getString()} method will return the serialized string value.
     *
     * @return the parsed enum value, or null if not found
     */
    public E get() {
        return enumVal;
    }
    
    /**
     * Returns the raw string value sent by the game client. This method should be preferred for logging or storing
     * game state data, as it will always contain the correct value sent by the game client.
     * @return the raw value sent by the game client
     */
    public String getString() {
        return rawVal;
    }
    
    /**
     * Returns whether the enum value could be resolved or not. If this value is false, {@link #get()} will return a
     * null value and {@link #getString()} should be used instead.
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
        if (isResolved())
            consumer.accept(get());
    }
    
    /**
     * Returns a string value of the contained object. If the enum can be resolved, then the {@link Enum#toString()}
     * value of the enum will be returned — if not, then the raw string value will be used instead.
     *
     * @return a string representation of this value
     */
    @Override
    public String toString() {
        return isResolved() ? get().toString() : getString();
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumValue<?> that = (EnumValue<?>)o;
        return enumVal == that.enumVal &&
                Objects.equals(rawVal, that.rawVal);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(enumVal, rawVal);
    }
    
    
    /**
     * Returns an {@link EnumValue} object containing the raw string, and value if resolved.
     * @param strVal the raw string value
     * @param clazz  the parsed object/enum class type
     * @param gson   the {@link Gson} object used to parse the value
     * @param <T>    the parsed object/enum type
     * @return an {@link EnumValue} containing the object
     */
    public static <T> EnumValue<T> of(String strVal, Class<T> clazz, Gson gson) {
        return of(strVal, gson.getAdapter(clazz));
    }
    
    private static <T> EnumValue<T> of(String strVal, TypeAdapter<T> adapter) {
        T val = adapter.fromJsonTree(new JsonPrimitive(strVal));
        return new EnumValue<>(val, strVal);
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
                    enumAdapter.write(out, value.get());
                } else {
                    out.value(value.getString());
                }
            }
        }
    }
    
}
