package io.opentelemetry.agents;

import static io.opentelemetry.agents.AgentVersion.LATEST_VERSION;

public final class Agents {

  public final static Agent NONE = Agent.builder()
      .name("none")
      .description("No Instrumentation")
      .build();

  public final static Agent LATEST_UPSTREAM_SNAPSHOT = Agent.builder()
      .name("snapshot")
      .description("Latest available snapshot version from main")
      .build();

  private final static String SPLUNK_AGENT_URL =
      "https://github.com/signalfx/splunk-otel-java/releases/download/v" + LATEST_VERSION + "/splunk-otel-javaagent.jar";

  public final static Agent SPLUNK_OTEL = Agent.builder()
      .name("splunk-otel")
      .description("Splunk OpenTelemetry Java agent")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .build();

  public final static Agent SPLUNK_PROFILER = Agent.builder()
      .name("cpu:text")
      .description("Splunk OpenTelemetry Java agent with AlwaysOn Profiling")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .additionalJvmArgs("-Dsplunk.profiler.enabled=true")
      .build();

  private Agents() {
  }
}
