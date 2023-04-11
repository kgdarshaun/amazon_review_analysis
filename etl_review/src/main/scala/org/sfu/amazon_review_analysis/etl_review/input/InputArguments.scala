package org.sfu.amazon_review_analysis.etl_review.input

import org.rogach.scallop.ScallopConf

class InputArguments(arguments: Array[String]) extends ScallopConf(arguments) {
  banner(
    """
      |ETL
      |
      |Example:
      |spark-submit amazon-etl-review-<version>.jar
      |     --reviewIn hdfs:///dataset/review.json
      |     --reviewOut hdfs:///dataset/review
      |     --year 2018
      |
      |spark-submit amazon-etl-review-<version>.jar
      |     -i hdfs:///dataset/review.json
      |     -o hdfs:///dataset/review
      |     -y 2018
      |
      |For usage see below:
      |""".stripMargin)
  val reviewFileIn = opt[String]("reviewIn", 'i', required = true, descr = "Input Review File Path")
  val reviewFileOut = opt[String]("reviewOut", 'o', required = true, descr = "Output Review File Path")
  val year = opt[Int]("year", 'y', required = false, descr = "ETL for specific year")
  verify()
}
