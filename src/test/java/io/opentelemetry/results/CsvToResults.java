package io.opentelemetry.results;

import io.opentelemetry.agents.Agent;
import io.opentelemetry.agents.Agents;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Long.parseLong;

// Can be used for ad-hoc testing to recreate results without running slow tests
class CsvToResults {

    static List<AppPerfResults> read(String pathToFile) throws Exception {

        BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(pathToFile))));
        String first = in.readLine();
        List<Field> fields = readFields(first);

        List<AppPerfResults> result = new ArrayList<>();
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            List<String> lineFields = Arrays.asList(line.split(","));
            lineFields = lineFields.subList(1, lineFields.size());
            result.addAll(buildResults(fields, lineFields));
        }

        return result;
    }

    private static List<AppPerfResults> buildResults(List<Field> fields, List<String> lineFields) {
        Map<String, Map<String, String>> agentToFieldValues = new HashMap<>();
        fields.stream()
                .map(f -> f.agent)
                .distinct()
                .forEach(agent -> agentToFieldValues.put(agent, new HashMap<>()));
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            agentToFieldValues.get(field.agent).put(field.field, lineFields.get(i));
        }
        return agentToFieldValues.entrySet().stream().map(entry -> {
                    Map<String, String> fieldValues = entry.getValue();
                    fieldValues.put("agent", entry.getKey());   // throw agent in there for later
                    return fieldValues;
                })
                .map(CsvToResults::toAppPerfResults)
                .collect(Collectors.toList());
    }

    private static AppPerfResults toAppPerfResults(Map<String, String> fv) {
        AppPerfResults.MinMax heap = new AppPerfResults.MinMax(parseLong(fv.get("minHeapUsed")), parseLong(fv.get("maxHeapUsed")));
        Agent agent = findAgent(fv.get("agent"));
        return AppPerfResults.builder()
                .agent(agent)
                .startupDurationMs(parseLong(fv.get("startupDurationMs")))
                .heapUsed(heap)
                .totalAllocated((long)parseDouble(fv.get("totalAllocatedMB"))*1024*1024)
                .totalGCTime(parseLong(fv.get("totalGCTime")))
                .maxThreadContextSwitchRate(parseFloat(fv.get("maxThreadContextSwitchRate")))
                .iterationAvg(parseDouble(fv.get("iterationAvg")))
                .iterationP95(parseDouble(fv.get("iterationP95")))
                .requestAvg(parseDouble(fv.get("requestAvg")))
                .requestP95(parseDouble(fv.get("requestP95")))
                .averageNetworkRead(parseLong(fv.get("netReadAvg")))
                .averageNetworkWrite(parseLong(fv.get("netWriteAvg")))
                .peakThreadCount(parseLong(fv.get("peakThreadCount")))
                .averageJvmUserCpu(parseFloat(fv.get("averageCpuUser")))
                .maxJvmUserCpu(parseFloat(fv.get("maxCpuUser")))
                .averageJvmSystemCpu(parseFloat(fv.get("averageCpuSystem")))
                .maxJvmSystemCpu(parseFloat(fv.get("maxCpuSystem")))
                .averageMachineCpuTotal(parseFloat(fv.get("averageMachineCpuTotal")))
                .runDurationMs(parseLong(fv.get("runDurationMs")))
                .totalGcPauseNanos(TimeUnit.MILLISECONDS.toNanos(parseLong(fv.get("gcPauseMs"))))
                .throughputRequestsPerSecond(parseDouble(fv.get("throughputAvg")))
                .build();
    }

    private static Agent findAgent(String agent) {
        switch(agent){
            case "none":
                return Agents.NONE;
            case "splunk-otel":
                return Agents.SPLUNK_OTEL;
            case "splunk-otel-profiler":
            case "profiler":
            case "cpu:text":
                return Agents.SPLUNK_PROFILER;
        }
        return new Agent.Builder().name("unknown").description("unknown agent").build();
    }

    private static List<Field> readFields(String firstLine) {
        String[] firstFields = firstLine.split(",");
        return Arrays.stream(firstFields)
                .skip(1)
                .map(field -> {
                    int i = field.lastIndexOf(':');
                    return new Field(field.substring(0, i), field.substring(i + 1));
                }).collect(Collectors.toList());
    }

    static class Field {
        String agent;
        String field;

        public Field(String agent, String field) {
            this.agent = agent;
            this.field = field;
        }
    }


}
