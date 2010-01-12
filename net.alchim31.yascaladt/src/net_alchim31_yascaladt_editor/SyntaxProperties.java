package net_alchim31_yascaladt_editor;

public class SyntaxProperties /*implements ILanguageSyntaxProperties*/ {

    public String getSingleLineCommentPrefix() {
        return "//";
    }

    public String getIdentifierConstituentChars() {
        // TODO Auto-generated method stub
        return null;
    }

    public int[] getIdentifierComponents(String ident) {
        // TODO Auto-generated method stub
        return new int[0];
    }

    public String[][] getFences() {
        return new String[][] { { "[", "]" }, { "(", ")" }, { "{", "}" } };
    }

    public String getBlockCommentStart() {
        return "/*";
    }

    public String getBlockCommentEnd() {
        return "*/";
    }

    public String getBlockCommentContinuation() {
        return "*";
    }
}
