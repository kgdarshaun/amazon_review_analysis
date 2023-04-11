import sys
import argparse
assert sys.version_info >= (3, 5) # make sure we have Python 3.5+

from pyspark.sql import SparkSession

def main(inputs):
    FileSystem = spark._jvm.org.apache.hadoop.fs.FileSystem
    Path = spark._jvm.org.apache.hadoop.fs.Path

    fs = FileSystem.get(spark._jsc.hadoopConfiguration())
    directories = fs.listStatus(Path(inputs.path))

    to_delete_directories = []
    for directory_status in directories:
        directory = directory_status.getPath()
        if "=" in str(directory):
            continue
        to_delete_directories.append(directory)
        files = fs.listStatus(directory)
        for file_status in files:
            if file_status.isDirectory():
                filePath = str(file_status.getPath())
                fileName = filePath.split("/")[-1]
                fs.rename(
                    file_status.getPath(),
                    Path("/".join((inputs.path, fileName)))
                )

    for directory in to_delete_directories:
        fs.delete(directory, True)

    fs.create(Path("/".join((inputs.path, "_SUCCESS"))), True)

def parse_input_arguments():
    parser = argparse.ArgumentParser(
        prog = 'move review partitions',
        description = 'move review partitions for each year to one single folder')

    parser.add_argument('-p', '--path', help="path containing different year review data")
    args, unknown_args = parser.parse_known_args()
    return args



if __name__ == '__main__':
    inputs = parse_input_arguments()
    spark = SparkSession.builder.appName('move review partitions').getOrCreate()
    assert spark.version >= '3.0' # make sure we have Spark 3.0+
    spark.sparkContext.setLogLevel('WARN')
    sc = spark.sparkContext
    main(inputs)
