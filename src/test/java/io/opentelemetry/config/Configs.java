/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.config;

import io.opentelemetry.agents.Agents;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Defines all test configurations
 */
public enum Configs {

//  RELEASE(TestConfig.builder()
//      .name("devguy")
//      .description("just for troubleshooting")
//      .withAgents(Agent.SPLUNK_OTEL)
//      .totalIterations(15)
//      .build()),
  RELEASE(TestConfig.builder()
      .name("release_30vu_8500iter")
      .description("multiple agent configurations compared")
      .withAgents(Agents.NONE, Agents.SPLUNK_OTEL, Agents.SPLUNK_PROFILER)
      .totalIterations(8500)
      .warmupSeconds(60)
      .maxRequestRate(900)
      .concurrentConnections(30)
      .build());

  public final TestConfig config;

  public static Stream<TestConfig> all(){
    return Arrays.stream(Configs.values()).map(x -> x.config);
  }

  Configs(TestConfig config) {
    this.config = config;
  }
}
