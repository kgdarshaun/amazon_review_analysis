package org.sfu.amazon_review_analysis.analytics.input

import org.rogach.scallop.ScallopConf

class InputArguments(arguments: Array[String]) extends ScallopConf(arguments) {
  banner(
    """
      |ETL
      |
      |Example:
      |spark-submit amazon-analytics-<version>.jar
      |     --metadata hdfs:///dataset/metadata
      |     --review hdfs:///dataset/review
      |     --outPath hdfs:///analytics
      |
      |spark-submit amazon-analytics-<version>.jar
      |     --m hdfs:///dataset/metadata
      |     --r hdfs:///dataset/review
      |     --o hdfs:///analytics
      |
      |For usage see below:
      |""".stripMargin)
  val metadataFile = opt[String]("metadata", 'm', required = true, descr = "Metadata File Path")
  val reviewFile = opt[String]("review", 'r', required = true, descr = "Review File Path")
  val outPath = opt[String]("outPath", 'o', required = true, descr = "Output File Path")
  verify()
}