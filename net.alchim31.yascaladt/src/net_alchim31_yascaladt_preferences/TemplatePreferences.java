package net_alchim31_yascaladt_preferences;

import net_alchim31_yascaladt.Activator;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class TemplatePreferences extends TemplatePreferencePage implements IWorkbenchPreferencePage {
    public TemplatePreferences() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setTemplateStore(Activator.getDefault().getTemplateStore());
        setContextTypeRegistry(Activator.getDefault().getContextTypeRegistry());
    }

    public boolean performOk() {
        boolean ok = super.performOk();
        return ok;
    }

    protected boolean isShowFormatterSetting() {
        return false;
    }
//
//    protected static class EditTemplateDialog extends TemplatePreferencePage.EditTemplateDialog {
//
//        public EditTemplateDialog(Shell arg0, Template arg1, boolean arg2, boolean arg3, ContextTypeRegistry arg4) {
//            super(arg0, arg1, arg2, arg3, arg4);
//        }
//
//        protected SourceViewer createViewer(Composite parent) {
//
//            SourceViewer viewer= new SourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
//            SourceViewerConfiguration configuration = new ScalaViewerConfiguration();
//            viewer.configure(configuration);
//
//            return viewer;
//        }
//
//    }
//
//    public TemplatePreferences(){
//        setPreferenceStore(Activator.getDefault().getPreferenceStore());
//        setTemplateStore(Activator.getDefault().getTemplateStore());
//        setContextTypeRegistry(Activator.getDefault().getContextTypeRegistry());
//
//    }
//
//    protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
//        EditTemplateDialog dialog= new EditTemplateDialog(getShell(), template, edit, isNameModifiable, getContextTypeRegistry());
//        if (dialog.open() == Window.OK) {
//            return dialog.getTemplate();
//        }
//        return null;
//    }
//
//    protected boolean isShowFormatterSetting() {
//        return false;
//    }


}
