package tipitaka_translation; /**
 * Created by rahul_ekbote on 8/8/2017.
 */
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *
 * @author rahul_ekbote
 */
public class FXMLDocumentController implements Initializable {
    private TranslationAPI m_api;
    private static final String LangPali = "Pali"; //Pāli";
    private static final String LangMarathi = "Marathi"; //Marāṭhī";
    private static  HashMap<String, BookInfo> bookMappings;
    private String m_currentBookContext = "";

    @FXML TextField m_serverURL;
    @FXML Label     m_serverStatus;
    @FXML ListView  m_BooksList;
    @FXML TextField m_startLine;
    @FXML TextField m_endLine;
    @FXML Button    m_fetchButton;
    @FXML Label     m_paliTextBook;
    @FXML TextArea  m_paliText;
    @FXML TextField m_translatedTextLine;
    @FXML TextArea  m_translatedText;
    @FXML Button    m_saveTranslatedText;
    @FXML Label     m_saveStatus;
    @FXML Button    m_getTranslatedText;

    @FXML
    private void onPingClicked(ActionEvent event) {
        System.out.println("Ping Clicked.");
        if(m_api != null) {
            if(m_api.Ping() == 1) {
                m_serverStatus.setTextFill(Color.GREEN);
                m_serverStatus.setText("Server is Alive.");
            } else {
                m_serverStatus.setTextFill(Color.RED);
                m_serverStatus.setText("Server is NOT available.");
            }
        }
    }

    private BookInfo getSelectedBookInfo() {
        int selectedIndex = m_BooksList.getSelectionModel().getSelectedIndex();
        return getBookInfo(selectedIndex);
    }

    private BookInfo getBookInfo(int ndx) {
        BookInfo selectedBook = null;
        for(BookInfo bi : bookMappings.values()) {
            if(ndx == bi.getIndex()) {
                selectedBook = bi;
            }
        }
        return selectedBook;
    }

    private void fetchLinesCommon() {
        if(m_api != null) {
            BookInfo selectedBook = getSelectedBookInfo();
            if(selectedBook == null) {
                System.err.println("Selected Book could not be found..");
                return;
            }

            m_currentBookContext = selectedBook.getShortName();

            int startLine = -1;
            int endLine = -1;
            try {
                String startLineS = m_startLine.getText();
                if(startLineS != null && !startLineS.isEmpty()) {
                    startLine = Integer.parseInt(startLineS);
                }
                String endLineS = m_endLine.getText();
                if(endLineS != null && !endLineS.isEmpty()) {
                    endLine = Integer.parseInt(endLineS);
                }

                if(endLine < startLine) {
                    throw new Exception("Ending Line number must be greater than Starting Line number");
                }

                ArrayList<HashMap<Integer, String>> lines = m_api.GetLines(m_currentBookContext,
                        LangPali,
                        startLine, endLine);
//               System.out.println("Fetched these lines..\n" + lines);
                StringBuffer  sb = new StringBuffer();
                int currLine = startLine;
                for(HashMap<Integer, String> line : lines) {
                    sb.append(String.format("%d. %s\n\n", currLine, line.get(currLine)));
                    currLine++;
                }
                //           System.out.println("SB->" + sb.toString());
                ;               m_paliTextBook.setText(String.format("Pāli Text: %s", selectedBook.getFullName()));
                paliTextBookCommon(selectedBook);
                m_paliText.setText(sb.toString());
                //clear up
                m_translatedTextLine.clear();
                m_translatedText.clear();
                m_saveStatus.setText("");

            } catch(Exception ex) {
                System.err.println("Exception during getting start and end line numbers.");
                m_paliText.clear();
                m_translatedText.clear();
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText(String.format("Could not fetch the requestd lines: %d to %d", startLine, endLine));
            }

        }
    }

    private void paliTextBookCommon(BookInfo bookInfo) {
        int numTotalLinesInTheBook = m_api.GetNumLines(bookInfo.getShortName());
        int numTranslatedLinesInTheBook = m_api.GetNumTranslatedLines(bookInfo.getShortName());

        m_paliTextBook.setText(String.format("Pāli Text: %s\t\t\tTotal Lines: %d\tTranslated: %d", bookInfo.getFullName(),
                numTotalLinesInTheBook, numTranslatedLinesInTheBook));
    }

    @FXML
    private void onFetchLinesClicked(ActionEvent event) {
//       System.out.println("Fetch Lines Clicked.");
        fetchLinesCommon();
    }

    @FXML
    private void onSaveTranslatedTextClicked(ActionEvent event) {
//       System.out.println("Save Translated Text Clicked.");
        if(m_api != null) {
            int translatedLineNumber = -1;
            String translatedText = "";
            try {
                translatedLineNumber = Integer.parseInt(m_translatedTextLine.getText());

                if(translatedLineNumber == -1) {
                    throw new InvalidLineNumberException("Invalid Translated Text Line Number.");
                }

                translatedText = m_translatedText.getText();
                if(translatedText == null || translatedText.isEmpty()) {
                    throw new InvalidTranslatedTextException("Translated Text is empty");
                }
            } catch(InvalidLineNumberException ex) {
                System.err.println("Exception during getting the translated line number.");
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText("Please specify valid line number to fetch from the translation db.");
                return;
            } catch(InvalidTranslatedTextException ex) {
                System.err.println("Exception during getting the translated text.");
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText("Please specify translated text to be saved to the translation db.");
                return;
            } catch(Exception ex) {
                System.err.println("Exception during getting the translated line number.");
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText("Please specify valid line number to fetch from the translation db.");
                return;
            }
            TranslatedLine trLine = new TranslatedLine(translatedLineNumber, translatedText);

            try {
                //Insert
                m_api.InsertTranslationLine(m_currentBookContext, trLine);

                m_saveStatus.setTextFill(Color.GREEN);
                m_saveStatus.setText(String.format("Line %d saved successfully.", translatedLineNumber));

                BookInfo selectedBook = getSelectedBookInfo();
                if(selectedBook != null) {
                    paliTextBookCommon(selectedBook);
                }
            } catch(Exception ex) {
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText(String.format("Line %d COULD NOT be saved.", translatedLineNumber));
            }
        }
    }

    @FXML
    private void onGetTranslatedTextClicked(ActionEvent event) {
//       System.out.println("Get Translated Text Clicked.");
        if(m_api != null) {
            int translatedLineNumber = -1;
            String translatedText = "";
            try {
                translatedLineNumber = Integer.parseInt(m_translatedTextLine.getText());

                if(translatedLineNumber == -1) {
                    throw new InvalidLineNumberException("Invalid Translated Text Line Number.");
                }

                HashMap<Integer, String> retData  = m_api.GetLine(m_currentBookContext, LangMarathi, translatedLineNumber);
                translatedText = retData.get(translatedLineNumber);
//               System.out.println("Marathi Line No.: " + translatedLineNumber + ", Text= " + translatedText);
            } catch(InvalidLineNumberException ex) {
                System.err.println("Exception during getting marathi line.");
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText(String.format("Could not get translated text for line %d. Probably the line is not yet translated.", translatedLineNumber));
                m_translatedText.clear();
                return;
            } catch(Exception ex) {
                System.err.println("Exception during getting marathi line.");
                m_saveStatus.setTextFill(Color.RED);
                m_saveStatus.setText("Could not get translated text, Invalid line number for the translated line.");
                m_translatedText.clear();
                return;
            }
            m_saveStatus.setTextFill(Color.GREEN);
            m_saveStatus.setText("Fetched Translated Line Successfully.");
            m_translatedText.setText(translatedText);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            bookMappings = new HashMap<>();
            bookMappings.put("VinayaMahavagga", new BookInfo(0, "VinayaMahavagga", "Vinaya Piṭaka -> Mahāvagga"));
            bookMappings.put("VinayaCullavagga", new BookInfo(1, "VinayaCullavagga", "Vinaya Piṭaka -> Cullavagga"));
            bookMappings.put("VinayaPacittiya", new BookInfo(2, "VinayaPacittiya", "Vinaya Piṭaka -> Pācittiya"));
            bookMappings.put("VinayaParajika", new BookInfo(3, "VinayaParajika", "Vinaya Piṭaka -> Pārājikā"));
            bookMappings.put("VinayaParivara", new BookInfo(4, "VinayaParivara", "Vinaya Piṭaka -> Parivāra"));

            m_api = new TranslationAPI();

            ObservableList<String> books = FXCollections.observableArrayList();
            for(int i=0; i<bookMappings.size(); i++) {
                BookInfo bi = getBookInfo(i);
                if(bi != null) {
                    books.add(bi.getFullName());
                }
            }
//            System.out.println("Books-> " + books);
            m_BooksList.setItems(books);
            m_BooksList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            m_BooksList.getSelectionModel().select(0);

            m_BooksList.getSelectionModel().selectedItemProperty().addListener(
                    new ChangeListener<String>() {
                        public void changed(ObservableValue<? extends String> ov, String oldVal, String newVal) {
//                           System.out.println("Old: " + oldVal + ", New: " + newVal + " New @Ndx: " + m_BooksList.getSelectionModel().getSelectedIndex());
                            BookInfo selectedBook = getSelectedBookInfo();
                            if(selectedBook != null) {
                                m_currentBookContext = selectedBook.getShortName();
                                ;                               m_paliTextBook.setText(selectedBook.getFullName());
                                paliTextBookCommon(selectedBook);
                            }
                            fetchLinesCommon();
                        }
                    });

            m_paliText.clear();
            //Initialize current book context based on selected of the boox
            BookInfo selectedBook = getSelectedBookInfo();
            if(selectedBook != null) {
                m_currentBookContext = selectedBook.getShortName();
                ;                m_paliTextBook.setText(selectedBook.getFullName());
                paliTextBookCommon(selectedBook);
            }
        } catch (Exception ex) {
            System.err.println("Exception: " + ex);
            throw ex;
        }

    }

}
