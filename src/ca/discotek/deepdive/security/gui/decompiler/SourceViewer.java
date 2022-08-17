package ca.discotek.deepdive.security.gui.decompiler;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.benf.cfr.reader.Main;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.AnalysisType;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.DumperFactory;
import org.benf.cfr.reader.util.output.DumperFactoryImpl;
import org.benf.cfr.reader.util.output.IllegalIdentifierDump;
import org.benf.cfr.reader.util.output.SummaryDumper;

//import org.jd.gui.util.decompiler.GuiPreferences;
//import org.jd.gui.util.decompiler.PlainTextPrinter;
//import org.jd.gui.view.component.TextPage;

import ca.discotek.common.swing.ButtonPanel;
import ca.discotek.common.swing.CloseableTab;
import ca.discotek.deepdive.grep.AsmUtil;
import ca.discotek.deepdive.io.IOUtil;
import ca.discotek.rebundled.org.objectweb.asm.ClassReader;
import ca.discotek.rebundled.org.objectweb.asm.tree.ClassNode;
//import jd.core.loader.Loader;
//import jd.core.loader.LoaderException;
//import jd.core.preferences.Preferences;
//import jd.core.process.DecompilerImpl;

public class SourceViewer extends JFrame {

    JTabbedPane tabber;
//    DecompilerImpl decompiler = new DecompilerImpl();
    
    public SourceViewer() {
        super("Decompiled Class Viewer");
        buildGui();
    }
    
    void buildGui() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        tabber = new JTabbedPane();
        add(tabber, BorderLayout.CENTER);
        
        ButtonPanel panel = new ButtonPanel();
        JButton button = new JButton("Clear All");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tabber.removeAll();
            }
        });
        panel.addButton(button);
        
        button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SourceViewer.this.setVisible(false);
            }
        });
        panel.addButton(button);
        
        add(panel, BorderLayout.SOUTH);
    }
    
//    public void addClass(File classFile) throws IOException, LoaderException {
//        Preferences preferences = new Preferences();
//        ClassFileLoader loader = new ClassFileLoader(classFile);
//        PlainTextPrinter printer = new PlainTextPrinter();
//        GuiPreferences guiPreferences = new GuiPreferences();
//        guiPreferences.setShowLineNumbers(false);
//        printer.setPreferences(guiPreferences);
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        PrintStream ps = new PrintStream(bos);
//        printer.setPrintStream(ps);
//        decompiler.decompile(preferences, loader, printer, null);
//        ps.close();
//        
//        TextPage tp = new TextPage();
//        tp.setText(new String(bos.toByteArray()));
//        
//        String tabText = classFile.getName();
//        tabber.addTab(tabText, tp);
//        int index = tabber.getTabCount()-1;
//        tabber.setTabComponentAt(index, new CloseableTab(tabText, tabber, tp));
//        tabber.setSelectedIndex(index);
//    }

    
    
    
    public void addClass(File classFile) throws IOException{
        String source = DecompileUtil.decomile(classFile);
        
        JEditorPane editor = new JEditorPane();
        JScrollPane scroller = new JScrollPane(editor);
        editor.setFont(new Font("Courier New", Font.PLAIN, 14));
        editor.setContentType("text/plain");
        editor.setText(source);
        editor.setCaretPosition(0);
        editor.setEditable(false);
        String tabText = classFile.getName();
        tabber.addTab(tabText, scroller);
        int index = tabber.getTabCount()-1;
        tabber.setTabComponentAt(index, new CloseableTab(tabText, tabber, scroller));
        tabber.setSelectedIndex(index);

    }
    
//    class ClassFileLoader implements Loader {
//
//        final byte bytes[];
//        
//        public ClassFileLoader(File file) throws IOException {
//            bytes = IOUtil.readFile(file);
//        }
//        
//        @Override
//        public boolean canLoad(String arg0) {
//            return true;
//        }
//
//        @Override
//        public DataInputStream load(String arg0) throws LoaderException {
//            return new DataInputStream(new ByteArrayInputStream(bytes));
//        }
//    }
    
    
}
