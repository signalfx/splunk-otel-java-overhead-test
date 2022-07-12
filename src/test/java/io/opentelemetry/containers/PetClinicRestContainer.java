/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.containers;

import io.opentelemetry.agents.Agent;
import io.opentelemetry.agents.AgentResolver;
import io.opentelemetry.util.NamingConventions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PetClinicRestContainer {

  private static final Logger logger = LoggerFactory.getLogger(PetClinicRestContainer.class);
  private static final int PETCLINIC_PORT = 9966;
  private final AgentResolver agentResolver = new AgentResolver();

  private final Network network;
  private final Startable collector;
  private final Agent agent;
  private final NamingConventions namingConventions;
  private final String postgresHost;
  private final String collectorHost;

  public PetClinicRestContainer(Network network, Agent agent, NamingConventions namingConventions, String postgresHost, String collectorHost) {
    this(network, null, agent, namingConventions, postgresHost, collectorHost);
  }

  public PetClinicRestContainer(Network network, Startable collector, Agent agent, NamingConventions namingConventions) {
    this(network, collector, agent, namingConventions, "postgres", "collector");
  }

  public PetClinicRestContainer(Network network, Startable collector, Agent agent, NamingConventions namingConventions,
                                String postgresHost, String collectorHost) {
    this.network = network;
    this.collector = collector;
    this.agent = agent;
    this.namingConventions = namingConventions;
    this.postgresHost = postgresHost;
    this.collectorHost = collectorHost;
  }

  public GenericContainer<?> build() throws Exception {

    Optional<Path> agentJar = agentResolver.resolve(this.agent);

    GenericContainer<?> container = new GenericContainer<>(
            DockerImageName.parse("ghcr.io/open-telemetry/opentelemetry-java-instrumentation/petclinic-rest-base:20220711201901"))
            .withNetwork(network)
            .withNetworkAliases("petclinic")
            .withLogConsumer(new Slf4jLogConsumer(logger))
            .withExposedPorts(PETCLINIC_PORT)
            .withFileSystemBind(namingConventions.localResults(), namingConventions.containerResults())
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("overhead.jfc"), "/app/overhead.jfc")
            .waitingFor(Wait.forHttp("/petclinic/actuator/health").forPort(PETCLINIC_PORT))
            .withEnv("spring_profiles_active", "postgresql,spring-data-jpa")
            .withEnv("spring_datasource_url", "jdbc:postgresql://" + postgresHost + ":5432/" + PostgresContainer.DATABASE_NAME)
            .withEnv("spring_datasource_username", PostgresContainer.USERNAME)
            .withEnv("spring_datasource_password", PostgresContainer.PASSWORD)
            .withEnv("spring.datasource.hikari.maximum-pool-size", "30")
            .withEnv("spring_jpa_hibernate_ddl-auto", "none")
            .withCommand(buildCommandline(agentJar));

    if(collector != null){
      container = container.dependsOn(collector);
    }

    GenericContainer<?> gc = container;
    agentJar.ifPresent(
        agentPath -> gc.withCopyFileToContainer(
            MountableFile.forHostPath(agentPath),
            "/app/" + agentPath.getFileName().toString())
    );
    return container;
  }

  @NotNull
  private String[] buildCommandline(Optional<Path> agentJar) {
    String collectorUrl = collector == null ?
            "http://" + collectorHost + ":4317"
            :
            "http://collector:4317";
    List<String> result = new ArrayList<>(Arrays.asList(
        "java",
        "-Xmx2g",
        "-XX:+AlwaysPreTouch",
        "-Dotel.traces.exporter=otlp",
        "-Dotel.imr.export.interval=5000",
        "-Dotel.exporter.otlp.insecure=true",
        "-Dotel.exporter.otlp.endpoint=" + collectorUrl,
        "-Dotel.resource.attributes=service.name=petclinic-otel-overhead"
    ));
    result.addAll(this.agent.getAdditionalJvmArgs());
    agentJar.ifPresent(path -> result.add("-javaagent:/app/" + path.getFileName()));

    result.add("-jar");
    result.add("/app/spring-petclinic-rest.jar");
    return result.toArray(new String[] {});
  }
}
