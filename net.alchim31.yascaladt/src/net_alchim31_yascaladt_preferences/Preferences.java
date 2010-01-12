package net_alchim31_yascaladt_preferences;

import net_alchim31_yascaladt.Activator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Preferences
    extends FieldEditorPreferencePage
    implements IWorkbenchPreferencePage {

    public Preferences() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("YaScalaDT");
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors() {
        addField(new IntegerFieldEditor(PreferenceConstants.FORMATTER_INDENTATION_SIZE.name(), "Indentation size (spaces)", getFieldEditorParent(), 1 ) );
        addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_CLOSE_STRINGS.name(), "Close \"\", '' ", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_CLOSE_BRACES.name(), "Close {}, (), []", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_CLOSE_COMMENTS.name(), "Close /*..*/", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.EDITOR_DELETE_PAIR.name(), "Delete Pair", getFieldEditorParent()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

}