package org.sfu.amazon_review_analysis.etl_review

import org.apache.spark.sql.SparkSession
import org.sfu.amazon_review_analysis.etl_review.etl.Review
import org.sfu.amazon_review_analysis.etl_review.input.{InputArgs, InputArguments}

object Main {
  def main(args: Array[String]): Unit = {

    val inputsArgs = new InputArguments(args)

    val spark = SparkSession
      .builder()
      .appName("Amazon review ETL")
      .config("spark.sql.legacy.timeParserPolicy", "LEGACY")
      .config("spark.sql.caseSensitive", true)
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()

    val inputs = InputArgs(
      inputsArgs.reviewFileIn.getOrElse(""),
      inputsArgs.reviewFileOut.getOrElse(""),
      inputsArgs.year.getOrElse(-1)
    )

    new Review(spark).process(inputs)
  }
}

