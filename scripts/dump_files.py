import sys
import argparse
assert sys.version_info >= (3, 5) # make sure we have Python 3.5+

from pyspark.sql import SparkSession, functions, types

def main(inputs):
    metadata = spark.read.parquet(inputs.meta_file)
    reviews = spark.read.parquet(inputs.review_file)

    reviews_meta = reviews.join(metadata, "asin").cache()

    filtered_df = reviews_meta.filter(f"main_cat == \"{inputs.category}\"") \
                                    .filter(f"brand == \"{inputs.brand}\"")
    
    filtered_df.write.parquet(inputs.out_file)


def parse_input_arguments():
    parser = argparse.ArgumentParser(
                    prog = 'Parquet file dumper',
                    description = 'Dump parquet Files with filters')

    parser.add_argument('-m', '--meta_file', help="input metadata file path")
    parser.add_argument('-r', '--review_file', help="input review file path")
    parser.add_argument('-c', '--category', help="category name to be filtered")
    parser.add_argument('-b', '--brand', help="brand name to be filtered")
    parser.add_argument('-o', '--out_file', help="output file name with path")
    args, unknown_args = parser.parse_known_args()
    return args



if __name__ == '__main__':
    inputs = parse_input_arguments()
    spark = SparkSession.builder.appName('dump file').getOrCreate()
    assert spark.version >= '3.0' # make sure we have Spark 3.0+
    spark.sparkContext.setLogLevel('WARN')
    sc = spark.sparkContext
    main(inputs)
