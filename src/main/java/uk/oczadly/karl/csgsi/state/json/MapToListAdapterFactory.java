package uk.oczadly.karl.csgsi.state.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Allows a map to be read as a list of values, ordered by the key.
 */
public class MapToListAdapterFactory implements TypeAdapterFactory {
    
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if(!List.class.isAssignableFrom(rawType)) return null;
        
        TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, typeToken);
        if(delegateAdapter == null) return null;
        
        TypeAdapter<TreeMap<String, ?>> mapAdapter = gson.getAdapter(new TypeToken<TreeMap<String, ?>>(){});
        
        return new Adapter(delegateAdapter, mapAdapter);
    }
    
    
    private static class Adapter<T> extends TypeAdapter<List<T>> {
        
        private TypeAdapter<List<T>> delegateAdapter;
        private TypeAdapter<TreeMap<String, T>> mapAdapter;
        
        public Adapter(TypeAdapter<List<T>> delegateAdapter, TypeAdapter<TreeMap<String, T>> mapAdapter) {
            this.delegateAdapter = delegateAdapter;
            this.mapAdapter = mapAdapter;
        }
        
        
        @Override
        public void write(JsonWriter out, List<T> value) {}
        
        @Override
        public List<T> read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.BEGIN_OBJECT) {
                TreeMap<String, T> map = mapAdapter.read(in);
                
                List<T> list = new ArrayList<>(map.size());
                list.addAll(map.values());
                return list;
            } else {
                return delegateAdapter.read(in);
            }
        }
        
    }
    
}
