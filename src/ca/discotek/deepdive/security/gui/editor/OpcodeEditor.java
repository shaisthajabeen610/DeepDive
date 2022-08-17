package ca.discotek.deepdive.security.gui.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.discotek.deepdive.grep.classmatcher.Modifier;
import ca.discotek.deepdive.grep.classmatcher.Opcode;

public class OpcodeEditor extends JPanel {

    public static Opcode FieldInstructionOpcodes[] = 
        {Opcode.GETFIELD, Opcode.GETSTATIC, Opcode.PUTFIELD, Opcode.PUTSTATIC};

    public static Opcode MethodInstructionOpcodes[] = 
        {Opcode.INVOKEINTERFACE, Opcode.INVOKESPECIAL, Opcode.INVOKESTATIC, Opcode.INVOKEVIRTUAL};
    
    public static Opcode JumpInstructionOpcodes[] =
        {Opcode.IFEQ, Opcode.IFNE, Opcode.IFLT, Opcode.IFGE, Opcode.IFGT, Opcode.IFLE, Opcode.IF_ICMPEQ, Opcode.IF_ICMPNE, Opcode.IF_ICMPLT, Opcode.IF_ICMPGE, Opcode.IF_ICMPGT, Opcode.IF_ICMPLE, Opcode.IF_ACMPEQ, Opcode.IF_ACMPNE, Opcode.GOTO, Opcode.JSR, Opcode.IFNULL, Opcode.IFNONNULL};
    
    public static Opcode InstructionOpcodes[] =
        {Opcode.NOP, Opcode.ACONST_NULL, Opcode.ICONST_M1, Opcode.ICONST_0, Opcode.ICONST_1, Opcode.ICONST_2, Opcode.ICONST_3, Opcode.ICONST_4, Opcode.ICONST_5, Opcode.LCONST_0, Opcode.LCONST_1, Opcode.FCONST_0, Opcode.FCONST_1, Opcode.FCONST_2, Opcode.DCONST_0, Opcode.DCONST_1, Opcode.IALOAD, Opcode.LALOAD, Opcode.FALOAD, Opcode.DALOAD, Opcode.AALOAD, Opcode.BALOAD, Opcode.CALOAD, Opcode.SALOAD, Opcode.IASTORE, Opcode.LASTORE, Opcode.FASTORE, Opcode.DASTORE, Opcode.AASTORE, Opcode.BASTORE, Opcode.CASTORE, Opcode.SASTORE, Opcode.POP, Opcode.POP2, Opcode.DUP, Opcode.DUP_X1, Opcode.DUP_X2, Opcode.DUP2, Opcode.DUP2_X1, Opcode.DUP2_X2, Opcode.SWAP, Opcode.IADD, Opcode.LADD, Opcode.FADD, Opcode.DADD, Opcode.ISUB, Opcode.LSUB, Opcode.FSUB, Opcode.DSUB, Opcode.IMUL, Opcode.LMUL, Opcode.FMUL, Opcode.DMUL, Opcode.IDIV, Opcode.LDIV, Opcode.FDIV, Opcode.DDIV, Opcode.IREM, Opcode.LREM, Opcode.FREM, Opcode.DREM, Opcode.INEG, Opcode.LNEG, Opcode.FNEG, Opcode.DNEG, Opcode.ISHL, Opcode.LSHL, Opcode.ISHR, Opcode.LSHR, Opcode.IUSHR, Opcode.LUSHR, Opcode.IAND, Opcode.LAND, Opcode.IOR, Opcode.LOR, Opcode.IXOR, Opcode.LXOR, Opcode.I2L, Opcode.I2F, Opcode.I2D, Opcode.L2I, Opcode.L2F, Opcode.L2D, Opcode.F2I, Opcode.F2L, Opcode.F2D, Opcode.D2I, Opcode.D2L, Opcode.D2F, Opcode.I2B, Opcode.I2C, Opcode.I2S, Opcode.LCMP, Opcode.FCMPL, Opcode.FCMPG, Opcode.DCMPL, Opcode.DCMPG, Opcode.IRETURN, Opcode.LRETURN, Opcode.FRETURN, Opcode.DRETURN, Opcode.ARETURN, Opcode.RETURN, Opcode.ARRAYLENGTH, Opcode.ATHROW, Opcode.MONITORENTER, Opcode.MONITOREXIT};
    
    public static Opcode IntInstructionOpcodes[] =
        {Opcode.BIPUSH, Opcode.SIPUSH, Opcode.NEWARRAY};
    
    public static Opcode VarInstructionOpcodes[] =
        {Opcode.ILOAD, Opcode.LLOAD, Opcode.FLOAD, Opcode.DLOAD, Opcode.ALOAD, Opcode.ISTORE, Opcode.LSTORE, Opcode.FSTORE, Opcode.DSTORE, Opcode.ASTORE, Opcode.RET};

    public static Opcode TypeInstructionOpcodes[] =
        {Opcode.NEW, Opcode.ANEWARRAY, Opcode.CHECKCAST, Opcode.INSTANCEOF};
    
    final Opcode opcodes[];
    JComboBox opcodeField;
    
    public OpcodeEditor(Opcode opcodes[]) {
        this.opcodes = opcodes;
        buildGui();
    }
    
    void buildGui() {
        setLayout(new GridBagLayout());
        
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = labelGbc.gridy = 0;
        labelGbc.weightx = labelGbc.weighty = 0;
        labelGbc.fill = GridBagConstraints.NONE;
        labelGbc.anchor = GridBagConstraints.EAST;
        labelGbc.insets = new Insets(2, 2, 2, 2);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = 0;
        fieldGbc.weightx = 1;
        fieldGbc.weighty = 0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.anchor = GridBagConstraints.WEST;
        fieldGbc.insets = new Insets(2, 2, 2, 2);
        
        add(new JLabel("Instruction"), labelGbc);

        opcodeField = new JComboBox(opcodes);
        add(opcodeField, fieldGbc);
    }
    
    public Opcode getOpcode() {
        return (Opcode) opcodeField.getSelectedItem();
    }
    
    public void setOpcode(Opcode opcode) {
        opcodeField.setSelectedItem(opcode);
    }

    public void setEnabled(boolean enabled) {
        opcodeField.setEnabled(enabled);
    }
}
