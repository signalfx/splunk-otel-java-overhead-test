package io.opentelemetry.results;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ResultsAverager {

    private final List<AppPerfResults> rawResults;

    public ResultsAverager(List<AppPerfResults> rawResults) {
        this.rawResults = rawResults;
    }

    double jvmUserCpu(String agentName){
        return avgDouble(agentName, x -> (double)x.averageJvmUserCpu);
    }

    double networkWriteAvgMbps(String agentName){
        return avgDouble(agentName, x -> (double)x.averageNetworkWrite) / (1024*1024);
    }

    double requestLatency(String agentName){
        return avgDouble(agentName, x -> x.requestAvg);
    }

    double throughput(String agentName) {
        return avgDouble(agentName, x -> x.throughputRequestsPerSecond);
    }

    double startupTime(String agentName) {
        return avgDouble(agentName, x -> (double)x.startupDurationMs) / 1000.0;
    }

    private double avgDouble(String agentName, Function<AppPerfResults,Double> toDoubleFunction) {
        long count = agentResults(agentName).count();
        double sum = agentResults(agentName)
                .reduce(0.0, (acc, cur) -> acc + toDoubleFunction.apply(cur), Double::sum);
        return sum/count;
    }

    private Stream<AppPerfResults> agentResults(String agentName){
        return rawResults.stream().filter(isAgent(agentName));
    }

    private Predicate<AppPerfResults> isAgent(String agentName) {
        return r -> r.getAgentName().equals(agentName);
    }
}
