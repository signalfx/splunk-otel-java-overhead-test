package io.opentelemetry.results;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

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

    private Map<String, Object> buildDataModel(List<AppPerfResults> results) throws IOException {
        ResultsAverager averager = new ResultsAverager(results);
        Map<String, Object> result = new HashMap<>();
        String none = "No Instrumentation";
        String agent = "Splunk OpenTelemetry Java agent";
        String profiler = "Splunk OpenTelemetry Java agent with AlwaysOn Profiling";

        result.put("version", guessVersion(results));
        result.put("datetime", new SimpleDateFormat("MMMM d, yyyy").format(date));

        Map<String, String> cpu = new HashMap<>();
        cpu.put(none, String.format("%d%%", (int) (100 * averager.jvmUserCpu("none"))));
        cpu.put(agent, String.format("%d%%", (int) (100 * averager.jvmUserCpu("splunk-otel"))));
        cpu.put(profiler, String.format("%d%%", (int) (100 * averager.jvmUserCpu("profiler"))));
        result.put("CPU", cpu);

        Map<String, String> network = new HashMap<>();
        network.put(none, String.format("%.2f MiB/s", averager.networkWriteAvgMbps("none")));
        network.put(agent, String.format("%.2f MiB/s", averager.networkWriteAvgMbps("splunk-otel")));
        network.put(profiler, String.format("%.2f MiB/s", averager.networkWriteAvgMbps("profiler")));
        result.put("Network", network);

        Map<String, String> latency = new HashMap<>();
        latency.put(none, String.format("%.2f milliseconds", averager.requestLatency("none")));
        latency.put(agent, String.format("%.2f milliseconds", averager.requestLatency("splunk-otel")));
        latency.put(profiler, String.format("%.2f milliseconds", averager.requestLatency("profiler")));
        result.put("Request latency", latency);

        Map<String, String> throughput = new HashMap<>();
        throughput.put(none, String.format("%.2f requests per second", averager.throughput("none")));
        throughput.put(agent, String.format("%.2f requests per second", averager.throughput("splunk-otel")));
        throughput.put(profiler, String.format("%.2f requests per second", averager.throughput("profiler")));
        result.put("Throughput", throughput);

        Map<String, String> startup = new HashMap<>();
        startup.put(none, String.format("%.2f seconds", averager.startupTime("none")));
        startup.put(agent, String.format("%.2f seconds", averager.startupTime("splunk-otel")));
        startup.put(profiler, String.format("%.2f seconds", averager.startupTime("profiler")));
        result.put("Startup time", startup);

        return result;
    }

    private String guessVersion(List<AppPerfResults> results) {
        return results.stream()
                .filter(r -> r.getAgentName().equals("splunk-otel"))
                .findFirst()
                .map(r -> r.agent.getDescription())
                .map(d -> d.substring(d.lastIndexOf(" ")+1))
                .orElse("unknown version");
    }

    public static void main(String[] args) throws Exception {
        List<AppPerfResults> rawRuns = CsvToResults.read("/tmp/results.csv");
        new YamlSummaryPersister(Path.of("out.yaml")).write(rawRuns);
    }

}
