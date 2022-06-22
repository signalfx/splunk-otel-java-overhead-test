package io.opentelemetry.agents;

import static io.opentelemetry.agents.AgentVersion.LATEST_VERSION;

public final class Agents {

  public final static Agent NONE = new Agent.Builder()
      .name("none")
      .description("No Instrumentation")
      .build();

  public final static Agent LATEST_UPSTREAM_SNAPSHOT = new Agent.Builder()
      .name("snapshot")
      .description("Latest available snapshot version from main")
      .build();

  private final static String SPLUNK_AGENT_URL =
      "https://github.com/signalfx/splunk-otel-java/releases/download/v" + LATEST_VERSION + "/splunk-otel-javaagent.jar";

  public final static Agent SPLUNK_OTEL = new Agent.Builder()
      .name("splunk-otel")
      .description("Splunk OpenTelemetry Java agent")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .build();

  public final static Agent SPLUNK_PROFILER = new Agent.Builder()
      .name("cpu:text")
      .description("Splunk OpenTelemetry Java agent with AlwaysOn Profiling")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .additionalJvmArgs("-Dsplunk.profiler.enabled=true")
      .build();

  public final static Agent SPLUNK_PROFILER_PPROF_10Hz = new Agent.Builder()
      .name("full-pprof:10Hz")
      .description("Splunk OpenTelemetry Java agent with AlwaysOn Profiling; 10Hz frequency")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .additionalJvmArgs(
          "-Dsplunk.profiler.enabled=true",
          "-Dsplunk.profiler.call.stack.interval=100",
          "-Dsplunk.profiler.memory.enabled=true",
          "-Dsplunk.profiler.cpu.data.format=pprof-gzip-base64")
      .build();

  public final static Agent SPLUNK_PROFILER_PPROF_1Hz = new Agent.Builder()
      .name("full-pprof:1Hz")
      .description("Splunk OpenTelemetry Java agent with AlwaysOn Profiling; 1Hz frequency")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .additionalJvmArgs(
          "-Dsplunk.profiler.enabled=true",
          "-Dsplunk.profiler.call.stack.interval=1000",
          "-Dsplunk.profiler.memory.enabled=true",
          "-Dsplunk.profiler.cpu.data.format=pprof-gzip-base64")
      .build();

  public final static Agent SPLUNK_PROFILER_PPROF_10s = new Agent.Builder()
      .name("full-pprof:0.1Hz")
      .description("Splunk OpenTelemetry Java agent with AlwaysOn Profiling; 0.1Hz frequency")
      .version(LATEST_VERSION)
      .url(SPLUNK_AGENT_URL)
      .additionalJvmArgs(
          "-Dsplunk.profiler.enabled=true",
          "-Dsplunk.profiler.memory.enabled=true",
          "-Dsplunk.profiler.cpu.data.format=pprof-gzip-base64")
      .build();

  private Agents() {
  }
}
