package org.sfu.amazon_review_analysis.dump_amazon_devices_reviews.input

import org.rogach.scallop.ScallopConf

class InputArguments(arguments: Array[String]) extends ScallopConf(arguments) {
  banner(
    """
      |ETL
      |
      |Example:
      |spark-submit dump-amazon-devices-reviews-<version>.jar
      |     --metadata hdfs:///dataset/metadata
      |     --review hdfs:///dataset/review
      |     --outPath hdfs:///amazon_devices
      |
      |spark-submit dump-amazon-devices-reviews-<version>.jar
      |     --m hdfs:///dataset/metadata
      |     --r hdfs:///dataset/review
      |     --o hdfs:///amazon_devices
      |
      |For usage see below:
      |""".stripMargin)
  val metadata = opt[String]("metadata", 'm', required = true, descr = "Metadata File Path")
  val review = opt[String]("review", 'r', required = true, descr = "Review File Path")
  val outPath = opt[String]("outPath", 'o', required = true, descr = "Output File Path")
  verify()
}
