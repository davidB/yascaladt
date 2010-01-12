package net_alchim31_yascaladt_editor;

import java.util.ArrayList;

import org.eclipse.jface.text.rules.*;

public class Scanner4Partition extends RuleBasedPartitionScanner {
    //string constants for different partition types
    public final static String MULTILINE_COMMENT= "multiline_comment"; //$NON-NLS-1$
    public final static String SINGLELINE_COMMENT= "singleline_comment"; //$NON-NLS-1$
    public final static String STRING ="string"; //$NON-NLS-1$
    public final static String[] PARTITION_TYPES = new String[] {MULTILINE_COMMENT, SINGLELINE_COMMENT, STRING};

    public Scanner4Partition() {

        ArrayList<IPredicateRule> rules= new ArrayList<IPredicateRule>();

        // Add rules for multi-line comments and scaladoc.
        IToken multilinecomment= new Token(MULTILINE_COMMENT);
        rules.add(new MultiLineRule("/*", "*/", multilinecomment, (char) 0, true));

        // Add rule for single line comments.
        IToken singlelinecomment= new Token(SINGLELINE_COMMENT);
        rules.add(new EndOfLineRule("//", singlelinecomment));


        // Add rule for strings and character constants.
        IToken string = new Token(STRING);
        rules.add(new MultiLineRule("\"\"\"", "\"\"\"", string));
        rules.add(new SingleLineRule("\"", "\"", string, '\\'));
        rules.add(new SingleLineRule("'", "'", string, '\\'));

        IPredicateRule[] result= new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }
}
