package io.opentelemetry.agents;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.opentelemetry.agents.AgentVersion.LATEST_VERSION;

public class Agent {

  public final static Agent NONE = new Agent("none", "no agent at all", null);
  public final static Agent LATEST_UPSTREAM_SNAPSHOT = new Agent("snapshot", "latest available snapshot version from main");

  private final static String SPLUNK_AGENT_URL =
      "https://github.com/signalfx/splunk-otel-java/releases/download/v" + LATEST_VERSION + "/splunk-otel-javaagent.jar";

  public final static Agent SPLUNK_PROFILER_PPROF_10s = new Agent("full-pprof:0.1Hz", LATEST_VERSION + "full-pprof:0.1Hz",
      SPLUNK_AGENT_URL,
      List.of("-Dsplunk.profiler.enabled=true", "-Dsplunk.profiler.memory.enabled=true", "-Dsplunk.profiler.cpu.data.format=pprof-gzip-base64"));

  public final static Agent SPLUNK_PROFILER_PPROF_1Hz = new Agent("full-pprof:1Hz", LATEST_VERSION + "full-pprof:1Hz",
      SPLUNK_AGENT_URL,
      List.of("-Dsplunk.profiler.enabled=true", "-Dsplunk.profiler.call.stack.interval=1000", "-Dsplunk.profiler.memory.enabled=true",
              "-Dsplunk.profiler.cpu.data.format=pprof-gzip-base64"));

  public final static Agent SPLUNK_PROFILER_PPROF_10Hz = new Agent("full-pprof:10Hz", LATEST_VERSION + "full-pprof:10Hz",
      SPLUNK_AGENT_URL,
      List.of("-Dsplunk.profiler.enabled=true", "-Dsplunk.profiler.call.stack.interval=100", "-Dsplunk.profiler.memory.enabled=true",
              "-Dsplunk.profiler.cpu.data.format=pprof-gzip-base64"));

  private final String name;
  private final String description;
  private final URL url;
  private final List<String> additionalJvmArgs;

  public Agent(String name, String description) {
    this(name, description, null);
  }

  public Agent(String name, String description, String url) {
    this(name, description, url, Collections.emptyList());
  }

  public Agent(String name, String description, String url, List<String> additionalJvmArgs) {
    this.name = name;
    this.description = description;
    this.url = makeUrl(url);
    this.additionalJvmArgs = new ArrayList<>(additionalJvmArgs);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public boolean hasUrl() {
    return url != null;
  }

  public URL getUrl() {
    return url;
  }

  public List<String> getAdditionalJvmArgs() {
    return Collections.unmodifiableList(additionalJvmArgs);
  }

  private static URL makeUrl(String url) {
    try {
      if (url == null) return null;
      return URI.create(url).toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error parsing url", e);
    }
  }
}
