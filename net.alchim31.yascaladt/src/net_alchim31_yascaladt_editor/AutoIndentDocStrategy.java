package net_alchim31_yascaladt_editor;

import net_alchim31_yascaladt.Log;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

/**
 * Auto indent line strategy into doc.
 */
public class AutoIndentDocStrategy extends DefaultIndentLineAutoEditStrategy {

    public AutoIndentDocStrategy() {
    }

    /* (non-Javadoc)
     * Method declared on IAutoIndentStrategy
     */
    @Override
    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        try {
            if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text)) {
                smartIndentAfterNewLine(d, c);
            }
        } catch(Exception exc) {
            Log.logException(exc, "failed to Indent");
        }
    }

    /**
     * Returns whether or not the given text ends with one of the documents legal line delimiters.
     *
     * @param d the document
     * @param txt the text
     * @return <code>true</code> if <code>txt</code> ends with one of the document's line delimiters, <code>false</code> otherwise
     */
    private boolean endsWithDelimiter(IDocument d, String txt) {
        String[] delimiters= d.getLegalLineDelimiters();
        if (delimiters != null)
            return TextUtilities.endsWith(delimiters, txt) > -1;
        return false;
    }

    /**
     * Set the indent of a new line based on the command provided in the supplied document.
     * @param document - the document being parsed
     * @param command - the command being performed
     */
     //TOTEST : insert into comment /*...*/
     private void smartIndentAfterNewLine(IDocument document, DocumentCommand command) throws Exception {
        int docLength= document.getLength();
        if (command.offset == -1 || docLength == 0)
            return;
        //if next char (on same line) is a close "brace" => double insert

        int p= (command.offset == docLength ? command.offset - 1 : command.offset);
        int line= document.getLineOfOffset(p);

        StringBuffer buf= new StringBuffer(command.text);

        int start= document.getLineOffset(line);
        int whiteend= findEndOfWhiteSpace(document, start, command.offset);
        boolean docStartAtCurrentLine = document.get(whiteend, command.offset - whiteend).contains("/*");
        String indentation = document.get(start, whiteend - start);
        if (docStartAtCurrentLine) {
            indentation = indentation + " ";
        }
        buf.append(indentation);
        buf.append("* ");
        if (command.offset+2 < docLength && "*/".equals(document.get(command.offset, 2)) && docStartAtCurrentLine) {
            command.shiftsCaret = false;
            command.caretOffset = command.offset + buf.length();
            buf.append("\n");
            buf.append(indentation);
        }
        command.text= buf.toString();
    }
}
