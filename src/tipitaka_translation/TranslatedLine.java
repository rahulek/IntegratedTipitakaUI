package tipitaka_translation;

/**
 * Created by rahul_ekbote on 8/8/2017.
 */
public class TranslatedLine {
    private final int    m_lineNumber;
    private final String m_lineText;

    public TranslatedLine(int lineNumber, String lineText) {
        m_lineNumber = lineNumber;
        m_lineText = lineText;
    }

    public int getLineNumber() {
        return m_lineNumber;
    }

    public String getLineText() {
        return m_lineText;
    }
}
