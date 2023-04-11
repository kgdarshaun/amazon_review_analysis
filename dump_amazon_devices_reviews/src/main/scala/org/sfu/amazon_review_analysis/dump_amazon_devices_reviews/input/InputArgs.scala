package org.sfu.amazon_review_analysis.dump_amazon_devices_reviews.input

case class InputArgs(
                      metadata: String,
                      review: String,
                      outPath: String
                    )