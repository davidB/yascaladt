package net_alchim31_yascaladt_editor;

import java.util.ResourceBundle;

import net_alchim31_yascaladt.Messages;

import org.eclipse.ui.editors.text.TextEditor;

public class SimpleScalaEditor extends TextEditor {

    public SimpleScalaEditor() {
        super();
        setSourceViewerConfiguration(new ScalaViewerConfiguration());
        setDocumentProvider(new ScalaDocumentProvider());
        setKeyBindingScopes( new String[] { "net.alchim31.yascaladt.Scope" } );
    }
    public void dispose() {
        //colorManager.dispose();
        super.dispose();
    }

    protected ResourceBundle getBundle() {
        //return JavaEditorMessages.getBundleForConstructedKeys();
        return Messages.getResourceBundle();
    }
//    /*
//     * @see AbstractTextEditor#createActions()
//     */
//    protected void createActions() {
//
//        super.createActions();
//
//        IAction action= getAction(ITextEditorActionConstants.CONTENT_ASSIST_CONTEXT_INFORMATION);
//        if (action != null) {
//            PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.PARAMETER_HINTS_ACTION);
//            action.setText(getBundle().getString("ContentAssistContextInformation.label")); //$NON-NLS-1$
//            action.setToolTipText(getBundle().getString("ContentAssistContextInformation.tooltip")); //$NON-NLS-1$
//            action.setDescription(getBundle().getString("ContentAssistContextInformation.description")); //$NON-NLS-1$
//        }
//
//        action= new TextOperationAction(getBundle(), "Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
//        action.setActionDefinitionId(IJavaEditorActionDefinitionIds.UNCOMMENT);
//        setAction("Uncomment", action); //$NON-NLS-1$
//        markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.UNCOMMENT_ACTION);
//
//        action= new ToggleCommentAction(getBundle(), "ToggleComment.", this); //$NON-NLS-1$
//        action.setActionDefinitionId(IJavaEditorActionDefinitionIds.TOGGLE_COMMENT);
//        setAction("ToggleComment", action); //$NON-NLS-1$
//        markAsStateDependentAction("ToggleComment", true); //$NON-NLS-1$
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
//        configureToggleCommentAction();
//
//        action= new AddBlockCommentAction(getBundle(), "AddBlockComment.", this);  //$NON-NLS-1$
//        action.setActionDefinitionId(IJavaEditorActionDefinitionIds.ADD_BLOCK_COMMENT);
//        setAction("AddBlockComment", action); //$NON-NLS-1$
//        markAsStateDependentAction("AddBlockComment", true); //$NON-NLS-1$
//        markAsSelectionDependentAction("AddBlockComment", true); //$NON-NLS-1$
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.ADD_BLOCK_COMMENT_ACTION);
//
//        action= new RemoveBlockCommentAction(getBundle(), "RemoveBlockComment.", this);  //$NON-NLS-1$
//        action.setActionDefinitionId(IJavaEditorActionDefinitionIds.REMOVE_BLOCK_COMMENT);
//        setAction("RemoveBlockComment", action); //$NON-NLS-1$
//        markAsStateDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
//        markAsSelectionDependentAction("RemoveBlockComment", true); //$NON-NLS-1$
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.REMOVE_BLOCK_COMMENT_ACTION);
//
//        action= new IndentAction(getBundle(), "Indent.", this, false); //$NON-NLS-1$
//        action.setActionDefinitionId(IJavaEditorActionDefinitionIds.INDENT);
//        setAction("Indent", action); //$NON-NLS-1$
//        markAsStateDependentAction("Indent", true); //$NON-NLS-1$
//        markAsSelectionDependentAction("Indent", true); //$NON-NLS-1$
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.INDENT_ACTION);
//
//        action= new IndentAction(getBundle(), "Indent.", this, true); //$NON-NLS-1$
//        setAction("IndentOnTab", action); //$NON-NLS-1$
//        markAsStateDependentAction("IndentOnTab", true); //$NON-NLS-1$
//        markAsSelectionDependentAction("IndentOnTab", true); //$NON-NLS-1$
//
//
//        if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_TAB)) {
//            // don't replace Shift Right - have to make sure their enablement is mutually exclusive
////			removeActionActivationCode(ITextEditorActionConstants.SHIFT_RIGHT);
//            setActionActivationCode("IndentOnTab", '\t', -1, SWT.NONE); //$NON-NLS-1$
//        }
//
////		fGenerateActionGroup= new GenerateActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
////		fRefactorActionGroup= new RefactorActionGroup(this, ITextEditorActionConstants.GROUP_EDIT, false);
////		ActionGroup surroundWith= new SurroundWithActionGroup(this, ITextEditorActionConstants.GROUP_EDIT);
////
////		fActionGroups.addGroup(surroundWith);
////		fActionGroups.addGroup(fRefactorActionGroup);
////		fActionGroups.addGroup(fGenerateActionGroup);
//
////		// We have to keep the context menu group separate to have better control over positioning
////		fContextMenuGroup= new CompositeActionGroup(new ActionGroup[] {
////			fGenerateActionGroup,
////			fRefactorActionGroup,
////			surroundWith,
////			new LocalHistoryActionGroup(this, ITextEditorActionConstants.GROUP_EDIT)});
////
////		fCorrectionCommands= new CorrectionCommandInstaller(); // allow shortcuts for quick fix/assist
////		fCorrectionCommands.registerCommands(this);
//    }
//
//    /**
//     * Configures the toggle comment action
//     *
//     * @since 3.0
//     */
//    private void configureToggleCommentAction() {
//        IAction action= getAction("ToggleComment"); //$NON-NLS-1$
//        if (action instanceof ToggleCommentAction) {
//            ISourceViewer sourceViewer= getSourceViewer();
//            SourceViewerConfiguration configuration= getSourceViewerConfiguration();
//            ((ToggleCommentAction)action).configure(sourceViewer, configuration);
//        }
//    }
//
//    /*
//     * @see org.eclipse.ui.texteditor.AbstractTextEditor#installTabsToSpacesConverter()
//     * @since 3.3
//     */
//    protected void installTabsToSpacesConverter() {
//        ISourceViewer sourceViewer= getSourceViewer();
//        SourceViewerConfiguration config= getSourceViewerConfiguration();
//        if (config != null && sourceViewer instanceof ITextViewerExtension7) {
//            int tabWidth= config.getTabWidth(sourceViewer);
//            TabsToSpacesConverter tabToSpacesConverter= new TabsToSpacesConverter();
//            tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);
//            IDocumentProvider provider= getDocumentProvider();
//            if (provider instanceof ICompilationUnitDocumentProvider) {
//                ICompilationUnitDocumentProvider cup= (ICompilationUnitDocumentProvider) provider;
//                tabToSpacesConverter.setLineTracker(cup.createLineTracker(getEditorInput()));
//            } else
//                tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
//            ((ITextViewerExtension7)sourceViewer).setTabsToSpacesConverter(tabToSpacesConverter);
//            updateIndentPrefixes();
//        }
//    }
//
//    /*
//     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#isTabsToSpacesConversionEnabled()
//     * @since 3.3
//     */
//    protected boolean isTabsToSpacesConversionEnabled() {
//        return true;
//    }


//	/**
//	 * Returns the refactor action group.
//	 *
//	 * @return the refactor action group, or <code>null</code> if there is none
//	 * @since 3.5
//	 */
//	protected RefactorActionGroup getRefactorActionGroup() {
//		return fRefactorActionGroup;
//	}
//
//	/**
//	 * Returns the generate action group.
//	 *
//	 * @return the generate action group, or <code>null</code> if there is none
//	 * @since 3.5
//	 */
//	protected GenerateActionGroup getGenerateActionGroup() {
//		return fGenerateActionGroup;
//	}
}
