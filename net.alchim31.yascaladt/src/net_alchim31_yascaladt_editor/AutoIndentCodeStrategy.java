package net_alchim31_yascaladt_editor;

import net_alchim31_yascaladt.Log;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;

/**
 * Auto indent line strategy sensitive to brackets.
 */
public class AutoIndentCodeStrategy extends DefaultIndentLineAutoEditStrategy {

    public AutoIndentCodeStrategy() {
    }

    /* (non-Javadoc)
     * Method declared on IAutoIndentStrategy
     */
    @Override
    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        try {
            if (c.length == 0 && c.text != null && endsWithDelimiter(d, c.text)) {
                smartIndentAfterNewLine(d, c);
            } else if (TextHelper.isCloseBrace(c.text)) { //$NON-NLS-1$
                smartInsertAfterBracket(d, c);
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
     //TOTEST : insert return in : {x}, {...x}, { ...\n...x\n}, {...\n...\nx}
     //TOTEST : insert with [...], (...), {...}
     //TOTEST : insert into "...", """..."""
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
        buf.append(document.get(start, whiteend - start));
        if (TextHelper.getOpenPairCount(document, start, command.offset, true) > 0) {
            buf.append('\t');
        }
        if (command.offset < docLength && TextHelper.isCloseBrace(document.getChar(command.offset))) {
        	System.out.println("openbrace");
            command.shiftsCaret = false;
            command.caretOffset = command.offset + buf.length() + 1;
            int indLine= TextHelper.findMatchingOpenPair(document, line, command.offset, 0);
            if (indLine == -1) {
                indLine= line;
            }
            if (indLine == line) {
                buf.append('\n');
            }
            buf.append(TextHelper.getIndentOfLine(document, indLine));
        }
        command.text= buf.toString();
    }

     
    /**
     * Set the indent of a bracket based on the command provided in the supplied document.
     * @param document - the document being parsed
     * @param command - the command being performed
     */
     private void smartInsertAfterBracket(IDocument document, DocumentCommand command) throws Exception {
        if (command.offset == -1 || document.getLength() == 0)
            return;

        int p= (command.offset == document.getLength() ? command.offset - 1 : command.offset);
        int line= document.getLineOfOffset(p);
        int start= document.getLineOffset(line);
        int whiteend= findEndOfWhiteSpace(document, start, command.offset);

        // shift only when line does not contain any text up to the closing bracket
        if (whiteend == command.offset) {
            // evaluate the line with the opening bracket that matches out closing bracket
            int indLine= TextHelper.findMatchingOpenPair(document, line, command.offset, 1);
            if (indLine != -1 && indLine != line) {
                // take the indent of the found line
                StringBuffer replaceText= new StringBuffer(TextHelper.getIndentOfLine(document, indLine));
                // add the rest of the current line including the just added close bracket
                replaceText.append(document.get(whiteend, command.offset - whiteend));
                replaceText.append(command.text);
                // modify document command
                command.length= command.offset - start;
                command.offset= start;
                command.text= replaceText.toString();
            }
        }
    }
     
     //basic (don't support every case) but should do the job for 75% of uses case
//     private void autoIndentNewLine(IDocument doc, DocumentCommand cmd, int nlIndex) {
//         try {
//             int p = (cmd.offset == doc.getLength() ? cmd.offset - 1 : cmd.offset);
//             IRegion info = doc.getLineInformationOfOffset(p);
//             int start = info.getOffset();
//             int end = TextHelper.findEndOfWhiteSpace(doc, start, cmd.offset);
//             StringBuffer buf = new StringBuffer(cmd.text);
//             if (end > start) buf.append(doc.get(start, end - start));
//             int nlOffset = nlIndex + cmd.offset;
//             int[] openCommentCount = TextHelper.getOpenCommentCount(doc,cmd.offset);
//             boolean newComment = false;
//             if (nlOffset > 1) {
//                 int currentOffset = nlOffset -2;
//                 for(;currentOffset >= 0;--currentOffset) {
//                     char c = doc.getChar(currentOffset);
//                     if (c == '\r' || c == '\n') {
//                         break;
//                     } else if (!Character.isWhitespace(c)) {
//                         break;
//                     }
//                 }
//                 char beforNewline = doc.getChar(currentOffset);
//                 if (beforNewline == '{' || beforNewline == '(' || beforNewline == '[') {
//                     char closePair = TextHelper.getMathingPair(beforNewline);
//                     buf.append('\t');
//                     if (_closeBraces && TextHelper.getOpenPairCount(doc, closePair) > 0) {
//                         // insert end of line bit here
//                         StringBuilder close = new StringBuilder();
//                         if (end > start) close.append(doc.get(start, end - start));
//                         close.append(closePair);
//                         close.append(cmd.text.substring(nlIndex - 1));
//                         int lineEnd = TextHelper.getLineEnd(doc, cmd.offset);
//                         cmd.addCommand(lineEnd, 0, close.toString(), null);
//                         cmd.doit = false;
//                     }
//                 } else if (beforNewline == '*') {
//                     for(--currentOffset;currentOffset >= 0;--currentOffset) {
//                         if (doc.getChar(currentOffset)!= '*')
//                             break;
//                     }
//                     char char1 = doc.getChar(currentOffset);
//                     if (_closeComments && char1== '/') {
//                         if (openCommentCount[0] > 0) {
//                             int lineEnd = TextHelper.getLineEnd(doc, cmd.offset);
//                             cmd.addCommand(lineEnd-1, 1, buf.toString()+" */"+cmd.text.substring(nlIndex-1), null);
//                             cmd.doit = false;
//                             newComment = true;
//                         }
//                     }
//                 }
//             }
//             cmd.text = buf.toString();
//             if (!newComment && openCommentCount[2] >= 0)
//                 cmd.text += " */";
//             else if (openCommentCount[1] >= 0)
//                 cmd.text += " * ";
//         } catch (BadLocationException e) {
//             Activator.getDefault().warn("Error deleting closing pair", e);
//             // stop work
//         }
//     }

}
