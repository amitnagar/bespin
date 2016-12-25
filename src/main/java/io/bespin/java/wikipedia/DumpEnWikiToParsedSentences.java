package io.bespin.java.wikipedia;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class DumpEnWikiToParsedSentences {
  private static class Args {
    @Option(name = "-input", metaVar = "[path]", required = true, usage = "input path")
    String input;

    @Option(name = "-output", metaVar = "[path]", required = true, usage = "output path")
    String output;
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

    PrintWriter writer = new PrintWriter(args.output, "UTF-8");

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

      WikiModel wikiModel = new WikiModel("${image}", "${title}");
      String plainText = wikiModel.render(new PlainTextConverter(), markup);
      String title = WikipediaPageParser.extractTitle(page).replaceAll("\\n+", " ");

      int cnt = 0;
      Reader reader = new StringReader(plainText);
      DocumentPreprocessor dp = new DocumentPreprocessor(reader);
      for (List<HasWord> sentence : dp) {
        writer.print(String.format("%s.%04d\t%s\n", title, cnt, Sentence.listToString(sentence)));
        cnt++;
      }
    }
    writer.close();
  }
}
