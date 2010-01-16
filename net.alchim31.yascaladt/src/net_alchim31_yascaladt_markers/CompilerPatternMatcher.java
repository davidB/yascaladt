package net_alchim31_yascaladt_markers;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Matches relevant compiler (command line, or maven) compiler output
 * and generates corresponding error/warning markers
 */
public class CompilerPatternMatcher implements IPatternMatchListenerDelegate {
    public static final String MARKER_TYPE = "net.alchim31.yascaladt.markers.Problem";

    private TextConsole _console;

    // TODO: we should share the same instance of the marker manager
    // throughout the plugin
    private MarkerInfoExtractor _extractor = new MarkerInfoExtractor();
    private MarkerInfo _previousMarker = null;

    // Caches IFile objects for faster retrieval (to keep ??, introduce bug/inconistancy if eclipse conf change (eg : changing the default association to file,...))
    private Map<String, IFile> _fileCache = new HashMap<String, IFile>();

    @Override
    public void connect(TextConsole console) {
        this._console = console;
    }

    @Override
    public void disconnect() {
        try {
            addMarker(_previousMarker);
            _previousMarker = null;
            _console = null;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    @Override
    public void matchFound(PatternMatchEvent event) {
        try {
            String line = getMatchedText(event);
            MarkerInfo info = _extractor.extract(line, event.getOffset(), _previousMarker);
            if (info == null) {
                // nothing to do
            } else if (info.isReset) {
                _previousMarker = null;
                removeMarkers(info);
            } else {
                if (_previousMarker != null && _previousMarker.filled) {
                    addMarker(_previousMarker);
                    _previousMarker = null;
                }
                if (info != null) {
                    if (info.filled) {
                        addMarker(info);
                    } else {
                        _previousMarker = info;
                    }
                }
            }
        } catch (Exception exc){
            exc.printStackTrace();
        }
    }

    protected String getMatchedText(PatternMatchEvent event) throws Exception{
        int eventOffset= event.getOffset();
        int eventLength= event.getLength();
        // TextConsole console = (TextConsole) event.getSource();
        IDocument document= _console.getDocument();
        String matchedText= null;
        try {
            matchedText = document.get(eventOffset, eventLength);
        } catch (BadLocationException e) {
            matchedText = null;
            throw e;
        }
        return matchedText;
    }

    protected IFile findIFile(IPath path) {
        IFile file= _fileCache.get(path.toPortableString());
        if (file == null) {
            file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
            if (file != null) {
                _fileCache.put(path.toPortableString(), file);
            }
        }
        return file;
    }

    protected IResource findIResource(IPath path) {
        IResource back = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
        if (back == null) {
            back = findIFile(path);
        }
        return back;
    }

    public void removeMarkers(MarkerInfo info) throws Exception {
        IResource back = findIResource(info.path);
        if (back != null) {
            for(IMarker marker : back.findMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE)) {
                marker.delete();
            }
        }
    }

    public void addMarker(MarkerInfo info) throws Exception {
        if (info == null) {
            return;
        }
        IFile file = findIFile(info.path);
        info.newEclipseMarker(file);
        if (file != null && info.hyperlinkLength > 0) {
            IHyperlink link = new FileLink(file, null, -1, -1, info.lineNumber);
            _console.addHyperlink(link, info.hyperlinkBeginAt, info.hyperlinkLength);
        }
    }

    public static class MarkerInfoExtractor {
        private static final Pattern _patternMarkMaven = Pattern.compile("\\s*(([A-Z]:)?[/|\\\\][^\\(\\):]*):(-?\\d+): (error|warning|caution): (.*)");
        private static final Pattern _patternMarkSbt = Pattern.compile("\\[(error|warning)\\]\\s*(([A-Z]:)?[/|\\\\][^\\(\\):]*):(-?\\d+): (.*)");
        private static final Pattern _patternMarkExtraLine = Pattern.compile("\\s*((?:symbol|location|found|required|missing).*)");
        private static final Pattern _patternMarkColumn = Pattern.compile("\\s*(\\^)");
        private static final Pattern _patternReset = Pattern.compile("\\s*(([A-Z]:)?[/|\\\\][^\\(\\):]*):(-?\\d+): info: compiling");

        // TODO add unit-test
        protected MarkerInfo extract(String line,  int lineOffset, MarkerInfo previousMarker) throws Exception {
            MarkerInfo back = null;
            Matcher matcher = null;

            // new marker
            if (back == null) {
                if ((matcher = _patternReset.matcher(line)).find()) {
                    if (matcher.group(1) != null && matcher.group(1).length() > 0) {
                        back = new MarkerInfo();
                        back.path = new Path(matcher.group(1));
                        back.isReset = true;
                    }
                } else if ((matcher = _patternMarkMaven.matcher(line)).find()) {
                    if (matcher.group(1) != null && matcher.group(1).length() > 0) {
                        back = new MarkerInfo();
                        back.path = new Path(matcher.group(1));
                        back.lineNumber = matcher.group(3).equals("") ? null : Integer.parseInt(matcher.group(3));
                        back.columnNumberBegin = null;//matcher.group(4).equals("") ? null : Integer.parseInt(matcher.group(4));
                        back.columnNumberEnd = null;
                        back.isError = matcher.group(4).equalsIgnoreCase("error");
                        back.message = matcher.group(5);
                        back.hyperlinkBeginAt = lineOffset + matcher.start();
                        back.hyperlinkLength = matcher.end() - matcher.start();
                    }
                } else if ((matcher = _patternMarkSbt.matcher(line)).find()) {
                    if(matcher.group(2) != null && matcher.group(2).length() > 0) {
                        back = new MarkerInfo();
                        back.path = new Path(matcher.group(2));
                        back.lineNumber = matcher.group(4).equals("") ? null : Integer.parseInt(matcher.group(4));
                        back.columnNumberBegin = null;//matcher.group(4).equals("") ? null : Integer.parseInt(matcher.group(4));
                        back.columnNumberEnd = null;
                        back.isError = matcher.group(1).equalsIgnoreCase("error");
                        back.message = matcher.group(5);
                        back.hyperlinkBeginAt = lineOffset + matcher.start();
                        back.hyperlinkLength = matcher.end() - matcher.start();
                    }
                }

                if (back != null && previousMarker != null) {
                    previousMarker.filled = true;
                }
            }

            // complete previousMarker ?
            if (back == null && previousMarker != null && !previousMarker.filled) {
                if ((matcher = _patternMarkExtraLine.matcher(line)).find()) {
                    previousMarker.addLineToMessage("... " + matcher.group(1));
                    back = previousMarker;
                } else if ((matcher = _patternMarkColumn.matcher(line)).find()) {
                    previousMarker.setColumn(matcher.start(1) -1);
                    previousMarker.filled = true;
                    back = previousMarker;
                }
            }

            return back;
        }
    }

    protected static class MarkerInfo {
      public IPath path = null;
      public Integer lineNumber = null;
      public Integer columnNumberBegin = null;
      public Integer columnNumberEnd = null;
      public boolean isError = false;
      public boolean isReset = false;
      public String message;
      public boolean filled = false;

      public int hyperlinkBeginAt = -1;
      public int hyperlinkLength = 0;

      public void addLineToMessage(String line) throws Exception {
          message = message + "\n..." + line;
      }

      public void setColumn(int i) throws Exception {
          columnNumberBegin = i+300;
          columnNumberEnd = i + 1;
      }

      public void newEclipseMarker(IResource back) throws Exception {
          if (back != null) {
              IMarker marker = back.createMarker(MARKER_TYPE);
              marker.setAttribute(IMarker.SEVERITY, isError ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING);
              marker.setAttribute("COLUMN_START", columnNumberBegin);
              marker.setAttribute("COLUMN_END", columnNumberEnd);
              marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
              marker.setAttribute(IMarker.MESSAGE, message);
              marker.setAttribute(IMarker.LOCATION, "line: " + lineNumber + ", column: " + columnNumberBegin);
          }
      }
    }
}
