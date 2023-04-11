package org.sfu.amazon_review_analysis.etl_review.etl

import org.apache.spark.sql.functions.{month, to_date, year}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.sfu.amazon_review_analysis.etl_review.input.InputArgs
import org.sfu.amazon_review_analysis.etl_review.schema

class Review(spark: SparkSession) {

  import spark.implicits._

  def process(inputs: InputArgs): Unit = {
    val reviewDf = spark.read.json(inputs.reviewFileIn)

    val reviewPreProcessed = reviewDf.withColumn("reviewTime", to_date($"reviewTime", "MM dd, yyyy"))
      .withColumn("reviewYear", year($"reviewTime"))
      .withColumn("reviewMonth", month($"reviewTime"))
      .drop($"style")

    if (inputs.year == -1) {
      reviewPreProcessed
        .repartition(264, $"reviewYear", $"reviewMonth")
    } else {
      val filteredReview = filterPartition(reviewPreProcessed, inputs.year)
      writeByPartitions(filteredReview, inputs.year, inputs.reviewFileOut)
    }
  }

  def filterPartition(reviews: DataFrame, year: Int): Dataset[schema.Review] = {
    reviews.filter(s"reviewYear == ${year}")
      .as[schema.Review]
  }

  def writeByPartitions(reviews: Dataset[schema.Review], year: Int, file: String): Unit = {
    reviews.write
      .partitionBy("reviewYear", "reviewMonth")
      .parquet(Seq(file, year).mkString("_"))
  }
}
