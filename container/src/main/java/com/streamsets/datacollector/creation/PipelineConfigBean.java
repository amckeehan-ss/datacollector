/**
 * Copyright 2015 StreamSets Inc.
 *
 * Licensed under the Apache Software Foundation (ASF) under one
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
package com.streamsets.datacollector.creation;

import com.streamsets.datacollector.config.DeliveryGuarantee;
import com.streamsets.datacollector.config.DeliveryGuaranteeChooserValues;
import com.streamsets.datacollector.config.ErrorHandlingChooserValues;
import com.streamsets.datacollector.config.ExecutionModeChooserValues;
import com.streamsets.datacollector.config.MemoryLimitExceeded;
import com.streamsets.datacollector.config.MemoryLimitExceededChooserValues;
import com.streamsets.datacollector.config.PipelineGroups;
import com.streamsets.datacollector.config.PipelineState;
import com.streamsets.datacollector.config.PipelineStateChooserValues;
import com.streamsets.pipeline.api.ConfigDef;
import com.streamsets.pipeline.api.ConfigGroups;
import com.streamsets.pipeline.api.ExecutionMode;
import com.streamsets.pipeline.api.GenerateResourceBundle;
import com.streamsets.pipeline.api.MultiValueChooserModel;
import com.streamsets.pipeline.api.Stage;
import com.streamsets.pipeline.api.StageDef;
import com.streamsets.pipeline.api.ValueChooserModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// we are using the annotation for reference purposes only.
// the annotation processor does not work on this maven project
// we have a hardcoded 'datacollector-resource-bundles.json' file in resources
@GenerateResourceBundle
@StageDef(
    version = PipelineConfigBean.VERSION,
    label = "Pipeline",
    upgrader = PipelineConfigUpgrader.class,
    onlineHelpRefUrl = "not applicable"
)
@ConfigGroups(PipelineGroups.class)
public class PipelineConfigBean implements Stage {

  public static final int VERSION = 4;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "Execution Mode",
      defaultValue= "STANDALONE",
      displayPosition = 10
  )
  @ValueChooserModel(ExecutionModeChooserValues.class)
  public ExecutionMode executionMode;

  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue="AT_LEAST_ONCE",
      label = "Delivery Guarantee",
      displayPosition = 20
  )
  @ValueChooserModel(DeliveryGuaranteeChooserValues.class)
  public DeliveryGuarantee deliveryGuarantee;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.BOOLEAN,
    defaultValue = "true",
    label = "Retry Pipeline on Error",
    displayPosition = 30)
  public boolean shouldRetry;

  @ConfigDef(
    required = false,
    type = ConfigDef.Type.NUMBER,
    defaultValue = "-1",
    label = "Retry Attempts",
    dependsOn = "shouldRetry",
    triggeredByValue = "true",
    description = "Max no of retries. To retry indefinitely, use -1. The wait time between retries starts at 15 seconds"
      + " and doubles until reaching 5 minutes.",
    displayPosition = 30)
  public int retryAttempts;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.NUMBER,
    label = "Max Pipeline Memory (MB)",
    defaultValue = "${jvm:maxMemoryMB() * 0.65}",
    description = "Maximum amount of memory the pipeline can use. Configure in relationship to the SDC Java heap " +
      "size. The default is 650 and a value of 0 or less disables the limit.",
    displayPosition = 60,
    min = 128,
    group = ""
  )
  public long memoryLimit;


  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      defaultValue="STOP_PIPELINE",
      label = "On Memory Exceeded",
      description = "Behavior when the pipeline exceeds the memory limit. Tip: Configure an alert to indicate when the " +
        "memory use approaches the limit." ,
      displayPosition = 70,
      group = ""
  )
  @ValueChooserModel(MemoryLimitExceededChooserValues.class)
  public MemoryLimitExceeded memoryLimitExceeded;

  @ConfigDef(
    required = false,
    type = ConfigDef.Type.MODEL,
    defaultValue = "[\"RUN_ERROR\", \"STOPPED\", \"FINISHED\"]",
    label = "Notify on Pipeline State Changes",
    description = "Notifies via email when pipeline gets to the specified states",
    displayPosition = 75,
    group = ""
  )
  @MultiValueChooserModel(PipelineStateChooserValues.class)
  public List<PipelineState> notifyOnStates;

  @ConfigDef(
    required = false,
    type = ConfigDef.Type.LIST,
    defaultValue = "[]",
    label = "Email IDs",
    description = "Email Addresses",
    displayPosition = 76,
    group = ""
  )
  public List<String> emailIDs;

  @ConfigDef(
      required = false,
      defaultValue = "{}",
      type = ConfigDef.Type.MAP,
      label = "Constants",
      displayPosition = 80,
      group = "CONSTANTS"
  )
  public Map<String, Object> constants;


  @ConfigDef(
      required = true,
      type = ConfigDef.Type.MODEL,
      label = "Error Records",
      displayPosition = 90,
      group = "BAD_RECORDS"
  )
  @ValueChooserModel(ErrorHandlingChooserValues.class)
  public String badRecordsHandling;


  @ConfigDef(
      required = true,
      type = ConfigDef.Type.NUMBER,
      label = "Worker Memory (MB)",
      defaultValue = "1024",
      displayPosition = 100,
      group = "CLUSTER",
      dependsOn = "executionMode",
      triggeredByValue = {"CLUSTER_BATCH", "CLUSTER_YARN_STREAMING"}
  )
  public long clusterSlaveMemory;


  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    label = "Worker Java Options",
    defaultValue = "-XX:PermSize=128M -XX:MaxPermSize=256M -Dlog4j.debug",
    description = "Add properties as needed. Changes to default settings are not recommended.",
    displayPosition = 110,
    group = "CLUSTER",
    dependsOn = "executionMode",
    triggeredByValue = {"CLUSTER_BATCH", "CLUSTER_YARN_STREAMING"}
  )
  public String clusterSlaveJavaOpts;


  @ConfigDef(
    required = false,
    type = ConfigDef.Type.MAP,
    defaultValue = "{}",
    label = "Launcher ENV",
    description = "Sets additional environment variables for the cluster launcher",
    displayPosition = 120,
    group = "CLUSTER",
    dependsOn = "executionMode",
    triggeredByValue = {"CLUSTER_BATCH", "CLUSTER_YARN_STREAMING"}
  )
  public Map clusterLauncherEnv;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    label = "Mesos Dispatcher URL",
    description = "URL for service which launches Mesos framework",
    displayPosition = 130,
    group = "CLUSTER",
    dependsOn = "executionMode",
    triggeredByValue = {"CLUSTER_MESOS_STREAMING"}
  )
  public String mesosDispatcherURL;

  @ConfigDef(
    required = true,
    type = ConfigDef.Type.STRING,
    label = "Hadoop/S3 Configuration Directory",
    description = "A directory under SDC resources directory to load core-site.xml and hdfs-site.xml files " +
      "to configure the Hadoop or S3 file system",
    displayPosition = 150,
    group = "CLUSTER",
    dependsOn = "executionMode",
    triggeredByValue = {"CLUSTER_MESOS_STREAMING"}
  )
  public String hdfsS3ConfDir;

  @Override
  public List<ConfigIssue> init(Info info, Context context) {
    return Collections.emptyList();
  }

  @Override
  public void destroy() {
  }

}
