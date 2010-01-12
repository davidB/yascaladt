package net_alchim31_yascaladt_preferences;

import net_alchim31_yascaladt.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();

        prefs.setDefault(PreferenceConstants.FORMATTER_INDENTATION_SIZE.name(), 2);
        prefs.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS.name(), true);
        prefs.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACES.name(), true);
        prefs.setDefault(PreferenceConstants.EDITOR_CLOSE_COMMENTS.name(), true);
        prefs.setDefault(PreferenceConstants.EDITOR_DELETE_PAIR.name(), true);
    }

}
