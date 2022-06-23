package io.opentelemetry.agents;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Agent {

  public static Builder builder() {
    return new Builder();
  }

  private final String name;
  private final String description;
  private final String version;
  private final URL url;
  private final List<String> additionalJvmArgs;

  Agent(Builder builder) {
    this.name = builder.name;
    this.description = builder.description;
    this.version = builder.version;
    this.url = builder.url;
    this.additionalJvmArgs = builder.additionalJvmArgs;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getVersion() {
    return version;
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

  public static final class Builder {

    private String name;
    private String description;
    private String version;
    private URL url;
    private List<String> additionalJvmArgs = Collections.emptyList();

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder version(String version) {
      this.version = version;
      return this;
    }

    public Builder url(String url) {
      if (url != null) {
        try {
          this.url = URI.create(url).toURL();
        } catch (MalformedURLException e) {
          throw new RuntimeException("Error parsing url", e);
        }
      }
      return this;
    }

    public Builder additionalJvmArgs(String... additionalJvmArgs) {
      this.additionalJvmArgs = List.of(additionalJvmArgs);
      return this;
    }

    public Agent build() {
      return new Agent(this);
    }
  }
}
