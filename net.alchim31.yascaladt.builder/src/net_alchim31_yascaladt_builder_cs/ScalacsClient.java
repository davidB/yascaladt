package net_alchim31_yascaladt_builder_cs;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * ScalacsClient is a client used to send request to a scalacs running server.
 *
 * @author davidB
 */
//TODO use Eclipse tool to extract jar and conf from bundle (EFS,...) ?
public class ScalacsClient {
    public static final String BOOT_PROP_RSRC = "scalacs.boot.properties";
    public static Pattern linePattern = Pattern.compile("^-(INFO|WARN|ERROR)\t([^\t]*)\t([^\t]*)\t(.*)$");
    public static Pattern locationPattern = Pattern.compile("([^#]*)#(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)");

    public enum Level {INFO, WARN, ERROR};

    public static class LogEvent {
        public Level level = Level.INFO;
        public String category = "";
        public File file = null;
        public int line = 0;
        public int column = 0;
        public int offset = 0;
        public int length = 0;
        public CharSequence text = "";

        @Override
        public String toString() {
            return level + "*" + category + "*" + file + "*" + line + "*" + column + "*" + offset + "*"+ length + "*"+ text+ "*";
        }
    }

    private String _csGroupId;
    private String _csArtifactId;
    private String _csVersion;

    public ScalacsClient(String csGroupId, String csArtifactId, String csVersion) {
        _csGroupId = csGroupId;
        _csArtifactId = csArtifactId;
        _csVersion = csVersion;
    }

    public List<LogEvent> parse(String response) throws Exception {
        List<LogEvent> back = new LinkedList<LogEvent>();
        BufferedReader in = new BufferedReader(new StringReader(response));
        try {
            for(String l = in.readLine(); l != null; l =in.readLine()){
                Matcher m = linePattern.matcher(l);
                if (m.matches()) {
                    LogEvent e = new LogEvent();
                    e.level = Level.valueOf(m.group(1).toUpperCase());
                    e.category = m.group(2);
                    e.text = m.group(4).replace('§', '\n');
                    Matcher ml = locationPattern.matcher(m.group(3));
                    if (ml.matches()) {
                        e.file = new File(ml.group(1));
                        e.line = Integer.parseInt(ml.group(2));
                        e.column = Integer.parseInt(ml.group(3));
                        e.offset = Integer.parseInt(ml.group(4));
                        e.length = Integer.parseInt(ml.group(5));
                    }
                    back.add(e);
                }
            }
        } finally {
            IOUtils.closeQuietly(in);
        }
        return back;
    }

    /**
     * request to createOrUpdate one or more project define in the Yaml syntax, each project definition should be separated by "---"
     * @return the output (log) of the request
     * @throws Exception
     */
    public String sendRequestCreateOrUpdate(String yamlDef) throws Exception {
        String back = "";
        try {
            back = sendRequest("createOrUpdate", yamlDef);
        } catch (java.net.ConnectException exc) {
            startNewServer();
            back = sendRequest("createOrUpdate", yamlDef);
        }
        return back;
    }

    /**
     * @return the output (log) of the request
     * @throws Exception
     */
    public String sendRequestRemove(String projectName) throws Exception {
        return sendRequest("remove?p=" + projectName, null);
    }

    /**
     *
     * @return the output (log) of the request
     * @throws Exception
     */
    public String sendRequestCompile(String projectName, boolean withDependencies, boolean withDependent) throws Exception {
        StringBuilder query = new StringBuilder("compile");
        if (StringUtils.isNotEmpty(projectName)) {
            query.append("?p=").append(projectName);
            if (!withDependencies) {
                query.append("&noDependencies=true");
            }
            // not supported by scalacs 0.2
            if (!withDependent) {
                query.append("&noDependent=true");
            }
        }
        return sendRequest(query.toString(), null);
    }

    /**
     *
     * @return the output (log) of the request
     * @throws Exception
     */
    public String sendRequestClean() throws Exception {
        return sendRequest("clean", null);
    }

    /**
     *
     * @return the output (log) of the request
     * @throws Exception
     */
    public String sendRequestStop() throws Exception {
        return sendRequest("stop", null);
    }

    protected String sendRequest(String action, String data) throws Exception {
        URL url = new URL("http://127.0.0.1:27616/" + action);
        traceUrl(url);
        URLConnection cnx = url.openConnection();
        cnx.setDoOutput(StringUtils.isNotEmpty(data));
        cnx.setDoInput(true);
        if (StringUtils.isNotEmpty(data)) {
            OutputStream os = cnx.getOutputStream();
            try {
                IOUtils.copy(new StringReader(data), os);
            } finally {
                IOUtils.closeQuietly(os);
            }
        }
        InputStream is = cnx.getInputStream();
        try {
            String back = IOUtils.toString(is);
            return back;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Implementation could override this method if it want to print, log, url requested
     *
     * @throws Exception
     */
    public void traceUrl(URL url) throws Exception {
        //System.out.println("request : " + url);
    }

    /**
     * Implementation should provide a way to startNewServer (used if call sendRequestCreateOrUpdate and no server is up)
     *
     * @throws Exception
     */
    public void startNewServer() throws Exception{
        boolean started = false;
//        _log.info("start scala-tools-server...");
        File installDir = new File(System.getProperty("user.home"), ".sbt-launch");
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
        ILaunchConfiguration found = null;
        for (ILaunchConfiguration configuration : manager.getLaunchConfigurations(type)) {
           if (configuration.getName().equals("Start Scalacs")) {
              //configuration.delete();
              found = configuration;
              break;
           }
        }
        if (found == null) {
            ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, "Start Scalacs");
            //workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.JRE_CONTAINER);
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, installDir.getCanonicalPath()); //$NON-NLS-1
            File booststrapFile = installJar(new File(installDir, "sbt-launch-0.7.2.jar"));
            IRuntimeClasspathEntry bootstrapEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(booststrapFile.getAbsolutePath()));
            bootstrapEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
            IPath systemLibsPath = new Path(JavaRuntime.JRE_CONTAINER);
            IRuntimeClasspathEntry systemLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(systemLibsPath, IRuntimeClasspathEntry.STANDARD_CLASSES);
            ArrayList<String> classpath = new ArrayList<String>();
            classpath.add(bootstrapEntry.getMemento());
            classpath.add(systemLibsEntry.getMemento());
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Dsbt.boot.properties="+ installConf(new File(installDir, _csArtifactId + "-"+ _csVersion +".boot.properties")).getCanonicalPath());
            workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "xsbt.boot.Boot");
            //workingCopy.setAttribute(ATTR_PROGRAM_ARGUMENTS, "start");
            found = workingCopy.doSave();
        }
        if (found == null) {
            throw new IllegalStateException("can't create or find the launcher for scalacs");
        }
        //DebugUITools.launch(found, ILaunchManager.RUN_MODE);
        ILaunch mon = found.launch(ILaunchManager.RUN_MODE, null);
        for(int i = 60; i>0 && !started && !mon.isTerminated(); i--) {
            try {
                System.out.print(".");
                Thread.sleep(1000);
                sendRequest("ping", null);
                started = true;
            } catch (java.net.ConnectException exc) {
                started = false; //useless but more readable
            }
        }
//            System.out.println("");
        if (!started) {
            throw new IllegalStateException("can't start and connect to scalacs");
//        } else {
//            _mojo.getLog().info("scalacs connected");
        }
    }

    private File installConf(File scalaCsBootConf) throws Exception {
        if (!scalaCsBootConf.isFile()) {
            scalaCsBootConf.getParentFile().mkdirs();
            InputStream is = null;
            StringWriter sw = new StringWriter();
            try {
                is = this.getClass().getResourceAsStream(BOOT_PROP_RSRC);
                if (is == null) {
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(BOOT_PROP_RSRC);
                }
                if (is == null) {
                    String abspath = "/" + this.getClass().getPackage().getName().replace('.', '/') + "/" + BOOT_PROP_RSRC;
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(abspath);
                    if (is == null) {
                        throw new IllegalStateException("can't find " + abspath + " in the classpath");
                    }
                }
                IOUtils.copy(is, sw);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(sw);
            }
            Properties p = new Properties(System.getProperties());
            p.setProperty("scalacs.groupId", _csGroupId);
            p.setProperty("scalacs.artifactId", _csArtifactId);
            p.setProperty("scalacs.version", _csVersion);
            p.setProperty("scalacs.directory", scalaCsBootConf.getParentFile().getCanonicalPath());
            String cfg = new StrSubstitutor(p).replace(sw.toString());
            FileUtils.writeStringToFile(scalaCsBootConf, cfg, "UTF-8");
        }
        return scalaCsBootConf.getCanonicalFile();
    }

    private File installJar(File scalaCsBootJar) throws Exception {
        if (!scalaCsBootJar.isFile()) {
            scalaCsBootJar.getParentFile().mkdirs();
            InputStream is = null;
            FileOutputStream os = null;
            try {
                String rsrcpath = "lib/" + scalaCsBootJar.getName();
                is = this.getClass().getResourceAsStream(rsrcpath);
                if (is == null) {
                    is = Thread.currentThread().getContextClassLoader().getResourceAsStream(rsrcpath);
                }
                if (is == null) {
                    throw new IllegalStateException("can't find " + rsrcpath + " in the classpath");
                }
                os = new FileOutputStream(scalaCsBootJar);
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }
        }
        return scalaCsBootJar.getCanonicalFile();
    }
}
