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
    public void test() {
        TestContainer vals = gson.fromJson("{\"standard\": \"B\", \"named\": \"BB\", \"deserializer\": \"VAL_B\"" +
                ", \"notFound\": \"D\", \"list\": [\"A\",\"B\",\"D\"]}", TestContainer.class);
    
        assertSame(Enum.B, vals.standard.val());
        assertEquals("B", vals.standard.stringVal());
        
        assertSame(EnumWithNames.B, vals.named.val());
        assertEquals("BB", vals.named.stringVal());
    
        assertSame(EnumWithDeserializer.B, vals.deserializer.val());
        assertEquals("VAL_B", vals.deserializer.stringVal());
    
        assertSame(null, vals.notFound.val());
        assertEquals("D", vals.notFound.stringVal());
        
        assertSame(3, vals.list.size());
        assertSame(Enum.A, vals.list.get(0).val());
        assertSame(Enum.B, vals.list.get(1).val());
        assertNull(vals.list.get(2).val());
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