package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * This class is a wrapper for {@code Enum} values, allowing for cases where the corresponding enum constant cannot be
 * parsed, while still retaining the original serialized string information.
 *
 * In most implementations, the {@link #val()} method can be used to retrieve the enum value as normal. For cases
 * where the originally returned value could not be parsed as an enum constant (resulting in a null enum value), the
 * {@link #stringVal()} value will return the raw serialized value received from the game client.
 *
 * @param <E> the enum class
 *
 * @see #val()
 */
@JsonAdapter(EnumValue.DeserializerFactory.class)
public class EnumValue<E extends Enum<E>> {
    
    private final E enumVal;
    private final String rawVal;
    
    public EnumValue(E enumVal, String rawVal) {
        this.enumVal = enumVal;
        this.rawVal = rawVal;
    }
    
    
    /**
     * Returns the parsed enum value, or null in cases where the corresponding enum constant could not be parsed. If
     * the game client did not send a null value, and the value returned from this method is null, then the
     * {@link #stringVal()} method will return the serialized string value.
     *
     * @return the parsed enum value, or null if not found
     */
    public E val() {
        return enumVal;
    }
    
    /**
     * Returns the raw string value sent by the game client. This method should be preferred for logging or storing
     * game state data, as it will always contain the correct value sent by the game client.
     * @return the raw value sent by the game client
     */
    public String stringVal() {
        return rawVal;
    }
    
    /**
     * Returns whether the enum value was resolved or not. When this value is true, {@link #val()} won't return a
     * null value.
     * @return true if the value is resolved
     */
    public boolean isResolved() {
        return enumVal != null;
    }
    
    @Override
    public String toString() {
        return isResolved() ? val().toString() : stringVal();
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
    }
    
    static class Deserializer<E extends Enum<E>> extends TypeAdapter<EnumValue<E>> {
        TypeAdapter<E> enumAdapter;
        
        Deserializer(TypeAdapter<E> enumAdapter) {
            this.enumAdapter = enumAdapter;
        }
        
        
        @Override
        public EnumValue<E> read(JsonReader in) throws IOException {
            String val = in.nextString();
            E enumVal = enumAdapter.read(new JsonReader(new StringReader("\"" + val + "\"")));
            return new EnumValue<>(enumVal, val);
        }
        
        @Override
        public void write(JsonWriter out, EnumValue<E> value) throws IOException {
            enumAdapter.write(out, value.val());
        }
    }

}
