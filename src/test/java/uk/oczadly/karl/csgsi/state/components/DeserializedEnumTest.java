package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

public class DeserializedEnumTest {

    private static final Gson gson = new Gson();
    
    
    @Test
    public void test() {
        TestContainer vals = gson.fromJson("{\"standard\": \"B\", \"named\": \"BB\", \"deserializer\": \"VAL_B\"" +
                ", \"notFound\": \"D\", \"list\": [\"A\",\"B\",\"D\"]}", TestContainer.class);
    
        assertSame(Enum.B, vals.standard.getEnum());
        assertEquals("B", vals.standard.getRawString());
        
        assertSame(EnumWithNames.B, vals.named.getEnum());
        assertEquals("BB", vals.named.getRawString());
    
        assertSame(EnumWithDeserializer.B, vals.deserializer.getEnum());
        assertEquals("VAL_B", vals.deserializer.getRawString());
    
        assertSame(null, vals.notFound.getEnum());
        assertEquals("D", vals.notFound.getRawString());
        
        assertSame(3, vals.list.size());
        assertSame(Enum.A, vals.list.get(0).getEnum());
        assertSame(Enum.B, vals.list.get(1).getEnum());
        assertNull(vals.list.get(2).getEnum());
    }
    
    
    static class TestContainer {
        DeserializedEnum<Enum> standard;
        DeserializedEnum<EnumWithNames> named;
        DeserializedEnum<EnumWithDeserializer> deserializer;
        DeserializedEnum<Enum> notFound;
        List<DeserializedEnum<Enum>> list;
    }
    
    
    enum Enum {
        A, B, C
    }
    
    enum EnumWithNames {
        @SerializedName("AA") A,
        @SerializedName("BB") B,
        @SerializedName("CC") C
    }
    
    @JsonAdapter(Deserializer.class)
    enum EnumWithDeserializer {
        A,
        B,
        C
    }
    
    static class Deserializer implements JsonDeserializer<EnumWithDeserializer> {
        @Override
        public EnumWithDeserializer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return json.getAsString().equals("VAL_B") ? EnumWithDeserializer.B : null;
        }
    }

}