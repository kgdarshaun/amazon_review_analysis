#!/bin/bash

VERSION="1.0"

set -e

# Building Jars
cd ../etl_metadata
sbt clean compile assembly

cd ../etl_review
sbt clean compile assembly

cd ../analytics
sbt clean compile assembly

cd ../dump_amazon_devices_reviews
sbt clean compile assembly

cd ../scripts
mkdir amazon_analysis_code
# Copying all the Jars
cp ../etl_metadata/target/scala-2.12/amazon-etl-metadata-${VERSION}.jar ./amazon_analysis_code/
cp ../etl_review/target/scala-2.12/amazon-etl-review-${VERSION}.jar ./amazon_analysis_code/
cp ../analytics/target/scala-2.12/amazon-analytics-${VERSION}.jar ./amazon_analysis_code/
cp ../dump_amazon_devices_reviews/target/scala-2.12/dump-amazon-devices-reviews-${VERSION}.jar ./amazon_analysis_code/

# Copying all the scripts
cp dump_files.py ./amazon_analysis_code/
cp move_review_paritions.py ./amazon_analysis_code/

zip -r -m amazon_analysis_code.zip amazon_analysis_code