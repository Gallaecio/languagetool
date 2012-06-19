package org.languagetool.synthesis;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.Tools;

public class BaseSynthesizer implements Synthesizer {
 
  protected IStemmer synthesizer;

  protected ArrayList<String> possibleTags;
  private final String tagFileName;
  private final String resourceFileName;
  
  public BaseSynthesizer(final String resourceFileName, final String tagFileName) {
    this.resourceFileName = resourceFileName;  
    this.tagFileName = tagFileName;
  }

  /**
   * Lookup the inflected forms of a lemma defined by a part-of-speech tag.
   * @param lemma the lemma to be inflected.
   * @param posTag the desired part-of-speech tag.
   * @param results the list to collect the inflected forms.
   */
  protected void lookup(String lemma, String posTag, List<String> results) {
    final List<WordData> wordForms = synthesizer.lookup(lemma + "|" + posTag);
    for (WordData wd : wordForms) {
      results.add(wd.getStem().toString());
    }
  }
  
  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * 
   * @param token
   *          AnalyzedToken to be inflected.
   * @param posTag
   *          The desired part-of-speech tag.
   * @return String value - inflected word.
   */
  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
    initSynthesizer();
    final List<String> wordForms = new ArrayList<String>();
    lookup(token.getLemma(), posTag, wordForms);
    return wordForms.toArray(new String[wordForms.size()]);
  }
      
  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag,
      final boolean posTagRegExp) throws IOException {
    if (posTagRegExp) {
      initSynthesizer();
      initPossibleTags();
      final Pattern p = Pattern.compile(posTag);
      final ArrayList<String> results = new ArrayList<String>();
      for (final String tag : possibleTags) {
        final Matcher m = p.matcher(tag);
        if (m.matches()) {
          lookup(token.getLemma(), tag, results);
        }
      }
      return results.toArray(new String[results.size()]);
    }
    return synthesize(token, posTag);
  }

  protected void initPossibleTags() throws IOException {
    if (possibleTags == null) {
      possibleTags = SynthesizerTools.loadWords(JLanguageTool.getDataBroker().getFromResourceDirAsStream(tagFileName));
    }
  }

  protected void initSynthesizer() throws IOException {
    if (synthesizer == null) {
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(resourceFileName);
      synthesizer = new DictionaryLookup(Dictionary.read(url));
    }
  }

  @Override
  public String getPosTagCorrection(final String posTag) {
    return posTag;
  }

}
