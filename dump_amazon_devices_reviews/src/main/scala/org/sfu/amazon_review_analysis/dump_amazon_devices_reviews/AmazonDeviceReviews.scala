package org.sfu.amazon_review_analysis.dump_amazon_devices_reviews

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.sfu.amazon_review_analysis.dump_amazon_devices_reviews.input.InputArgs

class AmazonDeviceReviews(spark: SparkSession, inputs: InputArgs) {

  import spark.implicits._

  def dump(): Unit = {
    val metadata = spark.read.parquet(inputs.metadata)
    val reviews = spark.read.parquet(inputs.review)

    val amazonDevicesMeta = metadata.filter($"main_cat" === "Amazon Devices")
      .filter($"brand" === "Amazon")
      .cache()

    dumpAmazonEcho(amazonDevicesMeta, reviews)

    val amazonFireTvWithStickMeta = amazonDevicesMeta.filter($"title".contains("Fire TV"))
      .cache()
    val amazonFireTvStickMeta = amazonFireTvWithStickMeta.filter($"title".contains("Fire TV Stick")).cache()

    dumpAmazonFireTvStick(amazonFireTvStickMeta, reviews)

    dumpAmazonFireTv(amazonFireTvWithStickMeta, amazonFireTvStickMeta, reviews)

    dumpAmazonFireTablets(amazonDevicesMeta, reviews)

    dumpAmazonSecurityCameras(amazonDevicesMeta, reviews)

    dumpAmazonTap(amazonDevicesMeta, reviews)

    dumpAmazonKindle(amazonDevicesMeta, reviews)

  }

  private def dumpAmazonEcho(amazonDevicesMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonEchoAsins = selectAsin(
      amazonDevicesMeta
        .filter($"title".contains("Echo"))
    )
    reviews.join(amazonEchoAsins, "asin").write.parquet(getFilePath("echo"))
  }

  private def dumpAmazonFireTvStick(amazonFireTvStickMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonFireTvStickAsin = selectAsin(
      amazonFireTvStickMeta
    )
    reviews.join(amazonFireTvStickAsin, "asin").write.parquet(getFilePath("fire_tv_stick"))
  }

  private def dumpAmazonFireTv(amazonFireTvWithStickMeta: DataFrame, amazonFireTvStickMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonFireTvAsin = selectAsin(
      amazonFireTvWithStickMeta.except(amazonFireTvStickMeta)
    )
    reviews.join(amazonFireTvAsin, "asin").write.parquet(getFilePath("fire_tv"))
  }

  private def dumpAmazonFireTablets(amazonDevicesMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonTabletsAsin = selectAsin(
      amazonDevicesMeta.filter($"title".contains("Fire HD") || $"title".contains("Fire Tablet"))
    )
    reviews.join(amazonTabletsAsin, "asin").write.parquet(getFilePath("fire_tablets"))
  }

  private def dumpAmazonSecurityCameras(amazonDevicesMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonSecurityMeta = selectAsin(
      amazonDevicesMeta.filter($"title".contains("Security"))
    )
    reviews.join(amazonSecurityMeta, "asin").write.parquet(getFilePath("security_cameras"))
  }

  private def dumpAmazonTap(amazonDevicesMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonTapMeta = selectAsin(
      amazonDevicesMeta.filter($"title".contains("Amazon Tap"))
    )
    reviews.join(amazonTapMeta, "asin").write.parquet(getFilePath("tap"))
  }

  private def dumpAmazonKindle(amazonDevicesMeta: DataFrame, reviews: DataFrame): Unit = {
    val amazonKindleMeta = selectAsin(
      amazonDevicesMeta.filter(
        $"title".contains("E-reader") ||
          $"title".contains("E Ink Display") ||
          $"title".contains("Wireless Reading Device") ||
          $"title".contains("Kindle Voyage") ||
          $"title".contains("Kindle Paperwhite") ||
          $"title".contains("Kindle Oasis")
      )
    )
    reviews.join(amazonKindleMeta, "asin").write.parquet(getFilePath("kindle"))
  }



  private def selectAsin(dataframe: DataFrame): DataFrame = {
    dataframe.select($"asin")
  }

  private def getFilePath(fileName: String) = {
    s"${inputs.outPath}/${fileName}"
  }
}
