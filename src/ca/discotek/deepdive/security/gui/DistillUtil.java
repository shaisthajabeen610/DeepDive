package ca.discotek.deepdive.security.gui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DistillUtil {

    public static final String NEW_LINE = System.getProperty("line.separator");
    
    public static String getDistilledManifest(Manifest manifest) {
        
        Attributes attributes = manifest.getMainAttributes();
        
        Set<Object> set = attributes.keySet();
        Iterator<Object> it = set.iterator();
        int keyLength, maxLength = -1;
        while (it.hasNext()) {
            keyLength = it.next().toString().length();
            if (keyLength > maxLength)
                maxLength = keyLength;
        }
        
        StringBuilder buffer = new StringBuilder();
        
        it = set.iterator();
        Object key;
        String keyString;
        while (it.hasNext()) {
            key = it.next();
            keyString = key.toString();
            keyLength = keyString.length();
            buffer.append(padStart(keyString, maxLength - keyLength));
            buffer.append(": ");
            buffer.append(attributes.get(key));
            buffer.append(NEW_LINE);
        }
        
        return buffer.toString();
    }
    
    static String padStart(String s, int padSize) {
        StringBuilder buffer = new StringBuilder();
        
        char chars[] = new char[padSize];
        Arrays.fill(chars, ' ');
        buffer.append(chars);
        buffer.append(s);
        
        return buffer.toString();
    }
}
