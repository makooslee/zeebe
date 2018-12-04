/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.model.bpmn.util.time;

import java.time.format.DateTimeParseException;
import java.util.Objects;

public class RepeatingInterval {
  public static final String INTERVAL_DESGINATOR = "/";
  public static final int INFINITE = -1;

  private final int repetitions;
  private final Interval interval;

  public RepeatingInterval(int repetitions, Interval interval) {
    this.repetitions = repetitions;
    this.interval = interval;
  }

  public int getRepetitions() {
    return repetitions;
  }

  public Interval getInterval() {
    return interval;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof RepeatingInterval)) {
      return false;
    }

    final RepeatingInterval repeatingInterval = (RepeatingInterval) o;

    return getRepetitions() == repeatingInterval.getRepetitions()
        && Objects.equals(getInterval(), repeatingInterval.getInterval());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getRepetitions(), getInterval());
  }

  public static RepeatingInterval parse(String text) {
    return parse(text, INTERVAL_DESGINATOR);
  }

  /**
   * Parses a repeating interval as two parts, separated by a given interval designator.
   *
   * <p>The first part describes how often the interval should be repeated, and the second part is
   * the interval itself; see {@link Interval#parse(String)} for more on parsing the interval.
   *
   * <p>The repeating part is conform to the following format: R[0-9]*
   *
   * <p>If given an interval with, e.g. the interval designator is not present in the text, it is
   * assumed implicitly that the interval is meant to be repeated infinitely.
   *
   * @param text text to parse
   * @param intervalDesignator the separator between the repeating and interval texts
   * @return a RepeatingInterval based on the given text
   */
  public static RepeatingInterval parse(String text, String intervalDesignator) {
    final int intervalDesignatorOffset = text.indexOf(intervalDesignator);
    int repetitions = INFINITE;
    final Interval interval;

    if (text.charAt(0) != 'R') {
      throw new DateTimeParseException("Repetition spec must start with R", text, 0);
    }

    if (intervalDesignatorOffset == -1 || intervalDesignatorOffset == text.length() - 1) {
      throw new DateTimeParseException("No interval given", text, intervalDesignatorOffset);
    }

    final String intervalText = text.substring(intervalDesignatorOffset + 1);
    interval = Interval.parse(intervalText);

    if (intervalDesignatorOffset > 1) {
      final String repetitionsText = text.substring(1, intervalDesignatorOffset);

      try {
        repetitions = Integer.parseInt(repetitionsText);
      } catch (NumberFormatException e) {
        throw new DateTimeParseException("Cannot parse repetitions count", repetitionsText, 1, e);
      }
    }

    return new RepeatingInterval(repetitions, interval);
  }
}