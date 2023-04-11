package org.sfu.amazon_review_analysis.dump_amazon_devices_reviews

import org.apache.spark.sql.SparkSession
import org.sfu.amazon_review_analysis.dump_amazon_devices_reviews.input.{InputArgs, InputArguments}

object Main {
  def main(args: Array[String]): Unit = {
    val inputsArgs = new InputArguments(args)

    val spark = SparkSession
      .builder()
      .appName("Amazon Devices Review")
      .getOrCreate()

    val inputs = InputArgs(
      inputsArgs.metadata.getOrElse(""),
      inputsArgs.review.getOrElse(""),
      inputsArgs.outPath.getOrElse("")
    )

    new AmazonDeviceReviews(spark, inputs).dump()
  }
}