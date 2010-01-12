package net_alchim31_yascaladt_editor;

import net_alchim31_yascaladt.Activator;
import net_alchim31_yascaladt_preferences.PreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class AutoInsertPair implements IAutoEditStrategy {

    private boolean _closeString;
    private boolean _closeBraces;
    private boolean _closeComments;
    private boolean _deletePair;

    public AutoInsertPair(IPreferenceStore prefs) {
        _closeString = prefs.getBoolean(PreferenceConstants.EDITOR_CLOSE_STRINGS.name());
        _closeBraces = prefs.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACES.name());
        _closeComments = prefs.getBoolean(PreferenceConstants.EDITOR_CLOSE_COMMENTS.name());
        _deletePair = prefs.getBoolean(PreferenceConstants.EDITOR_DELETE_PAIR.name());
        prefs.addPropertyChangeListener(new IPropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                switch (PreferenceConstants.valueOf(evt.getProperty())) {
                    case EDITOR_CLOSE_STRINGS :
                        _closeString = (Boolean)evt.getNewValue();
                        break;
                    case EDITOR_CLOSE_BRACES :
                        _closeBraces = (Boolean)evt.getNewValue();
                        break;
                    case EDITOR_CLOSE_COMMENTS :
                        _closeComments = (Boolean)evt.getNewValue();
                        break;
                    case EDITOR_DELETE_PAIR :
                        _deletePair = (Boolean)evt.getNewValue();
                        break;
                }
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        });
    }

    public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
        if (cmd.offset == -1 || doc.getLength() == 0 || cmd.text == null) return;
//        if (cmd.length == 0) {
//            int nlIndex = TextUtilities.endsWith(doc.getLegalLineDelimiters(), cmd.text);
//            if (nlIndex != -1) autoIndentNewLine(doc, cmd, nlIndex);
//        }
        if (cmd.text.length() == 1) closePair(doc, cmd);
        if (_deletePair && cmd.length == 1 && cmd.text.length() == 0) deletePair(doc, cmd);
    }

    private void deletePair(IDocument doc, DocumentCommand cmd) {
        try {
            char ch = doc.getChar(cmd.offset);
            if (TextHelper.isOpenPair(ch)) {
                char next = doc.getChar(cmd.offset + 1);
                if (next == TextHelper.getMathingPair(ch)) {
                    if (TextHelper.getOpenPairCount(doc, ch) == 0) {
                        // delete next
                        cmd.doit = false;
                        cmd.addCommand(cmd.offset + 1, 1, "", null);
                    }
                }
            }
        } catch (BadLocationException e) {
            Activator.getDefault().error("Error deleting closing pair", e);
        }
    }


    protected void closePair(IDocument doc, DocumentCommand cmd) {
        try {
            char c = cmd.text.charAt(0);
            boolean closeString = _closeString && (c == '"' || c == '\'');
            if (closeString || _closeBraces && TextHelper.isOpenBrace(c)) {
                if (TextHelper.getOpenPairCount(doc, c) == 0) {
                    if (!closeString || doc.getChar(cmd.offset) != c) {
                        cmd.shiftsCaret = false;
                        cmd.caretOffset = cmd.offset + 1;
                        cmd.text += TextHelper.getMathingPair(c);
                    } else if (closeString && doc.getChar(cmd.offset-1) == c) {
                        cmd.text = "";
                        cmd.caretOffset = cmd.offset + 1;
                    }
                }
            } else if (_closeComments && '/' == doc.getChar(cmd.offset-1) && '*' == c ) {
                cmd.shiftsCaret = false;
                cmd.caretOffset = cmd.offset + 1;
                cmd.text += "*/";
            } else if (_closeBraces && (c == '}' || c == ')' || c == ']')) {
                char o = TextHelper.getMathingPair(c);
                if (TextHelper.getOpenPairCount(doc, o) == 0) {
                    if (doc.getChar(cmd.offset)==c && doc.getChar(cmd.offset-1)==o) {
                        cmd.text = "";
                        cmd.caretOffset = cmd.offset +1;
                    }
                }
            }
        } catch (BadLocationException e) {
            Activator.getDefault().warn("Error closing pair", e);
        }
    }
}