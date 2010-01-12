package net_alchim31_yascaladt;

import java.io.IOException;

import net_alchim31_yascaladt_template.MyTemplateContextType;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "YaScalaDT";
    public static final String TEMPLATE_STORE_ID = PLUGIN_ID + ".template";

    // The shared instance
    private static Activator plugin;

    private ContributionContextTypeRegistry _contextTypeRegistry;

    private TemplateStore _templateStore;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        Log.initializeTraceLogger();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     *
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public TemplateStore getTemplateStore() {
        if (_templateStore == null) {
            _templateStore = new ContributionTemplateStore(getContextTypeRegistry(), getDefault().getPreferenceStore(), TEMPLATE_STORE_ID);
            try {
                _templateStore.load();
            } catch (IOException e) {
                Log.logException(e);
            }
        }
        return _templateStore;
    }

    public ContextTypeRegistry getContextTypeRegistry() {
        if (_contextTypeRegistry == null) {
            _contextTypeRegistry = new ContributionContextTypeRegistry();
            _contextTypeRegistry.addContextType(MyTemplateContextType.CONTEXT_TYPE);
        }
        return _contextTypeRegistry;
    }

    public void log(IStatus status) {
        getLog().log(status);
    }

    public void log(Throwable e) {
        log(new Status(Status.WARNING, PLUGIN_ID, 10001, "exception raised", e));
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void warn(String message, Throwable throwable) {
        log(new Status(Status.WARNING, PLUGIN_ID, 10001, message, throwable));
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Throwable throwable) {
        log(new Status(Status.ERROR, PLUGIN_ID, 10001, message, throwable));
    }
}
