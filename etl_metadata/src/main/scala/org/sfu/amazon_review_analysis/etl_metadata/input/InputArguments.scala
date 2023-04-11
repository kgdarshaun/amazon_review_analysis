package org.sfu.amazon_review_analysis.etl_metadata.input

import org.rogach.scallop.ScallopConf

class InputArguments(arguments: Array[String]) extends ScallopConf(arguments) {
  banner(
    """
      |ETL
      |
      |Example:
      |spark-submit amazon-etl-metadata-<version>.jar
      |     --metaIn hdfs:///dataset/metadata.json
      |     --metaOut hdfs:///dataset/metadata
      |
      |spark-submit amazon-etl-metadata-<version>.jar
      |     -i hdfs:///dataset/metadata.json
      |     -o hdfs:///dataset/metadata
      |
      |For usage see below:
      |""".stripMargin)
  val metaFileIn = opt[String]("metaIn", 'i', required = true, descr = "Input Metadata File Path")
  val metaFileOut = opt[String]("metaOut", 'o', required = true, descr = "Output Metadata File Path")
  verify()
}
