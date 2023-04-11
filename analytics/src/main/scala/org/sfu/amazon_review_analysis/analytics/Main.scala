package org.sfu.amazon_review_analysis.analytics

import org.apache.spark.sql.SparkSession
import org.sfu.amazon_review_analysis.analytics.input.{InputArgs, InputArguments}

object Main {
  def main(args: Array[String]): Unit = {

    val inputsArgs = new InputArguments(args)

    val spark = SparkSession
      .builder()
      .appName("Amazon Review Analysis Analytics")
      .getOrCreate()

    val inputs = InputArgs(
      inputsArgs.metadataFile.getOrElse(""),
      inputsArgs.reviewFile.getOrElse(""),
      inputsArgs.outPath.getOrElse("")
    )

    new Analytics(spark, inputs).process()
  }
}