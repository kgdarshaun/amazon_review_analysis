package org.sfu.amazon_review_analysis.etl_review.schema

import java.sql.Date

case class Review(
                   asin: String,
                   overall: Double,
                   reviewText: String,
                   reviewTime: Date,
                   reviewerID: String,
                   reviewerName: String,
                   summary: String,
                   unixReviewTime: Long,
                   verified: Boolean,
                   vote: String
                 )

//style: Map[String, String],
