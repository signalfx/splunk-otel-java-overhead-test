package io.opentelemetry.results;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.opentelemetry.agents.Agents;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlSummaryPersister implements ResultsPersister {

    private final Path outfile;
    private final Date date;

    public YamlSummaryPersister(Path outfile) {
        this(outfile, new Date());
    }

    public YamlSummaryPersister(Path outfile, Date date) {
        this.outfile = outfile;
        this.date = date;
    }

    @Override
    public void write(List<AppPerfResults> results) {
        try {
            Map<String, Object> data = buildDataModel(results);
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory().enable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
            mapper.writeValue(outfile.toFile(), data);
        } catch (Exception e) {
            System.out.println("Error writing yaml summary file");
            e.printStackTrace();
        }
    }

    private Map<String, Object> buildDataModel(List<AppPerfResults> results) {
        Map<String, Object> result = new HashMap<>();
        result.put("version", guessVersion(results));
        result.put("datetime", new SimpleDateFormat("MMMM d, yyyy").format(date));

        Map<String, String> cpu = new HashMap<>();
        Map<String, String> network = new HashMap<>();
        Map<String, String> latency = new HashMap<>();
        Map<String, String> throughput = new HashMap<>();
        Map<String, String> startup = new HashMap<>();

        ResultsAverager averager = new ResultsAverager(results);
        for (AppPerfResults r : results) {
            String agentName = r.agent.getName();
            String agentDescription = r.agent.getDescription();

            cpu.put(agentDescription, String.format("%d%%", (int) (100 * averager.jvmUserCpu(agentName))));
            network.put(agentDescription, String.format("%.2f MiB/s", averager.networkWriteAvgMbps(agentName)));
            latency.put(agentDescription, String.format("%.2f milliseconds", averager.requestLatency(agentName)));
            throughput.put(agentDescription, String.format("%.2f requests per second", averager.throughput(agentName)));
            startup.put(agentDescription, String.format("%.2f seconds", averager.startupTime(agentName)));
        }

        result.put("CPU", cpu);
        result.put("Network", network);
        result.put("Request latency", latency);
        result.put("Throughput", throughput);
        result.put("Startup time", startup);

        return result;
    }

    private String guessVersion(List<AppPerfResults> results) {
        return results.stream()
                .filter(r -> r.getAgentName().equals(Agents.SPLUNK_OTEL.getName()))
                .findFirst()
                .map(r -> r.agent.getVersion())
                .orElse("unknown version");
    }

    public static void main(String[] args) throws Exception {
        List<AppPerfResults> rawRuns = CsvToResults.read("/tmp/results.csv");
        new YamlSummaryPersister(Path.of("out.yaml")).write(rawRuns);
    }

}
