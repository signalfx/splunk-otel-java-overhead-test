package io.opentelemetry.results;

import org.testcontainers.shaded.com.google.common.base.Function;
import org.testcontainers.shaded.com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultsAverager {

    private final List<AppPerfResults> rawResults;

    public ResultsAverager(List<AppPerfResults> rawResults) {
        this.rawResults = rawResults;
    }

    public Map<String,AppPerfResults> average(List<AppPerfResults> rawResults){
        Map<String, List<AppPerfResults>> groupedByAgent = rawResults.stream()
                .reduce(new HashMap<>(),
                        (map, res) -> {
                            List<AppPerfResults> list = map.computeIfAbsent(res.getAgentName(), k -> new ArrayList<>());
                            list.add(res);
                            return map;
                        }, (stringListHashMap, stringListHashMap2) -> {
                            stringListHashMap.putAll(stringListHashMap2);
                            return stringListHashMap;
                        });
        return Maps.transformValues(groupedByAgent, this::combine);
    }

    private AppPerfResults combine(List<AppPerfResults> results){
        long minHeap = avgLong(results, r -> r.heapUsed.min);
        long maxHeap = avgLong(results, r -> r.heapUsed.max);
        AppPerfResults.MinMax heap = new AppPerfResults.MinMax(minHeap, maxHeap);
        return AppPerfResults.builder()
                .agent(results.get(0).agent)
                .startupDurationMs(avgLong(results, x -> x.startupDurationMs))
                .heapUsed(heap)
                .totalAllocated(avgLong(results, x -> x.totalAllocated))
                .totalGCTime(avgLong(results, x -> x.totalGCTime))
                .maxThreadContextSwitchRate(avgFloat(results, x -> x.maxThreadContextSwitchRate))
                .iterationAvg(avgDouble(results, x -> x.iterationAvg))
                .iterationP95(avgDouble(results, x -> x.iterationP95))
                .requestAvg(avgDouble(results, x -> x.requestAvg))
                .requestP95(avgDouble(results, x -> x.requestP95))
                .averageNetworkRead(avgLong(results, x -> x.averageNetworkRead))
                .averageNetworkWrite(avgLong(results, x -> x.averageNetworkWrite))
                .peakThreadCount(avgLong(results, x -> x.peakThreadCount))
                .averageJvmUserCpu(avgFloat(results, x -> x.maxJvmUserCpu))
                .maxJvmUserCpu(avgFloat(results, x -> x.maxJvmUserCpu))
                .averageJvmSystemCpu(avgFloat(results, x -> x.averageJvmSystemCpu))
                .maxJvmSystemCpu(avgFloat(results, x -> x.maxJvmSystemCpu))
                .averageMachineCpuTotal(avgFloat(results, x -> x.averageMachineCpuTotal))
                .runDurationMs(avgLong(results, x -> x.runDurationMs))
                .totalGcPauseNanos(avgLong(results, x -> x.totalGcPauseNanos))
                .throughputRequestsPerSecond(avgDouble(results, x -> x.throughputRequestsPerSecond))
                .build();
    }

    private float avgFloat(List<AppPerfResults> results, Function<AppPerfResults,Float> fn){
        float sum = results.stream().reduce(0.0f, (acc, cur) -> acc + fn.apply(cur), Float::sum);
        return sum/results.size();
    }

    private double avgDouble(List<AppPerfResults> results, Function<AppPerfResults,Double> fn){
        double sum = results.stream().reduce(0.0, (acc, cur) -> acc + fn.apply(cur), Double::sum);
        return sum/results.size();
    }

    private long avgLong(List<AppPerfResults> results, Function<AppPerfResults,Long> fn){
        long sum = results.stream().reduce(0L, (acc, cur) -> acc + fn.apply(cur), Long::sum);
        return sum/results.size();
    }
}
