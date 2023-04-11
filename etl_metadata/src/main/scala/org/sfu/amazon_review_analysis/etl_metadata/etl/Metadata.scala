package org.sfu.amazon_review_analysis.etl_metadata.etl

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.sfu.amazon_review_analysis.etl_metadata.utils.UDF.removeDollarSign
import org.sfu.amazon_review_analysis.etl_metadata.input.InputArgs
import org.sfu.amazon_review_analysis.etl_metadata.schemas.{Metadata => MetadataSchema}

class Metadata(spark: SparkSession) {

  import spark.implicits._

  def process(inputs: InputArgs): Unit = {
    val metadataDf = spark.read.json(inputs.metaFileIn)

    val metadata = metadataDf.select($"asin", $"title", $"brand", $"main_cat", $"price")

    //Cleaning category column
    val categoryCleaned = metadata.withColumn("main_cat", when($"main_cat".startsWith("<img"), regexp_extract($"main_cat", ("alt=\"[A-Za-z ]*"), 0)).otherwise($"main_cat"))
      .withColumn("main_cat", when($"main_cat".startsWith("alt"), $"main_cat".substr(lit(6), length($"main_cat")-1)).otherwise($"main_cat"))
      .withColumn("main_cat", regexp_replace($"main_cat", "&amp;", "&"))
      .filter($"main_cat".isNotNull)

    //Cleaning price column
    val priceCleaned = categoryCleaned.withColumn("price", removeDollarSign($"price"))
      .withColumn("split_price", when($"price".contains("-"), split($"price", "-")))
      .withColumn("price", when($"price".isNull, 0).when($"price" === "", 0).when($"price".rlike("[A-Za-z]"), 0).when($"price".contains("-"), round(($"split_price"(0).cast("double")+$"split_price"(1).cast("double"))/2, 2)).otherwise($"price".cast("double")))
      .drop($"split_price")

    //Cleaned brand
    val brandCleaned = priceCleaned.filter($"brand" =!= "")

    brandCleaned.as[MetadataSchema]
      .write
      .partitionBy("main_cat")
      .parquet(inputs.metaFileOut)
  }

}
