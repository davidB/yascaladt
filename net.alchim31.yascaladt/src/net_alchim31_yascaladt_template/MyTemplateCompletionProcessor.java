package net_alchim31_yascaladt_template;

import net_alchim31_yascaladt.Activator;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;

/**
 * Completion processor used for YEdit templates.
 */
public class MyTemplateCompletionProcessor extends TemplateCompletionProcessor {

    @Override
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        return Activator.getDefault().getContextTypeRegistry().getContextType(MyTemplateContextType.CONTEXT_TYPE);
    }

    @Override
    protected Image getImage(Template template) {
        return null;
    }

    /**
     * @return All the templates for the specified context type id. All the template objects
     * are actually YEditTemplate objects and not Template objects. By returning YEditTemplate
     * objects we can override the default match() method in template and get more sensible
     * template matching.
     */
    @Override
    protected Template[] getTemplates(String contextTypeId) {
        return Activator.getDefault().getTemplateStore().getTemplates();
    }

}
