package ca.discotek.deepdive.security.gui.config.exclusions;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.discotek.common.swing.ButtonPanel;
import ca.discotek.common.swing.DialogWrapper;
import ca.discotek.deepdive.security.Path;

public class ExclusionEditor extends JPanel {

    ProjectTree projectTree;
    
    public ExclusionEditor() {
        buildGui();
    }
    
    void buildGui() {
        setLayout(new BorderLayout());
        projectTree = new ProjectTree();
        add(projectTree, BorderLayout.CENTER);
    }
    
    public void addDeploymentUnit(String file) throws IOException {
        addDeploymentUnit(new File(file));
    }
    
    public void addDeploymentUnit(File file) throws IOException {
        projectTree.addDeployment(file);
    }
    
    public String[] getSelectedPaths() {
        return projectTree.getSelectedPaths();
    }
    
    public void setSelectedPaths(String paths[]) {
        projectTree.setSelectedPaths(paths);
    }
    
    public static void main(String[] args) throws IOException {
        JFrame f = new JFrame();
        f.setSize(800, 600);
        f.setVisible(true);

        
        ExclusionEditor editor = new ExclusionEditor();
        editor.addDeploymentUnit("c:/temp/discotek.website2.ear");
        editor.setSelectedPaths(new String[] {
            "c:\\temp\\discotek.website2.ear",
            "c:\\temp\\discotek.website2.ear->discotek.website.war->WEB-INF/lib/discotek.modifly-1.0.jar"
        });
        
        boolean canceled = DialogWrapper.showDialogWrapper(f, "Deployment Selector", editor);
        if (!canceled) {
            String paths[] = editor.getSelectedPaths();
            for (int i=0; i<paths.length; i++)
                System.out.println(paths[i]);
        }
    }
}
