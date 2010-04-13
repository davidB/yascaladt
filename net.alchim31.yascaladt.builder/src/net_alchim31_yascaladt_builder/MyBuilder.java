package net_alchim31_yascaladt_builder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net_alchim31_yascaladt_builder_cs.ScalacsClient;
import net_alchim31_yascaladt_builder_cs.ScalacsClient.Level;
import net_alchim31_yascaladt_builder_cs.ScalacsClient.LogEvent;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.yaml.snakeyaml.Yaml;

//TODO update registration when scalacs shutdown
//TODO update registration when project definition change
//TODO add marker (IPatternMatchListener)
//TODO add colorization (IPageParticipant)
public class MyBuilder extends IncrementalProjectBuilder {

    public static final String BUILDER_ID = "net.alchim31.yascaladt.builder";

    private static final String MARKER_TYPE = "net.alchim31.yascaladt.builder.Problem";

    private final ScalacsClient _scs;

    private MessageConsoleStream _buildConsoleStream;

    private boolean _isRegistered = false;

    public MyBuilder() {
        super();
        _scs = new ScalacsClient("net.alchim31", "scalacs", "0.2");
        MessageConsole buildConsole = new MessageConsole("YascalaDT Build Console", null);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new MessageConsole[]{buildConsole});
        _buildConsoleStream = buildConsole.newMessageStream();
//        listener = new CSharpPatternMatchListener();
//        buildConsole.addPatternMatchListener(listener);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
        try {
            monitor.beginTask("Building", 1);
//            DebugUtil.DebugPrint("Build for project " + project.getName(), debugit);
            boolean hasNoProblems = false;
            if (kind == FULL_BUILD) {
                hasNoProblems = fullBuild(monitor);
            } else {
                IResourceDelta delta = getDelta(getProject());
                if (delta == null) {
                    hasNoProblems = fullBuild(monitor);
                } else {
                    hasNoProblems = incrementalBuild(delta, monitor);
                }
            }
            if (!hasNoProblems) {
                PlatformUI.getWorkbench().getDisplay().syncExec(
                        new Runnable() {
                            public void run() {
                                try {
                                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
                                    showView(IPageLayout.ID_PROBLEM_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);
                                } catch (Exception e) {
                                    // The workbench or view can be null if the build is done
                                    // during the shutdown process
                                }
                            }
                        });
            }
            return null;
        } catch (CoreException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("wrap : " + e, e);
        } finally {
            monitor.done();
        }
    }

    private boolean fullBuild(final IProgressMonitor monitor) throws Exception {
        _isRegistered = false;
        return backendClean(monitor) && backendCompile();
    }

    private boolean incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws Exception {
//        System.out.println("incremental build on "+delta);
//        try {
//           delta.accept(new IResourceDeltaVisitor() {
//              public boolean visit(IResourceDelta delta) {
//                 System.out.println("changed: "+ delta.getResource().getRawLocation());
//                 return true; // visit children too
//              }
//           });
//        } catch (CoreException e) {
//           e.printStackTrace();
//        }
        return backendCompile();
    }

    private boolean backendCompile() throws Exception {
        if (!_isRegistered) {
            if (!display(register())) {
                return false;
            }
            _isRegistered = true; //TODO check output before set register to true
        }
        return display(compile());
    }

    private boolean backendClean(final IProgressMonitor monitor) throws Exception {
        getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
//        for(IMarker marker : getProject().findMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE)) {
//            marker.delete();
//        }
        return display(_scs.sendRequestClean());
    }

    private boolean display(CharSequence out) throws Exception {
        IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
        String str = out.toString();
        _buildConsoleStream.print(str);
        boolean hasNoProblems = true;
        for (LogEvent e : _scs.parse(str)) {
            System.out.println(e.text);
            System.out.println(e.file);
            if (e.file != null) {
                IPath ipath = new Path(e.file.getAbsolutePath());
                if ("compiling".equalsIgnoreCase(e.text.toString())) {
                    IResource r = wsroot.getContainerForLocation(ipath);
                    System.out.println("compiling found, apply on : " + r);
                    if (r != null) {
                        //r.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_INFINITE);
                        for(IMarker marker : r.findMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE)) {
                            marker.delete();
                        }
                    }
                } else if (e.level != Level.INFO) {
                    IResource  r = wsroot.getFileForLocation(ipath);
                    if ( r != null) {
                        IMarker marker = r.createMarker(MARKER_TYPE);
                        marker.setAttribute(IMarker.MESSAGE, e.text);
                        marker.setAttribute(IMarker.SEVERITY, e.level.ordinal());
                        if (e.offset != -1) {
                            marker.setAttribute(IMarker.CHAR_START, e.offset);
                            marker.setAttribute(IMarker.CHAR_END, e.offset + e.length);
                        } else if (e.line != -1) {
                            marker.setAttribute(IMarker.LINE_NUMBER, e.line);
                        }
                        marker.setAttribute(IMarker.LOCATION, e.line + "," + e.column);
                        hasNoProblems = false;
                    }
                }// else no marker for info
            } else if (e.level != Level.INFO) {
                IMarker marker = getProject().createMarker(MARKER_TYPE);
                marker.setAttribute(IMarker.MESSAGE, e.text);
                hasNoProblems = false;
            }
        }
        return hasNoProblems;
    }

    private CharSequence register() throws Exception {
        String yaml = toYaml(getProject()).toString();
//        if (dumpYaml) {
//            new File(project.getBuild().getDirectory()).mkdirs();
//            FileUtils.fileWrite(project.getBuild().getDirectory() + "/project.yaml", "UTF-8", yaml);
//        }
        return _scs.sendRequestCreateOrUpdate(yaml);
    }

    private CharSequence compile() throws Exception {
        return _scs.sendRequestCompile(getProject().getName() + "/.*", true, true);
    }

    //TODO use a more robust ( check each conversion to avoid NPE) IPath => local path algorightm
    private CharSequence toYaml(IProject project) throws Exception {
        IJavaProject javaProject = JavaCore.create(project);
        IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
        HashMap<String, Object> dataCompile = new HashMap<String, Object>();
        //TODO create a yaml project per output dir
        //TODO manage filter
        dataCompile.put("name", project.getName() + "/0");
        List<String> s = new LinkedList<String>();
        for (IPackageFragmentRoot p : javaProject.getPackageFragmentRoots()) {
            if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
                IPath loc = wsroot.getFolder(p.getPath()).getLocation();
                if (loc != null) {
                    s.add(loc.toOSString());
                }
            }
        }
        dataCompile.put("sourceDirs", s);
//        if (includes != null) {
//            dataCompile.put("includes", new ArrayList<String>(includes));
//        }
//        if (excludes != null) {
//            dataCompile.put("excludes", new ArrayList<String>(excludes));
//        }
        dataCompile.put("targetDir", wsroot.getFolder(javaProject.getOutputLocation()).getLocation().toOSString());
        List<String> cp = new LinkedList<String>();
        for(IClasspathEntry ce : javaProject.getResolvedClasspath(true)) {
            cp.add(ce.getPath().makeAbsolute().toOSString());
        }
        dataCompile.put("classpath", cp);
//        if (args != null) {
//            dataCompile.put("args", args);
//        }
//        dataCompile.put("exported", new File(localRepo.getBasedir() , localRepo.pathOf(project.getArtifact())).getCanonicalPath());

        Yaml yaml = new Yaml();
        List<HashMap<String, Object>> prjs = new LinkedList<HashMap<String, Object>>();
        prjs.add(dataCompile);
        return yaml.dumpAll(prjs.iterator());
    }

}
