/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.config;

import io.opentelemetry.agents.Agent;

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
      .withAgents(Agent.NONE, Agent.SPLUNK_OTEL, Agent.SPLUNK_PROFILER)
//      .withAgents(Agent.SPLUNK_PROFILER, Agent.SPLUNK_PROFILER_PPROF, Agent.SPLUNK_PROFILER_TLAB, Agent.SPLUNK_PROFILER_TLAB_PPROF)
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
