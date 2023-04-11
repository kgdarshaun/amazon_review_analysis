package org.sfu.amazon_review_analysis.etl_metadata

import org.apache.spark.sql.SparkSession
import org.sfu.amazon_review_analysis.etl_metadata.etl.Metadata
import org.sfu.amazon_review_analysis.etl_metadata.input.{InputArgs, InputArguments}

object Main {
  def main(args: Array[String]): Unit = {

    val inputsArgs = new InputArguments(args)

    val spark = SparkSession
      .builder()
      .appName("Amazon metadata ETL")
      .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
      .config("spark.sql.caseSensitive", true)
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()

    val inputs = InputArgs(
      inputsArgs.metaFileIn.getOrElse(""),
      inputsArgs.metaFileOut.getOrElse("")
    )

    new Metadata(spark).process(inputs)
  }
}

