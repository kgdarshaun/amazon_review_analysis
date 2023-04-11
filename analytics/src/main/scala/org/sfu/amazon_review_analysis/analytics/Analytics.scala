package org.sfu.amazon_review_analysis.analytics

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.sfu.amazon_review_analysis.analytics.input.InputArgs

class Analytics (spark: SparkSession, inputs: InputArgs) {

  import spark.implicits._

  def process() = {

    val metadata = spark.read.parquet(inputs.metadataFile).filter($"main_cat".isNotNull)
    val reviews = spark.read.parquet(inputs.reviewFile)

    val reviewsWithMeta = reviews.join(metadata, "asin").cache()
    val reviewerInfo = reviews.select("reviewerId", "reviewerName").distinct().cache()

    // Number of reviews per category, year and month
    numberReviewsPerCatYearMonth(reviewsWithMeta)

    // Top 5 reviewers with counts per year and month
    top5ReviewersWithCountPerYearMonth(reviews, reviewerInfo)

    // Top 5 brands with most reviews per category
    top5BrandsWithMostReviewsPerCategory(reviewsWithMeta)

    // Top 5 loyal reviewers to brands per category
    top5loyalReviewersWithBrandPerCategory(reviewsWithMeta, reviewerInfo)

    // Top 10 reviewers who spent more money
    top10MostSpentReviewers(reviewsWithMeta, reviewerInfo)

    // TODO: Metrics on price
    // Sales per category, year and month trend
    salesPerCategoryYearMonth(reviewsWithMeta)

    // TODO: Metrics on overall and reviewers
    val overallGrouped = reviewsWithMeta.groupBy($"main_cat", $"brand", $"overall")
      .count()
      .cache()

    // Top 5 brands in each category with most 4,5 ratings
    top5BrandsWith4_5RatingPerCategory(overallGrouped)

    // Top 5 brands in each category with most 1,2,3 ratings
    top5BrandsWith1_2_3RatingPerCategory(overallGrouped)

    // TODO: Metrics on verified reviews/reviewers

    // TODO: Metrics on Review vote
    top10HelpfulReviewers(reviews, reviewerInfo)

    // Process part files to json files
    renamePartFiles(inputs.outPath)
  }

  private def numberReviewsPerCatYearMonth(reviewsWithMeta: DataFrame): Unit = {
    reviewsWithMeta.groupBy($"main_cat", $"reviewYear", $"reviewMonth").count()
      .coalesce(1)
      .write
      .json(getFilePath("category_count"))
  }

  private def top5ReviewersWithCountPerYearMonth(reviews: DataFrame, reviewerInfo: DataFrame) = {

    val idGrouped = reviews.groupBy("reviewerId", "reviewYear", "reviewMonth")
      .count()
      .cache()

    val top5Reviewers = idGrouped.groupBy($"reviewerId")
      .agg(sum($"count").alias("count"))
      .sort(desc("count"))
      .limit(5)
      .select($"reviewerID")

    val top5ReviewersInfo = reviewerInfo.join(top5Reviewers, "reviewerId")

    idGrouped.join(top5ReviewersInfo, "reviewerId")
      .coalesce(1)
      .write
      .json(getFilePath("top_5_reviewers_trend"))
  }

  private def top5BrandsWithMostReviewsPerCategory(reviewsWithMeta: DataFrame) = {
    val windowSpec  = Window.partitionBy("main_cat").orderBy(desc("count"))

    reviewsWithMeta.groupBy($"main_cat", $"brand")
      .count()
      .withColumn("row_number", row_number.over(windowSpec))
      .filter($"row_number".leq(5))
      .drop($"row_number")
      .withColumn("brand", regexp_replace($"brand", "\n", ""))
      .coalesce(1)
      .sort(asc("main_cat"), desc("count"))
      .write
      .json(getFilePath("top_5_brands_per_category"))
  }

  private def top5loyalReviewersWithBrandPerCategory(reviewsWithMeta: DataFrame, reviewerInfo: DataFrame) = {
    val windowSpec  = Window.partitionBy("main_cat").orderBy(desc("count"))

    val top5LoyalReviewers = reviewsWithMeta.groupBy($"reviewerId", $"brand", $"main_cat")
      .count()
      .withColumn("row_number", row_number.over(windowSpec))
      .filter($"row_number".leq(5))
      .drop($"row_number")

    reviewerInfo.join(top5LoyalReviewers, "reviewerId")
      .coalesce(1)
      .sort(asc("main_cat"), desc("count"))
      .write
      .json(getFilePath("top_5_loyal_reviewers_by_category"))
  }

  private def salesPerCategoryYearMonth(reviewsWithMeta: DataFrame) = {
    reviewsWithMeta.select($"main_cat", $"price", $"reviewYear", $"reviewMonth")
      .groupBy($"main_cat", $"reviewYear", $"reviewMonth")
      .agg(sum($"price").as("sales"))
      .coalesce(1)
      .sort(asc("reviewYear"), asc("reviewMonth"), asc("main_cat"))
      .write
      .json(getFilePath("sales_per_category_year_month"))
  }

  private def top10MostSpentReviewers(reviewsWithMeta: DataFrame, reviewerInfo: DataFrame) = {
    val top10MostSpentReviewers = reviewsWithMeta.select($"reviewerId", $"price")
      .groupBy($"reviewerId").agg(sum($"price").as("expenditure"))
      .sort(desc("expenditure"))
      .limit(10)

    reviewerInfo.join(top10MostSpentReviewers, "reviewerId")
      .coalesce(1)
      .write
      .json(getFilePath("top_10_most_spent_reviewers"))
  }

  private def top5BrandsWith4_5RatingPerCategory(overallGrouped: DataFrame) = {
    overallGrouped
      .filter($"overall".geq(4))
      .groupBy($"main_cat", $"brand")
      .agg(sum($"count").alias("count"))
      .sort(desc("count"))
      .limit(5)
      .coalesce(1)
      .write
      .json(getFilePath("top_5_brand_with_5_4_rating"))
  }

  private def top5BrandsWith1_2_3RatingPerCategory(overallGrouped: DataFrame) = {
    overallGrouped
      .filter($"overall".leq(3))
      .groupBy($"main_cat", $"brand")
      .agg(sum($"count").alias("count"))
      .sort(desc("count"))
      .limit(5)
      .coalesce(1)
      .write
      .json(getFilePath("top_5_brand_with_1_2_3_rating"))
  }

  private def top10HelpfulReviewers(reviews: DataFrame, reviewerInfo: DataFrame) = {
    val top10HelpfulReviewers = reviews.select("reviewerId", "vote")
      .groupBy($"reviewerId").agg(sum("vote").alias("votes"))
      .sort(desc("votes"))
      .limit(10)

    reviewerInfo.join(top10HelpfulReviewers.hint("broadcast"), "reviewerId")
      .coalesce(1)
      .write
      .json(getFilePath("top_10_helpful_reviewers"))
  }




  private def getFilePath(fileName: String): String = {
    Seq(inputs.outPath, fileName).mkString("/")
  }

  private def renamePartFiles(directory: String) = {
    val fileSystem = new HdfsUtils(spark).getFileSystem()

    fileSystem.listStatus(new Path(directory))
      .filter(_.isDirectory)
      .map(dir => {
        val path = dir.getPath
        renamePartFile(path, fileSystem)
        fileSystem.delete(path, true)
      })
  }

  private def renamePartFile(filePath: Path, fileSystem: FileSystem) = {
    val partFilePath = fileSystem.listStatus(filePath)
      .map(_.getPath.toString)
      .filter(_.contains("part-"))
      .head
    val jsonFileName = partFilePath.split('/').dropRight(1).last + ".json"
    val jsonFilePath = getFilePath(jsonFileName)

    fileSystem.rename(new Path(partFilePath), new Path(jsonFilePath))
  }
}
