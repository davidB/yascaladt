package net_alchim31_yascaladt_template;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public class MyTemplateContextType extends TemplateContextType {

    public static final String CONTEXT_TYPE = "net.alchim31.yascaladt.template";

    public MyTemplateContextType(){
        addGlobalResolvers();
    }

    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
    }

}
