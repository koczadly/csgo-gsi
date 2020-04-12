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

@JsonAdapter(DeserializedEnum.DeserializerFactory.class)
public class DeserializedEnum<E extends Enum<E>> {
    
    private final E enumVal;
    private final String rawVal;
    
    public DeserializedEnum(E enumVal, String rawVal) {
        this.enumVal = enumVal;
        this.rawVal = rawVal;
    }
    
    
    /**
     * @return the parsed enum value, or null if not found
     */
    public E getEnum() {
        return enumVal;
    }
    
    /**
     * @return the raw value sent by the game client
     */
    public String getRawString() {
        return rawVal;
    }
    
    @Override
    public String toString() {
        return "DeserializedEnum{" +
                "enumVal=" + enumVal +
                ", rawVal='" + rawVal + '\'' +
                '}';
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeserializedEnum<?> that = (DeserializedEnum<?>)o;
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
            
            if (rawType.isAssignableFrom(DeserializedEnum.class)) {
                Type enumType = ((ParameterizedType)type.getType()).getActualTypeArguments()[0];
                TypeAdapter<?> enumAdapter = gson.getAdapter(TypeToken.get(enumType));
                
                @SuppressWarnings({"unchecked", "rawtypes"})
                TypeAdapter<T> result = new Deserializer(enumAdapter);
                return result;
            }
            return null;
        }
    }
    
    static class Deserializer<E extends Enum<E>> extends TypeAdapter<DeserializedEnum<E>> {
        TypeAdapter<E> enumAdapter;
        
        Deserializer(TypeAdapter<E> enumAdapter) {
            this.enumAdapter = enumAdapter;
        }
        
        
        @Override
        public DeserializedEnum<E> read(JsonReader in) throws IOException {
            String val = in.nextString();
            
            E enumVal = enumAdapter.read(new JsonReader(new StringReader("\"" + val + "\"")));
            return new DeserializedEnum<E>(enumVal, val);
        }
        
        @Override
        public void write(JsonWriter out, DeserializedEnum<E> value) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

}
