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

import static converter.TimeConverter.epochInMilliseconds;
import static converter.TimeConverter.epochToDateTime;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.sps.api.tasks.TasksProvider;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/** Class that allows to schedule some tasks in the free time slot of the calendar. */
public class Scheduler {
  public static final int START_HOUR = 9;
  public static final int START_MINUTE = 0;
  public static final int END_HOUR = 18;
  public static final int END_MINUTE = 0;
  public static final long DEFAULT_DURATION_IN_MILLISECONDS = TimeUnit.MINUTES.toMillis(30);

  /**
   * Schedules the tasks in the free time slot of the calendar events, which must be of the same day
   * specified in the parameter.
   * For each task that can be scheduled, a new calendar event object is created and a list of
   * all of them is returned.
   */
  public static List<Event> schedule(
      List<Event> calendarEvents, List<String> tasks, String timeZone, LocalDate dayDate) {
    List<Event> tasksEvents = new ArrayList<>();

    Set<Event> orderedCalendarEvents = new TreeSet<>(
        Comparator.comparingLong(event -> event.getStart().getDateTime().getValue()));
    orderedCalendarEvents.addAll(calendarEvents);

    long dayStartEpochMilliseconds =
        epochInMilliseconds(dayDate, LocalTime.of(START_HOUR, START_MINUTE), timeZone);
    long dayEndEpochMilliseconds =
        epochInMilliseconds(dayDate, LocalTime.of(END_HOUR, END_MINUTE), timeZone);

    long lastEnd = dayStartEpochMilliseconds;
    Iterator<String> tasksIterator = tasks.iterator();

    for (Event event : orderedCalendarEvents) {
      long eventEnd = event.getEnd().getDateTime().getValue();
      if (eventEnd <= dayStartEpochMilliseconds) {
        continue;
      }

      long eventStart = event.getStart().getDateTime().getValue();
      if (eventStart >= dayEndEpochMilliseconds) {
        break;
      }

      while (eventStart - lastEnd >= DEFAULT_DURATION_IN_MILLISECONDS &&
          tasksIterator.hasNext()) {
        DateTime startTime = epochToDateTime(lastEnd, timeZone);
        lastEnd += DEFAULT_DURATION_IN_MILLISECONDS;
        DateTime endTime = epochToDateTime(lastEnd, timeZone);
        tasksEvents.add(
            createEventWithSummary(startTime, endTime, timeZone, tasksIterator.next()));
      }

      if (!tasksIterator.hasNext()) {
        break;
      }

      if (eventEnd > lastEnd) {
        lastEnd = eventEnd;
      }
    }

    while (dayEndEpochMilliseconds - lastEnd >= DEFAULT_DURATION_IN_MILLISECONDS &&
        tasksIterator.hasNext()) {
      DateTime startTime = epochToDateTime(lastEnd, timeZone);
      lastEnd += DEFAULT_DURATION_IN_MILLISECONDS;
      DateTime endTime = epochToDateTime(lastEnd, timeZone);
      tasksEvents.add(
          createEventWithSummary(startTime, endTime, timeZone, tasksIterator.next()));
    }

    return tasksEvents;
  }

  /**
   * Schedules some sample tasks in the free time slot of the calendar events, which must be of
   * the same day specified in the parameter.
   * For each task that can be scheduled, a new calendar event object is created and a list of
   * all of them is returned.
   */
  public static List<Event> schedule(List<Event> calendarEvents, String timeZone, LocalDate dayDate) {
    // TODO: remove the sample tasks after the Tasks API integration (issue #8)
    return schedule(calendarEvents, TasksProvider.TASKS_SAMPLE, timeZone, dayDate);
  }

  /**
   * Returns an event with the given start and end time in the specific time zone.
   */
  public static Event createEvent(DateTime startTime, DateTime endTime, String timeZone) {
    // TODO: move this method to the class managing the calendar API (issue #7)
    Event event = new Event();

    EventDateTime eventStart = new EventDateTime()
        .setDateTime(startTime)
        .setTimeZone(timeZone);

    EventDateTime eventEnd = new EventDateTime()
        .setDateTime(endTime)
        .setTimeZone(timeZone);

    event.setStart(eventStart)
        .setEnd(eventEnd);

    return event;
  }

  /**
   * Returns an event with the given start and end time in the specific time zone
   * and with a summary.
   */
  public static Event createEventWithSummary(
      DateTime startTime, DateTime endTime, String timeZone, String summary) {
    // TODO: move this method to the class managing the calendar API (issue #7)
    Event event = createEvent(startTime, endTime, timeZone);

    event.setSummary(summary);

    return event;
  }
}
