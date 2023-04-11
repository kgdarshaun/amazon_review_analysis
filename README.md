# Amazon Customer Review Analysis

This project is to clean, perform ETL and dump analytics for the amazon review [dataset](https://nijianmo.github.io/amazon/index.html). The data set contains 2 large json files - metadata and review. The metadata contains the metadata of the amazon product and review contains the original review data. The schema of the both data sets can be found in the link tagged above.

## Visualization
The visualization of all the analytics has been hosted to public in [Tableau Public cloud](https://public.tableau.com/app/profile/gauri.dilip.mane/viz/AmazonReviews_16706177505000/SentimentalAnalysisofAmazonBrandCustomerReview2017-18) -

## Prerequisites
In order to build project, you need to have the following:
- Java (version >= 11)
- Scala (version >= 2.12.12)
- SBT ( version = 1.8.0)

It would be **preferred** to open the project in [IntelliJ](https://www.jetbrains.com/idea/download/#section=windows) for easy build and compile.

## Modules (in order of execution):
### etl_metadata
- This module contains code to perform ETL on the amazon product metadata.
- The input to the module will be the metadata json file (compressed or un-compressed) and the output will be parquet files (snappy compressed) and partitioned by category.
- It takes various parameters as input. Information of parameters can be found below

| Argument  | Short-Hand | Description               | Required |
|-----------|------------|---------------------------|----------|
| --metaIn  | -i         | Input Metadata File Path  | True     |
| --metaOut | -o         | Output Metadata File Path | True     |

> **Note:** You can always provide --help / -h to get more details about the jar and input arguments

- Example of running the jar
```commandline
spark-submit amazon-etl-metadata-1.0.jar \
--metaIn hdfs:///dataset/metadata.json \
--metaOut hdfs:///dataset/metadata
     
# OR

spark-submit amazon-etl-metadata-1.0.jar \
-i hdfs:///dataset/metadata.json \
-o hdfs:///dataset/metadata
```

### etl_review
- This module contains code for performing ETL on amazon review data. The module supports 1-year data extraction or the complete years data extraction.
- The input to the module will be amazon data in json format (compressed / un-compressed) and the output will be parquet files (snappy compressed) partitioned by year and month of the review.
- Details about the input arguments to the spark job can be found below.

| Argument    | Short-Hand | Description             | Required         |
|-------------|------------|-------------------------|------------------|
| --reviewIn  | -i         | Input Review File Path  | True             |
| --reviewOut | -o         | Output Review File Path | True             |
| --year      | -y         | ETL for specific year   | False (Optional) |

> **Note:** You can always provide --help / -h to get more details about the jar and input arguments

- Example of running the jar
```commandline
spark-submit amazon-etl-review-1.0.jar \
     --reviewIn hdfs:///dataset/review.json \
     --reviewOut hdfs:///dataset/review \
     [--year 2018]

spark-submit amazon-etl-review-1.0.jar \
    -i hdfs:///dataset/review.json \
    -o hdfs:///dataset/review \
    [-y 2018]
```

### analytics
- This module is to build descriptive analytics over the processed metadata and review data from the above steps/modules.
- Input to the module comprises metadata and review parquet file paths (preferably snappy compressed) and the output of the module will be a bunch of json files (not part files but single json files) which consist of aggregated data for visualisation.
- The input arguments for this spark-job can be found below

| Argument   | Short-Hand | Description        | Required |
|------------|------------|--------------------|----------|
| --metadata | -m         | Metadata File Path | True     |
| --review   | -r         | Review File Path   | True     |
| --outPath  | -o         | Output File Path   | True     |

> **Note:** You can always provide --help / -h to get more details about the jar and input arguments

- Example of running the jar
```commandline
spark-submit amazon-analytics-1.0.jar \
     --metadata hdfs:///dataset/metadata \
     --review hdfs:///dataset/review \
     --outPath hdfs:///analytics

# OR

spark-submit amazon-analytics-1.0.jar \
     --m hdfs:///dataset/metadata \
     --r hdfs:///dataset/review \
     --o hdfs:///analytics \
```

### dump_amazon_devices_review
- This module is to dump all the review data (merged with metadata) of amazon devices. These reviews will be used as input for sentimental analysis and further metric constructions.
- The amazon devices include:
  - Echo
  - Fire TV Stick
  - Fire TV
  - Fire Tablets
  - Security system (cameras)
  - Tap
  - Kindle
- The input to the module includes path to both metadata and review processed parquet files (from first 2 modules) and the output is parquet files for each of the amazon devices containing reviews (snappy compressed)
- Following are the input arguments expected by the spark-job:

| Argument   | Short-Hand | Description        | Required |
|------------|------------|--------------------|----------|
| --metadata | -m         | Metadata File Path | True     |
| --review   | -r         | Review File Path   | True     |
| --outPath  | -o         | Output File Path   | True     |

> **Note:** You can always provide --help / -h to get more details about the jar and input arguments

- Example of running the jar
```commandline
spark-submit dump-amazon-devices-reviews-1.0.jar \
     --metadata hdfs:///dataset/metadata \
     --review hdfs:///dataset/review \
     --outPath hdfs:///amazon_device_reviews

# OR 

spark-submit amazon-analytics-1.0.jar \
     --m hdfs:///dataset/metadata \
     --r hdfs:///dataset/review \
     --o hdfs:///amazon_devices_reviews
```

## Build and deployment
### Build
- Running build.sh from scripts folder would create a zip file called "amazon_analysis_code" which basically contains all the uber/fat jard of all modules and scripts.
```commandline
cd scripts/
chmod u+x build.sh
./build.sh
```
- We can scp this zip file to target destination and unzip using the command
```commandline
scp amazon_analysis_code.zip <target>
unzip amazon_analysis_code.zip && rm amazon_analysis_code.zip
```

### Deployment
Under Construction

## Scripts
This module contains scripts for short and simple tasks.

### dump_files.py
- This script is to dump amazon review data for particular category and brand.
- The input for this script consists of processed parquet path of metadata and review, and outputs parquet files of particular category and brand.
- The input arguments for the script are as follows:

| Argument      | Short-Hand | Description                  | Required |
|---------------|------------|------------------------------|----------|
| --meta_file   | -m         | input metadata file path     | True     |
| --review_file | -r         | input review file path       | True     |
| --category    | -c         | category name to be filtered | True     |
| --brand       | -b         | brand name to be filtered    | True     |
| --out_file    | -o         | output file name with path   | True     |

> **Note:** You can always provide --help / -h to get more details about the script and input arguments

- Example of running the jar
```commandline
spark-submit dump_files.py \
     --meta_file hdfs:///dataset/metadata \
     --review_file hdfs:///dataset/review \
     --category "All Electronics" \
     --brand Samsung \
     --out_file hdfs:///samsung_reviews

# OR 

spark-submit dump_files.py \
     -m hdfs:///dataset/metadata \
     -r hdfs:///dataset/review \
     -c "All Electronics" \
     -b Samsung \
     -o hdfs:///samsung_reviews
```

### move_review_partitions.py
- This script is to move individual year preocessed review data to one common folder.
- _This utils script will be used only when different year review data are extracted separately._
- The input for this script consists of processed parquet path of reviews, and does not output anything. It just moves the folders to right path.
- The input arguments for the script are as follows:

| Argument | Short-Hand | Description                                | Required |
|----------|------------|--------------------------------------------|----------|
| --path   | -p         | path containing different year review data | True     |

> **Note:** You can always provide --help / -h to get more details about the script and input arguments

- Example of running the jar
```commandline
spark-submit move_review_partitions.py \
     --path hdfs:///dataset/reviews \

# OR 

spark-submit move_review_paritions.py \
     -p hdfs:///dataset/reviews \
```