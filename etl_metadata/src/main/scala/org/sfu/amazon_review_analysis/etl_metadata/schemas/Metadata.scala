package org.sfu.amazon_review_analysis.etl_metadata.schemas

case class Metadata(
                     asin: String,
                     title: String,
                     brand: String,
                     main_cat: String,
                     price: Double
                   )

//                     category: Seq[String]
//
//                     date: String,
//                     description: Seq[String],
//                     feature: Seq[String],
//
//                     fit: String,
//                     also_buy: Seq[String],
//                     tech2: String,
//
//                     rank: String,
//                     also_view: Seq[String],
//                     //details: Map[String, String],
//
//                     similar_item: String,
//
//
//
//                   //not needed
//                     imageURL: Seq[String],
//                     imageURLHighRes: Seq[String]


