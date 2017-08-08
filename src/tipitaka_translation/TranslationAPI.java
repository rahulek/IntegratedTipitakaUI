package tipitaka_translation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 *
 * @author rahul_ekbote
 */
public class TranslationAPI {
    String m_url = "http://localhost:3000";

    public TranslationAPI() {
    }

    public TranslationAPI(String urlString) {
        if(urlString != null) {
            m_url = urlString;
        }
    }

    public int Ping() {
        try {
            String pingUrl = String.format("%s/ping", m_url);
            URL url = new URL(pingUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
            return -1;
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
            return -1;
        }
        return 1;
    }

    public ArrayList<String> GetBooks() {
        ArrayList<String> allBooks = new ArrayList<>();
        try {
            String getBooksUrl = String.format("%s/books", m_url);
            URL url = new URL(getBooksUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            JsonReader reader = Json.createReader(conn.getInputStream());
            JsonArray arr = reader.readArray();
            System.out.println("Got->" + arr);
            arr.stream().forEach((val) -> {
                JsonValue.ValueType typ = val.getValueType();
                if (typ == JsonValue.ValueType.STRING) {
                    allBooks.add(((JsonString)val).toString());
                }
            });
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
        }
        return allBooks;
    }


    public int GetNumLines(String bookCtx) {
        int numLines = -1;
        try {
            String numLinesUrl = String.format("%s/numlines?ctx=%s", m_url, bookCtx);
            URL url = new URL(numLinesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            JsonReader reader = Json.createReader(conn.getInputStream());
            JsonObject findFirst = reader.readObject();
            if(findFirst != null) {
                numLines = findFirst.getInt("num-lines", -1);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
        }
        return numLines;
    }

    public int GetNumTranslatedLines(String bookCtx) {
        int numTranslatedLines = -1;
        try {
            String numLinesUrl = String.format("%s/numtranslatedlines?ctx=%s", m_url, bookCtx);
            URL url = new URL(numLinesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            JsonReader reader = Json.createReader(conn.getInputStream());
            JsonObject findFirst = reader.readObject();
            if(findFirst != null) {
                numTranslatedLines = findFirst.getInt("NumTranslatedLines", -1);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
        }
        return numTranslatedLines;
    }

    public ArrayList<HashMap<Integer, String>> GetLines(String bookCtx, String language, int startNumber, int endNumber) {
        ArrayList<HashMap<Integer, String>> lines = new ArrayList<>();
        try {
            String getLinesUrl = String.format("%s/lines?ctx=%s&proclang=%s&start=%d&end=%d",
                    m_url, bookCtx, language, startNumber, endNumber);
            URL url = new URL(getLinesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            JsonReader reader = Json.createReader(conn.getInputStream());
            JsonArray arr = reader.readArray();
//            System.out.println("Got->" + arr);
            arr.stream().forEach((JsonValue val) -> {
                if(val.getValueType() == JsonValue.ValueType.OBJECT) {
                    int lineNumber = ((JsonObject)val).getInt("LineNumber", -1);
                    String lineText = ((JsonObject)val).getString("Line");
                    HashMap<Integer, String>e = new HashMap<>();
                    e.put(lineNumber, lineText);
                    lines.add(e);
                }
            });
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
        }
        return lines;
    }

    public HashMap<Integer, String> GetLine(String bookCtx, String language, int lineNumber) {
        //Use an existing function rather than a new HTTP request
        ArrayList<HashMap<Integer, String>> lines = GetLines(bookCtx, language, lineNumber, lineNumber);
        if(lines != null) {
            return lines.get(0);
        } else
            return null;
    }

    public void InsertTranslationLine(String bookCtx, TranslatedLine trLine) {
        try {
            HttpURLConnection conn;

            JsonObjectBuilder objBuilder = Json.createObjectBuilder();
            if(objBuilder != null) {
                objBuilder.add("LineNumber", String.format("%d", trLine.getLineNumber()));
                objBuilder.add("TranslatedText", trLine.getLineText());

                JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
                if(arrBuilder != null) {
                    arrBuilder.add(objBuilder);
                }
                String jsonBody = arrBuilder.build().toString();
                if(jsonBody != null) {
//                System.out.println("Built Array->" + jsonBody);

                    String insertLinesUrl = String.format("%s/insert?ctx=%s", m_url, bookCtx);
                    URL url = new URL(insertLinesUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.addRequestProperty("Accept", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Content-Length", String.format("%d", jsonBody.length()));

                    conn.setDoOutput(true);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    baos.write(jsonBody.getBytes("UTF-8"));
                    baos.writeTo(conn.getOutputStream());
//                System.out.println("Byte Array->" + baos.toByteArray());

                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + conn.getResponseCode());
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
        }

    }

    public void InsertTranslationLines(String bookCtx, ArrayList<TranslatedLine> trLines) {
        try {
            HttpURLConnection conn;

            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            if(arrBuilder == null) {
                throw new RuntimeException("Could not create JsonArrayBuilder");
            }

            trLines.stream().forEach((trLine) -> {
                JsonObjectBuilder objBuilder = Json.createObjectBuilder();
                if (objBuilder != null) {
                    objBuilder.add("LineNumber", String.format("%d", trLine.getLineNumber()));
                    objBuilder.add("TranslatedText", trLine.getLineText());

                    arrBuilder.add(objBuilder);
                }
            });

            String jsonBody = arrBuilder.build().toString();
            if(jsonBody != null) {
                System.out.println("Built Array->" + jsonBody);

                String insertLinesUrl = String.format("%s/insert?ctx=%s", m_url, bookCtx);
                URL url = new URL(insertLinesUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Accept", "application/json; charset=UTF-8");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Content-Length", String.format("%d", jsonBody.length()));

                conn.setDoOutput(true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(jsonBody.getBytes("UTF-8"));
                baos.writeTo(conn.getOutputStream());
//                System.out.println("Byte Array->" + baos.toByteArray());

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }
            }
        } catch (MalformedURLException e) {
            System.err.println("MalformedURLException ->" + e);
        } catch (IOException e) {
            System.err.println("IOException ->" + e);
        }

    }

}
