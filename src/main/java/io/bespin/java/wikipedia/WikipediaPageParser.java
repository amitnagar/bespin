package io.bespin.java.wikipedia;

import org.apache.commons.lang.StringEscapeUtils;

public class WikipediaPageParser {

  private static final String XML_START_TAG_TITLE = "<title>";
  private static final String XML_END_TAG_TITLE = "</title>";

  public static final String extractTitle(String s) {
    int start = s.indexOf(XML_START_TAG_TITLE);
    int end = s.indexOf(XML_END_TAG_TITLE, start);
    if (start < 0 || end < 0) {
      return "";
    }
    return StringEscapeUtils.unescapeHtml(s.substring(start + 7, end));
  }

  private static final String XML_START_TAG_ID = "<id>";
  private static final String XML_END_TAG_ID = "</id>";

  public static final String extractId(String s) {
    // parse out the document id
    int start = s.indexOf(XML_START_TAG_ID);
    int end = s.indexOf(XML_END_TAG_ID);
    return (start == -1 || end == -1 || start > end) ? "0" : s.substring(start + 4, end);
  }

  private static final String XML_START_TAG_TEXT = "<text xml:space=\"preserve\"";
  private static final String XML_END_TAG_TEXT = "</text>";

  public static String extractWikiMarkup(String s) {
    // parse out actual text of article
    int textStart = s.indexOf(XML_START_TAG_TEXT);
    int textEnd = s.indexOf(XML_END_TAG_TEXT, textStart);

    if (textStart == -1 || textStart + 27 > textEnd) {
      // Returning empty string is preferable to returning null to prevent NPE.
      return "";
    }

    return s.substring(textStart + 27, textEnd);
  }
}
