package net_alchim31_yascaladt_editor;

import java.util.Arrays;

import net_alchim31_yascaladt.Activator;
import net_alchim31_yascaladt_preferences.PreferenceConstants;
import net_alchim31_yascaladt_template.MyTemplateCompletionProcessor;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TabsToSpacesConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class ScalaViewerConfiguration extends TextSourceViewerConfiguration {
    private Scanner4Code _scanner;
    private final IColorManager _colorManager;

    public ScalaViewerConfiguration(IColorManager colorManager, IPreferenceStore jdtPreferenceStore) {
        _colorManager = colorManager;
        _scanner = new Scanner4Code(_colorManager, jdtPreferenceStore);
    }

    public ScalaViewerConfiguration() {
        this(JavaUI.getColorManager(), org.eclipse.jdt.ui.PreferenceConstants.getPreferenceStore());
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            Scanner4Partition.MULTILINE_COMMENT,
            Scanner4Partition.SINGLELINE_COMMENT,
            Scanner4Partition.STRING
        };
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        NonRuleBasedDamagerRepairer ndr0 = new NonRuleBasedDamagerRepairer(new TextAttribute( _colorManager.getColor(IJavaColorConstants.JAVADOC_DEFAULT)));
        reconciler.setDamager(ndr0, Scanner4Partition.MULTILINE_COMMENT);
        reconciler.setRepairer(ndr0, Scanner4Partition.MULTILINE_COMMENT);

        NonRuleBasedDamagerRepairer ndr1 = new NonRuleBasedDamagerRepairer(new TextAttribute( _colorManager.getColor(IJavaColorConstants.JAVA_SINGLE_LINE_COMMENT)));
        reconciler.setDamager(ndr1, Scanner4Partition.SINGLELINE_COMMENT);
        reconciler.setRepairer(ndr1, Scanner4Partition.SINGLELINE_COMMENT);

        NonRuleBasedDamagerRepairer ndr2 = new NonRuleBasedDamagerRepairer(new TextAttribute( _colorManager.getColor(IJavaColorConstants.JAVA_STRING)));
        reconciler.setDamager(ndr2, Scanner4Partition.STRING);
        reconciler.setRepairer(ndr2, Scanner4Partition.STRING);

        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(_scanner);
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        return reconciler;
    }

    /**
     * Goals  (on typing, on pasting):
     * * auto-insert  end of pair (), [], {}, "", (open/close comment)
     * * auto-convert tab to space
     * * auto-indent new line
     * * auto-insert double new line for pair
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
     */
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType ){
        //If the TabsToSpacesConverter is not configured here the editor field in the template
        //preference page will not work correctly. Without this configuration the tab key
        //will be not work correctly and instead change the focus
        int tabWidth= getTabWidth(sourceViewer);
        TabsToSpacesConverter tabToSpacesConverter = new TabsToSpacesConverter();
        tabToSpacesConverter.setLineTracker(new DefaultLineTracker());
        tabToSpacesConverter.setNumberOfSpacesPerTab(tabWidth);

        if( IDocument.DEFAULT_CONTENT_TYPE.equals(contentType) ){
            return new IAutoEditStrategy[] {
                    new AutoInsertPair(Activator.getDefault().getPreferenceStore()),
                    new AutoIndentCodeStrategy(),
                    tabToSpacesConverter,
                    //new DefaultIndentLineAutoEditStrategy(),
            };
        } else if (Scanner4Partition.MULTILINE_COMMENT.equals(contentType)) {
            return new IAutoEditStrategy[] {
                    new AutoInsertPair(Activator.getDefault().getPreferenceStore()),
                    new AutoIndentDocStrategy(),
                    tabToSpacesConverter,
                    //new DefaultIndentLineAutoEditStrategy(),
            };
        } else {
            return new IAutoEditStrategy[]{ new DefaultIndentLineAutoEditStrategy() };
        }
    }

    public int getTabWidth( ISourceViewer sourceViewer ){
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        return prefs.getInt(PreferenceConstants.FORMATTER_INDENTATION_SIZE.name());
    }

    /**
     * This methods is necessary for proper implementation of Shift Left and Shift Right.
     *
     * This implementation overrides the default implementation to ensure that only spaces
     * are inserted and not tabs.
     *
     * @returns An array of prefixes. The prefix at position 0 is used when shifting right.
     * When shifting left all the prefixes are checked and one of the matches that prefix is
     * removed from the line.
     */
    public String[] getIndentPrefixes( ISourceViewer sourceViewer, String contentType ){
            int tabWidth = getTabWidth( sourceViewer );

            String[] indentPrefixes = new String[ tabWidth ];
            for( int prefixLength = 1; prefixLength <= tabWidth; prefixLength++  ){
                    char[] spaceChars = new char[prefixLength];
                    Arrays.fill(spaceChars, ' ');
                    indentPrefixes[tabWidth - prefixLength] = new String(spaceChars);
            }

            return indentPrefixes;
    }

    /**
     * Returns the prefixes used when doing prefix operations (eg ToggleComment).
     * Without overriding this method ToggleComment will not work.
     */
    public String[] getDefaultPrefixes( ISourceViewer viewer, String contentType ){
            return new String[] { "//", "" };
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant= new ContentAssistant();
        assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

        IContentAssistProcessor processor= new MyTemplateCompletionProcessor();
        assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
        assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

        return assistant;
    }
}