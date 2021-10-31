package uk.oczadly.karl.csgsi.state.components;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.*;

public class EnumValueTest {

    private static final Gson gson = new Gson();
    
    
    @Test
    public void testDeserialize() {
        TestContainer vals = gson.fromJson("{\"standard\": \"B\", \"named\": \"BB\", \"deserializer\": \"VAL_B\"" +
                ", \"notFound\": \"D\", \"list\": [\"A\",\"B\",\"D\"]}", TestContainer.class);
        
        assertSame(Enum.B, vals.standard.enumVal());
        assertEquals("B", vals.standard.rawVal());
        
        assertSame(EnumWithNames.B, vals.named.enumVal());
        assertEquals("BB", vals.named.rawVal());
        
        assertSame(EnumWithDeserializer.B, vals.deserializer.enumVal());
        assertEquals("VAL_B", vals.deserializer.rawVal());
        
        assertSame(null, vals.notFound.enumVal());
        assertEquals("D", vals.notFound.rawVal());
        
        assertSame(3, vals.list.size());
        assertSame(Enum.A, vals.list.get(0).enumVal());
        assertSame(Enum.B, vals.list.get(1).enumVal());
        assertNull(vals.list.get(2).enumVal());
    }
    
    @Test
    public void testOf() {
        // Valid
        EnumValue<EnumWithNames> val1 = EnumValue.of("BB", EnumWithNames.class, gson);
        assertNotNull(val1);
        assertEquals(EnumWithNames.B, val1.enumVal());
        assertEquals("BB", val1.rawVal());
    
        // Invalid
        EnumValue<EnumWithNames> val2 = EnumValue.of("DD", EnumWithNames.class, gson);
        assertNotNull(val2);
        assertNull(val2.enumVal());
        assertEquals("DD", val2.rawVal());
    }
    
    
    static class TestContainer {
        EnumValue<Enum> standard;
        EnumValue<EnumWithNames> named;
        EnumValue<EnumWithDeserializer> deserializer;
        EnumValue<Enum> notFound;
        List<EnumValue<Enum>> list;
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