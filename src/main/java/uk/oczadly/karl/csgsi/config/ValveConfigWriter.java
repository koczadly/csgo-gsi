package uk.oczadly.karl.csgsi.config;

import uk.oczadly.karl.csgsi.internal.Util;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Helper class for writing Valve configuration files (JSON-like).
 */
// TODO: discard empty objects without writing
public class ValveConfigWriter implements Closeable, Flushable {
    
    private final Writer out;
    private final String indentChar, newLineChar;
    private final Stack<ObjectState> stack = new Stack<>();
    
    public ValveConfigWriter(Writer out) {
        this(out, 4, System.lineSeparator());
    }
    
    public ValveConfigWriter(Writer out, int indentSize, String newLine) {
        this.out = out;
        this.indentChar = Util.repeatChar(' ', indentSize);
        this.newLineChar = newLine;
        this.stack.add(new ObjectState());
    }
    
    
    public ValveConfigWriter beginObject() throws IOException {
        ObjectState os = peekStack();
        flushObject(os);
        if (os.deferredKey == null)
            throw new IllegalStateException("A key must be set before writing an object.");
        // Write opening key
        writeIndent();
        writeKey(os.deferredKey, 0);
        write("{").write(newLineChar);
        os.deferredKey = null;
        // Create new object on stack
        stack.add(new ObjectState());
        return this;
    }
    
    public ValveConfigWriter endObject() throws IOException {
        ObjectState os = peekStack();
        if (os.deferredKey != null)
            throw new IllegalStateException("Object has an unfinished key.");
        flushObject(os); // Flush ending object
        stack.pop(); // Remove object from stack
        // Write closing brace
        writeIndent();
        write("}").write(newLineChar);
        return this;
    }
    
    public ValveConfigWriter key(String key) {
        ObjectState os = peekStack();
        if (key == null) key = "";
        if (os.deferredKey != null)
            throw new IllegalStateException("A key has already been assigned and is waiting for a value.");
        if (os.deferredKV.containsKey(key))
            throw new IllegalStateException("Key \"" + key + "\" already exists.");
        os.deferredKey = key;
        return this;
    }
    
    public ValveConfigWriter value(Object val) {
        ObjectState os = peekStack();
        if (os.deferredKey == null)
            throw new IllegalStateException("A key must be set before writing a value.");
        if (val != null) // Ignore null values
            os.deferredKV.put(os.deferredKey, val.toString());
        os.deferredKey = null;
        return this;
    }
    
    @Override
    public void close() throws IOException {
        ObjectState os = peekStack();
        flushObject(os);
        if (stack.size() > 1)
            throw new IllegalStateException("Unclosed object(s).");
        if (os.deferredKey != null)
            throw new IllegalStateException("Dangling key not followed by an object or value.");
        stack.empty();
        out.close();
    }
    
    @Override
    public void flush() throws IOException {
        flushObject(peekStack());
        out.flush();
    }
    
    
    private void writeKeyValues(Map<String, String> kvs) throws IOException {
        // Determine max key length (for padding)
        int maxLen = kvs.keySet().stream()
                .mapToInt(String::length)
                .max().orElse(0);
        // Write values
        for (Map.Entry<String, String> kv : kvs.entrySet()) {
            writeIndent();
            writeKey(kv.getKey(), maxLen);
            writeValue(kv.getValue());
        }
    }
    
    private void writeIndent() throws IOException {
        for (int i = 1; i < stack.size(); i++)
            write(this.indentChar);
    }
    
    private void writeKey(String key, int maxKeyLen) throws IOException {
        writeField(key).write(" ");
        
        // Spacers
        int padding = Math.max(0, maxKeyLen - key.length());
        for (int i = 0; i < padding; i++)
            write(" ");
    }
    
    private void writeValue(String value) throws IOException {
        writeField(value).write(newLineChar);
    }
    
    private void flushObject(ObjectState os) throws IOException {
        if (!os.deferredKV.isEmpty()) {
            writeKeyValues(os.deferredKV);
            os.deferredKV.clear();
        }
    }
    
    private ObjectState peekStack() {
        if (stack.isEmpty())
            throw new IllegalStateException("ConfigWriter is closed.");
        return stack.peek();
    }
    
    private ValveConfigWriter write(String str) throws IOException {
        out.write(str);
        return this;
    }
    
    private ValveConfigWriter writeField(String str) throws IOException {
        return write("\"")
                .write(str.replace("\\", "\\\\").replace("\"", "\\\""))
                .write("\"");
    }
    
    
    private static class ObjectState {
        private final Map<String, String> deferredKV = new LinkedHashMap<>();
        private String deferredKey;
    }
    
}
