package net_alchim31_yascaladt_editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

/**
 *
 * @author dwayne
 * @based (copy/paste) from org.eclipse.jdt.internal.ui.text.AbstractJavaScanner and JavaCodeScanner(final)
 */
@SuppressWarnings("restriction")
public class Scanner4Code extends  AbstractJavaScanner {//RuleBasedScanner {
    private final static String[] KEYWORDS = {
        "package", "import",
        "class", "object", "trait","extends", "with",
        "def", "val", "var", "type",
        "public", "protected", "private",
        "lazy", "implicit", "override", "synchronized", "abstract", "sealed",
        "match", "case", "=>",
        "try", "catch", "finally",
        "do", "while", "for", "yield",
        "if", "else",
        "return",
        "this", "super", "new", "throw",
        "true", "false", "null"
    };

//    private IColorManager fColorManager;
//    private IPreferenceStore fPreferenceStore;

    public Scanner4Code(IColorManager colorManager,  IPreferenceStore jdtPreferenceStore) {
        super(colorManager, jdtPreferenceStore);
        initialize();
        IToken other = getToken(IJavaColorConstants.JAVA_DEFAULT);
        setDefaultReturnToken(other);
    }

    protected List<IRule> createRules() {
        ArrayList<IRule> rules= new ArrayList<IRule>();

        // Add word rule for keywords.
        IToken keyword = getToken(IJavaColorConstants.JAVA_KEYWORD);
        WordRule keywordRule = new WordRule(new IWordDetector() {
            public boolean isWordStart(char c) {
                return Character.isLetter(c); //isJavaIdentifierStart(c);
            }
            public boolean isWordPart(char c) {
                return Character.isLetter(c);//isJavaIdentifierPart(c);
            }
        });
        for (String k : KEYWORDS) {
            keywordRule.addWord(k, keyword);
        }
        rules.add(keywordRule);

//        // Add rules for multi-line comments and scaladoc.
//        IToken multilinecomment = new Token(new TextAttribute(colors.getColor(SharedTextColors.MULTI_LINE_COMMENT)));
//        rules.add(new MultiLineRule("/*", "*/", multilinecomment, (char) 0, true));
//
//        // Add rule for single line comments.
//        IToken comment = new Token(new TextAttribute(colors.getColor(SharedTextColors.SINGLE_LINE_COMMENT)));
//        rules.add(new EndOfLineRule("//", comment));
//
//        // Add rule for strings and character constants.
//        IToken string = new Token(new TextAttribute(colors.getColor(SharedTextColors.STRING)));
//        rules.add(new MultiLineRule("\"\"\"", "\"\"\"", string));
//        rules.add(new SingleLineRule("\"", "\"", string, '\\'));
//        rules.add(new SingleLineRule("'", "'", string, '\\'));

        // A generic whitespace rule.
        rules.add(new WhitespaceRule(new Detector4Whitespace()));

        rules.add(new BracketRule(getToken(IJavaColorConstants.JAVA_BRACKET)));

        return rules;
    }
    //
    // (copy/paste) from org.eclipse.jdt.internal.ui.text.AbstractJavaScanner and JavaCodeScanner(final)

    private static final String[] fgTokenProperties = {
        IJavaColorConstants.JAVA_KEYWORD,
        IJavaColorConstants.JAVA_STRING,
        IJavaColorConstants.JAVA_DEFAULT,
        IJavaColorConstants.JAVA_KEYWORD_RETURN,
        IJavaColorConstants.JAVA_OPERATOR,
        IJavaColorConstants.JAVA_BRACKET,
    };

    @Override
    protected String[] getTokenProperties() {
        return fgTokenProperties;
    }

    /**
     * Rule to detect java brackets.
     *
     *
     * @based
     */
    private static final class BracketRule implements IRule {

        /** Java brackets */
        private final char[] JAVA_BRACKETS= { '(', ')', '{', '}', '[', ']' };
        /** Token to return for this rule */
        private final IToken fToken;

        /**
         * Creates a new bracket rule.
         *
         * @param token Token to use for this rule
         */
        public BracketRule(IToken token) {
            fToken= token;
        }

        /**
         * Is this character a bracket character?
         *
         * @param character Character to determine whether it is a bracket character
         * @return <code>true</code> iff the character is a bracket, <code>false</code> otherwise.
         */
        public boolean isBracket(char character) {
            for (int index= 0; index < JAVA_BRACKETS.length; index++) {
                if (JAVA_BRACKETS[index] == character)
                    return true;
            }
            return false;
        }

        /*
         * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
         */
        public IToken evaluate(ICharacterScanner scanner) {

            int character= scanner.read();
            if (isBracket((char) character)) {
                do {
                    character= scanner.read();
                } while (isBracket((char) character));
                scanner.unread();
                return fToken;
            } else {
                scanner.unread();
                return Token.UNDEFINED;
            }
        }
    }

}
