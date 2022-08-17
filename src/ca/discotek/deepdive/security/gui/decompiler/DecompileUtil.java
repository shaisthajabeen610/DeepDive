package ca.discotek.deepdive.security.gui.decompiler;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.benf.cfr.reader.Main;

import ca.discotek.deepdive.grep.AsmUtil;


public class DecompileUtil {

    static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"), "_decompiled_");
    
    public static String decomile(File classFile) throws IOException {
        Main.main(new String[] {classFile.getAbsolutePath(), "--outputdir", TMPDIR.getAbsolutePath(), "--silent", "true", "--clobber", "true", "--comments", "false"});

        FileInputStream fis2 = null;
        FileInputStream fis = null;

        fis = new FileInputStream(classFile);

        File file = new File(TMPDIR, AsmUtil.getClassName(fis) + ".java");
        file.deleteOnExit();
        
        
        fis2 = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis2);
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        StringBuilder buffer = new StringBuilder();
        boolean inComment = false;
        while ( (line = br.readLine()) != null) {
            if (line.trim().startsWith("/*"))
                inComment = true;
            else if (line.trim().startsWith("*/")) {
                inComment = false;
                continue;
            }
            
            if (inComment)
                continue;
            else 
                buffer.append(line + "\n");
        }
        
        return buffer.toString();

    }
}
