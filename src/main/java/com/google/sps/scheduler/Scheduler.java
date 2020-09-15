// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.scheduler;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.sps.data.ExtendedTask;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static com.google.sps.converter.TimeConverter.epochInMilliseconds;
import static com.google.sps.converter.TimeConverter.epochToDateTime;

/**
 * Schedules some tasks in the free time slot of the calendar.
 * A new instance should be created for each scheduling.
 */
public class Scheduler {
  public static final int START_HOUR = 9;
  public static final int START_MINUTE = 0;
  public static final int END_HOUR = 18;
  public static final int END_MINUTE = 0;
  public static final long DEFAULT_DURATION_IN_MILLISECONDS = TimeUnit.MINUTES.toMillis(30);
  private List<ExtendedTask> tasks;
  private String timeZone;
  private TreeMultimap<Long, ExtendedTask> longestFirstOrderedTasks;
  private Set<Event> orderedCalendarEvents;
  private List<ExtendedTask> scheduledTasks;

  public Scheduler(Collection<Event> calendarEvents, List<ExtendedTask> tasks, String timeZone) {
    this.tasks = tasks;
    this.timeZone = timeZone;

    orderedCalendarEvents = new TreeSet<>(
        Comparator.comparingLong(event -> event.getStart().getDateTime().getValue()));
    orderedCalendarEvents.addAll(calendarEvents);
  }

  /**
   * Schedules the tasks in the free time slot of the calendar events, which must be of the same day
   * specified in the parameter.
   * For each task that can be scheduled, the due time is set and a list of all scheduled tasks
   * is returned.
   */
  public List<ExtendedTask> scheduleInRange(LocalDate startDate, LocalDate endDate) {
    scheduledTasks = new ArrayList<>();

    // It is possible to omit the parameters, but then we would have to implement a comparator on the tasks
    longestFirstOrderedTasks = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
    for (ExtendedTask task : tasks) {
      longestFirstOrderedTasks.put(task.getDuration(), task);
    }

    LocalDate scheduleDate = startDate;

    while (!scheduleDate.isAfter(endDate) && !longestFirstOrderedTasks.isEmpty()) {
      scheduleForADay(scheduleDate);
      scheduleDate = scheduleDate.plusDays(1);
    }
    return scheduledTasks;
  }

  private void scheduleForADay(LocalDate dayDate) {

    long dayStartEpochMilliseconds =
        epochInMilliseconds(dayDate, LocalTime.of(START_HOUR, START_MINUTE), timeZone);
    long dayEndEpochMilliseconds =
        epochInMilliseconds(dayDate, LocalTime.of(END_HOUR, END_MINUTE), timeZone);

    long lastEnd = dayStartEpochMilliseconds;

    for (Event event : orderedCalendarEvents) {
      long eventEnd = event.getEnd().getDateTime().getValue();
      if (eventEnd <= dayStartEpochMilliseconds) {
        continue;
      }

      long eventStart = event.getStart().getDateTime().getValue();
      if (eventStart >= dayEndEpochMilliseconds) {
        break;
      }

      lastEnd = scheduleInterval(eventStart, lastEnd);

      if (longestFirstOrderedTasks.isEmpty()) {
        break;
      }

      if (eventEnd > lastEnd) {
        lastEnd = eventEnd;
      }
    }

    scheduleInterval(dayEndEpochMilliseconds, lastEnd);
  }

  /**
   * Schedules the tasks in the free intervals between lastEnd and limit.
   */
  private long scheduleInterval(long limit, long lastEnd) {

    long scheduleInterval = limit - lastEnd;
    while (!longestFirstOrderedTasks.isEmpty()) {
      DateTime startTime = epochToDateTime(lastEnd, timeZone);

      Long longestFittingLength = longestFirstOrderedTasks.asMap().floorKey(scheduleInterval);

      // Potential improvement: do not look up the same length again.
      // This is marginal as long as we are working such a small amount of tasks (And especially such small amount of different possible durations)
      if (longestFittingLength == null) {
        return lastEnd;
      }
      ExtendedTask task = longestFirstOrderedTasks.get(longestFittingLength).pollFirst();

      task.getTask().setDue(startTime.toStringRfc3339());
      scheduledTasks.add(task);
      lastEnd += task.getDuration();
      scheduleInterval = limit - lastEnd;
    }

    return lastEnd;
  }
}
