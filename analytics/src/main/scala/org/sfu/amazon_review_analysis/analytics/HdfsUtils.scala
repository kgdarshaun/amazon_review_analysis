package org.sfu.amazon_review_analysis.analytics

import org.apache.hadoop.fs.FileSystem
import org.apache.spark.sql.SparkSession

class HdfsUtils(spark: SparkSession) {

  def getFileSystem() = {
    val conf = spark.sparkContext.hadoopConfiguration
    FileSystem.get(conf)
  }

}
