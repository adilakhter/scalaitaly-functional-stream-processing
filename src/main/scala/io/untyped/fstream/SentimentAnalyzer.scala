package io.untyped.fstream

import java.util.Properties

import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{ Annotation, StanfordCoreNLP }
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations

import scala.collection.convert.wrapAll._

object SentimentAnalyzer {
  val props = new Properties()
  props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")

  val pipeline: StanfordCoreNLP = new StanfordCoreNLP(props)

  def sentiment(input: String): Int = Option(input) match {
    case Some(text) if !text.isEmpty ⇒
      extractSentiments(text) maxBy {
        case (sentence, _) ⇒ sentence.length
      } match {
        case (_, sentiment) ⇒ sentiment
      }
    case _ ⇒ throw new IllegalArgumentException("input can't be null or empty")
  }

  def extractSentiments(text: String): List[(String, Int)] = {
    val annotation: Annotation = pipeline.process(words(text))
    val sentences = annotation.get(classOf[CoreAnnotations.SentencesAnnotation])

    sentences
      .map(sentence ⇒ (sentence, sentence.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])))
      .map { case (sentence, tree) ⇒ (sentence.toString, RNNCoreAnnotations.getPredictedClass(tree)) }.toList
  }

  def words(text: String): String = {
    text.split(" ").filter(_.matches("^[a-zA-Z0-9 ]+$")).fold("")((a, b) ⇒ a + " " + b).trim
  }
}