package ca.discotek.deepdive.security.gui.editor;

import ca.discotek.deepdive.grep.classmatcher.AbstractInstructionDescriptor;

public interface InstructionDescriptionEditor {

    public AbstractInstructionDescriptor getDescriptor();
    public void setDescriptor(AbstractInstructionDescriptor d);
    public void savePreferences();
    public void loadPreferences();
}
