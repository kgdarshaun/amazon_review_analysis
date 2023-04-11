package org.sfu.amazon_review_analysis.etl_review.input

case class InputArgs(
                      reviewFileIn: String,
                      reviewFileOut: String,
                      year: Int
                    )