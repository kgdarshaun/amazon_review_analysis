package org.sfu.amazon_review_analysis.etl_metadata.utils

import org.apache.spark.sql.functions.udf

object UDF {
  val removeDollarSign = udf((line: String) => line.replace("$", ""))
}
