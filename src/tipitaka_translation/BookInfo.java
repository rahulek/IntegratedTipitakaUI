package tipitaka_translation;

/**
 * Created by rahul_ekbote on 8/8/2017.
 */
public class BookInfo {
    private int     m_atIndex;
    private String  m_shortName;
    private String  m_fullName;

    BookInfo(int ndx, String shortName, String fullName) {
        m_atIndex = ndx;
        m_shortName = shortName;
        m_fullName = fullName;
    }

    public int getIndex() {
        return m_atIndex;
    }

    public String getShortName() {
        return m_shortName;
    }

    public String getFullName() {
        return m_fullName;
    }
}
