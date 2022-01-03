/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package io.opentelemetry.util;

import io.opentelemetry.results.AppPerfResults.MinMax;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.record.Record;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class JFRUtils {

  public static long sumLongEventValues(Path jfrFile, String eventName,
      String longValueKey) throws IOException {
    return reduce(jfrFile, eventName, longValueKey, 0L, Long::sum);
  }

  public static float findMaxFloat(Path jfrFile, String eventName, String valueKey) throws IOException {
    return reduce(jfrFile, eventName, valueKey, 0.0f, (BiFunction<Float, Float, Float>) Math::max);
  }

  public static MinMax findMinMax(Path jfrFile, String eventName, String valueKey) throws IOException {
    return reduce(jfrFile, eventName, valueKey, new MinMax(), (MinMax acc, Long value) -> {
      if (value > acc.max) {
          acc = acc.withMax(value);
        }
        if (value < acc.min) {
          acc = acc.withMin(value);
        }
        return acc;
    });
  }

  public static long findAverageLong(Path jfrFile, String eventName, String valueKey) throws IOException {
    return reduce(jfrFile, eventName, valueKey,
        new AverageSupport(),
        AverageSupport::add).average();
  }

  public static long findAveragePredicatedLong(Path jfrFile, String eventName, String valueKey, Predicate<RecordedEvent> filter) throws IOException {
    return reduce(jfrFile, eventName, valueKey,
        new AverageSupport(),
        AverageSupport::add, filter).average();
  }

  public static float computeAverageFloat(Path jfrFile, String eventName, String valueKey) throws IOException {
    return reduce(jfrFile, eventName, valueKey,
        new AverageFloatSupport(),
        AverageFloatSupport::add).average();
  }

  private static <T, V> T reduce(Path jfrFile, String eventName,
      String valueKey, T initial, BiFunction<T,V,T> reducer) throws IOException {
    return reduce(jfrFile, eventName, valueKey, initial, reducer, x -> true);
  }

  private static <T, V> T reduce(Path jfrFile, String eventName,
                                 String valueKey, T initial, BiFunction<T,V,T> reducer,
                                 Predicate<RecordedEvent> additionalEventFilter) throws IOException {
    RecordingFile recordingFile = new RecordingFile(jfrFile);
    T result = initial;
    while (recordingFile.hasMoreEvents()) {
      RecordedEvent recordedEvent = recordingFile.readEvent();
      if (eventName.equals(recordedEvent.getEventType().getName())) {
        if(additionalEventFilter.test(recordedEvent)) {
          V value = recordedEvent.getValue(valueKey);
          result = reducer.apply(result, value);
        }
      }
    }
    return result;
  }

  static class AverageSupport {
    long count;
    long total;
    AverageSupport add(long value){
      count++;
      total += value;
      return this;
    }
    long average(){
      if(count == 0) return -1;
      return total/count;
    }
  }

  static class AverageFloatSupport {
    long count;
    float total;
    AverageFloatSupport add(float value){
      count++;
      total += value;
      return this;
    }
    float average(){
      if(count == 0) return -1;
      return total/count;
    }
  }
}
