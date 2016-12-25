package io.bespin.java.wikipedia;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class WikipediaBz2DumpInputStream {
  private static final int DEFAULT_STRINGBUFFER_CAPACITY = 1024;

  private BufferedReader br;
  private FileInputStream fis;

  /**
   * Creates an input stream for reading Wikipedia articles from a bz2-compressed dump file.
   *
   * @param file path to dump file
   * @throws IOException
   */
  public WikipediaBz2DumpInputStream(String file) throws IOException {
    br = null;
    fis = new FileInputStream(file);
    byte[] ignoreBytes = new byte[2];
    fis.read(ignoreBytes); // "B", "Z" bytes from commandline tools
    br = new BufferedReader(new InputStreamReader(new CBZip2InputStream(fis), "UTF8"));
  }

  public String readNext() throws IOException {
    String s;
    StringBuffer sb = new StringBuffer(DEFAULT_STRINGBUFFER_CAPACITY);

    while ((s = br.readLine()) != null) {
      if (s.endsWith("<page>"))
        break;
    }

    if (s == null) {
      fis.close();
      br.close();
      return null;
    }

    sb.append(s + "\n");

    while ((s = br.readLine()) != null) {
      sb.append(s + "\n");

      if (s.endsWith("</page>"))
        break;
    }

    return sb.toString();
  }

  private static class Args {
    @Option(name = "-input", metaVar = "[path]", required = true, usage = "input path")
    String input;
  }

  public static void main(String[] argv) throws Exception {
    Args args = new Args();
    CmdLineParser parser = new CmdLineParser(args, ParserProperties.defaults().withUsageWidth(100));

    try {
      parser.parseArgument(argv);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      parser.printUsage(System.err);
      System.exit(-1);
    }
    PrintStream out = new PrintStream(System.out, true, "UTF-8");

    WikipediaBz2DumpInputStream stream = new WikipediaBz2DumpInputStream(args.input);
    String page;
    while ((page = stream.readNext()) != null) {
      if ( page.contains("<ns>") && !page.contains("<ns>0</ns>")) {
        continue;
      }

      String markup = WikipediaPageParser.extractWikiMarkup(page);
      if (markup.contains("#REDIRECT")) {
        continue;
      }

      out.println("Title: " + WikipediaPageParser.extractTitle(page));
      out.println("Id: " + WikipediaPageParser.extractId(page));
      out.println("Markup: \n\n" + markup);
      out.println("\n\n#################################\n");
    }
    out.close();
  }
}
