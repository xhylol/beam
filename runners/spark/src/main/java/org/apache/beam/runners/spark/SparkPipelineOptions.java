/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.runners.spark;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.beam.runners.core.construction.PipelineResources;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.options.ApplicationNameOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.DefaultValueFactory;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.MoreObjects;

/**
 * Spark runner {@link PipelineOptions} handles Spark execution-related configurations, such as the
 * master address, batch-interval, and other user-related knobs.
 */
public interface SparkPipelineOptions
    extends PipelineOptions, StreamingOptions, ApplicationNameOptions {

  String DEFAULT_MASTER_URL = "local[4]";
  String DEFAULT_FILE_SYSTEM = "hdfs://172.17.117.203:9000";

  @Description("The url of the spark master to connect to, (e.g. spark://host:port, local[4]).")
  @Default.String(DEFAULT_MASTER_URL)
  String getSparkMaster();

  void setSparkMaster(String master);

  @Description("Shared file system.")
  @Default.String(DEFAULT_FILE_SYSTEM)
  String getFileSystem();

  void setFileSystem(String fileSystem);

  @Description("Batch interval for Spark streaming in milliseconds.")
  @Default.Long(500)
  Long getBatchIntervalMillis();

  void setBatchIntervalMillis(Long batchInterval);

  @Description("Batch default storage level")
  @Default.String("MEMORY_AND_DISK")
  String getStorageLevel();

  void setStorageLevel(String storageLevel);

  @Description("Minimum time to spend on read, for each micro-batch.")
  @Default.Long(200)
  Long getMinReadTimeMillis();

  void setMinReadTimeMillis(Long minReadTimeMillis);

  @Description("Max records per micro-batch. For streaming sources only.")
  @Default.Long(-1)
  Long getMaxRecordsPerBatch();

  void setMaxRecordsPerBatch(Long maxRecordsPerBatch);

  @Description(
      "A value between 0-1 to describe the percentage of a micro-batch dedicated "
          + "to reading from UnboundedSource.")
  @Default.Double(0.1)
  Double getReadTimePercentage();

  void setReadTimePercentage(Double readTimePercentage);

  @Description(
      "A checkpoint directory for streaming resilience, ignored in batch. "
          + "For durability, a reliable filesystem such as HDFS/S3/GS is necessary.")
  @Default.InstanceFactory(TmpCheckpointDirFactory.class)
  String getCheckpointDir();

  void setCheckpointDir(String checkpointDir);

  /**
   * Returns the default checkpoint directory of /tmp/${job.name}. For testing purposes only.
   * Production applications should use a reliable filesystem such as HDFS/S3/GS.
   */
  class TmpCheckpointDirFactory implements DefaultValueFactory<String> {
    @Override
    public String create(PipelineOptions options) {
      return "/tmp/" + options.as(SparkPipelineOptions.class).getJobName();
    }
  }

  @Description(
      "The period to checkpoint (in Millis). If not set, Spark will default "
          + "to Max(slideDuration, Seconds(10)). This PipelineOptions default (-1) will end-up "
          + "with the described Spark default.")
  @Default.Long(-1)
  Long getCheckpointDurationMillis();

  void setCheckpointDurationMillis(Long durationMillis);

  @Description(
      "If set bundleSize will be used for splitting BoundedSources, otherwise default to "
          + "splitting BoundedSources on Spark defaultParallelism. Most effective when used with "
          + "Spark dynamicAllocation.")
  @Default.Long(0)
  Long getBundleSize();

  @Experimental
  void setBundleSize(Long value);

  @Description("Enable/disable sending aggregator values to Spark's metric sinks")
  @Default.Boolean(true)
  Boolean getEnableSparkMetricSinks();

  void setEnableSparkMetricSinks(Boolean enableSparkMetricSinks);

  @Description(
      "If the spark runner will be initialized with a provided Spark Context. "
          + "The Spark Context should be provided with SparkContextOptions.")
  @Default.Boolean(false)
  boolean getUsesProvidedSparkContext();

  void setUsesProvidedSparkContext(boolean value);

  /**
   * List of local files to make available to workers.
   *
   * <p>Jars are placed on the worker's classpath.
   *
   * <p>The default value is the list of jars from the main program's classpath.
   */
  @Description(
      "Jar-Files to send to all workers and put on the classpath. "
          + "The default value is all files from the classpath.")
  List<String> getFilesToStage();

  void setFilesToStage(List<String> value);

  @Description(
      "Disable caching of reused PCollections for whole Pipeline."
          + " It's useful when it's faster to recompute RDD rather than save. ")
  @Default.Boolean(false)
  boolean isCacheDisabled();

  void setCacheDisabled(boolean value);

  /**
   * Classpath contains non jar files (eg. directories with .class files or empty directories) will
   * cause exception in running log. Though the {@link org.apache.spark.SparkContext} can handle
   * this when running in local master, it's better not to include non-jars files in classpath.
   */
  static void prepareFilesToStage(SparkPipelineOptions options) {
    List<String> filesToStage =
        options.getFilesToStage().stream()
            .map(File::new)
            .filter(File::exists)
            .map(
                file -> {
                  return file.getAbsolutePath();
                })
            .collect(Collectors.toList());
    options.setFilesToStage(
        PipelineResources.prepareFilesForStaging(
            filesToStage,
            MoreObjects.firstNonNull(
                options.getTempLocation(), System.getProperty("java.io.tmpdir"))));
  }
}
