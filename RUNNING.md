# Amazon Customer Review Analysis

This document is to provide a runbook to run all the aspects of the project.

## Build and Deploy
- The complete project can be built and all artifacts can be bundled by running the build.sh script from scripts folder.
- Once the command is run, your get a zip file names "amazon_analysis_code.zip".
- scp/cp the code to required target and unzip the zipped file.
- The code is available at sfu cluster edgenode at - /home/dka101/project/amazon_analysis_code

## ETL
- There are 2 ETLs which need to be run to preprocess the data in sfu cluster.

### Metadata ETL
- This performs the etl for amazon product metadata.
- The input for this etl can be found at - hdfs:///user/dka101/project/original/metadata.json.gz
- The output for the same can be found at - hdfs:///user/dka101/project/etl/metadata
- The below command was used to perform the ETL

```commandline
spark-submit amazon-etl-metadata-1.0.jar \
     --metaIn hdfs:///user/dka101/project/original/metadata.json.gz \
     --metaOut hdfs:///user/dka101/project/etl/metadata
```
> **Note:** metaOut path already contains the processed data. Please change it in order to run the ETL

### Review ETL
- This performs the etl for amazon product metadata.
- The input for this etl can be found at - hdfs:///user/dka101/project/original/metadata.json.gz
- The output for the same can be found at - hdfs:///user/dka101/project/etl/metadata
- The below command was used to perform the ETL
 
> **!Caution:** The cluster executor does not have enough memory to handle the shuffle operations of 100+ GB of review data. So please provide year filter and run the etl multiple times. 

```commandline
spark-submit amazon-etl-review-1.0.jar \
     --reviewIn hdfs:///user/dka101/project/original/review.json.gz \
     --reviewOut hdfs:///user/dka101/project/etl/review
     --year 2018
```
> **Note:** metaOut path already contains the processed data. Please change it in order to run the ETL

#### Moving review data partitions
- If we have run the ETL for multiple years, the parquets would have be split and written in multiple folders, so we run the script "move_review_partitions.py" to move all the partitions to one parent folder - review.
- Run the script like following

```commandline
spark-submit move_review_paritions.py --path hdfs:///user/dka101/project/etl/review
```

## Descriptive Analytics
- Once we have pre-processed metadata and review data, we can run analytics and dump json files.
- The inputs for this spark job can be found at
  - metadata = hdfs:///user/dka101/project/etl/metadata
  - review = hdfs:///user/dka101/project/etl/review
- The output folder for the same can be found at - hdfs:///user/dka101/project/analytics (This was run over the complete set of data ie. 100+GB of review data)
- Please use the following example command to run the spark job

```commandline
spark-submit amazon-analytics-1.0.jar \
     --metadata hdfs:///user/dka101/project/etl/metadata \
     --review hdfs:///user/dka101/project/etl/review \
     --outPath hdfs:///user/dka101/project/analytics
```
> **Note:** the outPath already exists, so please change it while running.

> **!Caution:** The review input data contains all the years which is 100+ GB of data. Please use spark configuration to limit number of executors.

## Dump amazon device reviews for sentimental analysis
- This is a pre-processing step to filter out amazon device data for sentimental analysis.
- The inputs for this spark job exists at :
  - metadata = hdfs:///user/dka101/project/etl/metadata
  - review = hdfs:///user/dka101/project/etl/review
- The output folder containing the parquets for various amazon devices can be found at - hdfs:///user/dka101/project/amazon_device_reviews (This contains the complete set of data over all the years)
- The spark job can be run using the example below

```commandline
spark-submit dump-amazon-devices-reviews-1.0.jar \
     --metadata hdfs:///user/dka101/project/etl/metadata \
     --review hdfs:///user/dka101/project/etl/review \
     --outPath hdfs:///user/dka101/project/amazon_device_reviews
```
> **Note:** the outPath already exists, so please change it while running.

## Dump Brand reviews for sentimental analysis
- This is another pre-processing step to dump reviews of each specific brand so sentimental analysis can be performed over that.
- The inputs of the spark job are available at:
  - metadata = hdfs:///user/dka101/project/etl/metadata
  - review = hdfs:///user/dka101/project/etl/review
- The output folder containing the parquet for each of the brand can be found at - 
- The spark job can be triggered for each brand using the sample below:

```commandline
spark-submit dump_files.py \
    --meta_file hdfs:///user/dka101/project/etl/metadata \
    --review_file hdfs:///user/dka101/project/etl/review \
    --category "All Electronics" \
    --brand Samsung \
    --out_file hdfs:///user/dka101/project/brand_reviews/Samsung
```
> **Note:** the out_file already exists, so please change it while running.

## Sentimental Analysis on Google Colaboratory
- As the spark-nlp library is throwing error in sfu cluster, The sentimental analysis is run in Google Colaboratory.
- The data used for sentimental analysis is also reduced to 2 years 2017-2018 to perform operations in faster pace.
- The instructions to run the Colaboratory has been provided in the same [notebook](https://colab.research.google.com/drive/1RQy5NA-5XbdMzdL-UbU0xMmO_AD-fyzP?usp=sharing).
- The input data required for the sentimental analysis is hosted in [google cloud drive](https://drive.google.com/drive/folders/1DM8n_IH3nowWOire8oSoOgIEedrg3ycS?usp=share_link)