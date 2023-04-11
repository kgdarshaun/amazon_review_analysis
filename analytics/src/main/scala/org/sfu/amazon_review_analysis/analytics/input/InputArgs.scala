package org.sfu.amazon_review_analysis.analytics.input

case class InputArgs(
                      metadataFile: String,
                      reviewFile: String,
                      outPath: String
                    )