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

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.sps.api.calendar.CalendarClientHelper.createEvent;
import static com.google.sps.api.tasks.TasksClientHelper.createTaskWithDue;
import static com.google.sps.converter.TimeConverter.createDateTime;
import static com.google.sps.scheduler.Scheduler.scheduleForADay;

@RunWith(JUnit4.class)
public class SchedulerTest {
  private final static String ZURICH_TIME_ZONE = "Europe/Zurich";
  private final static String UTC_TIME_ZONE = "UTC";
  private final static String LOS_ANGELES_TIME_ZONE = "America/Los_Angeles";
  private final static String SHANGHAI_TIME_ZONE = "Asia/Shanghai";
  private final static int TOTAL_SAMPLES = 5;
  private List<Task> sampleTasks;

  @Before
  public void setUp() {
    sampleTasks = new ArrayList<>();
    for (int i = 0; i < TOTAL_SAMPLES; i++) {
      Task task = new Task();
      task.setId("Index " + i);
      sampleTasks.add(new Task());
    }
  }

  @Test
  public void notEnoughRoomOneEvent() {
    // Events : |---------------------|
    // Day    : |---------------------|
    // Tasks  :
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2020, 8, 20);
    Event eventAllDay = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, Scheduler.END_HOUR, Scheduler.END_MINUTE, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    calendarEvents.add(eventAllDay);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Event> expectedScheduledTasks = Arrays.asList();

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void justOneTask() {
    // Events : |----A----|  |----B---|
    // Day    : |---------------------|
    // Tasks  :           |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2010, 3, 9);
    Event eventA = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 13, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 13, 30, ZURICH_TIME_ZONE),
        createDateTime(day, Scheduler.END_HOUR, Scheduler.END_MINUTE, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    calendarEvents.add(eventA);
    calendarEvents.add(eventB);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 13, 0, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void tasksAtStartAndEnd() {
    // Events :     |------A-------|
    // Day    : |---------------------|
    // Tasks  : |--|               |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2026, 12, 31);
    Event eventA = createEvent(
        createDateTime(day, 9, 30, ZURICH_TIME_ZONE),
        createDateTime(day, 17, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    calendarEvents.add(eventA);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 17, 30, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void consecutiveTasks() {
    // Events : |-A-|       |-B-|
    // Day    : |-------------------------------|
    // Tasks  :     |--||--|    |--||--||--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2048, 6, 23);
    Event eventA = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 10, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 11, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 13, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    calendarEvents.add(eventA);
    calendarEvents.add(eventB);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 10, 0, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 10, 30, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 13, 0, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 13, 30, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 14, 0, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void outOfPhaseEvents() {
    // Events :|--A--|     |-B-|    |-C-|    |--D--|
    // Day    :  |-------------------------------|
    // Tasks  :       |--|     |--|     |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2110, 9, 28);
    Event eventA = createEvent(
        createDateTime(day, Scheduler.START_HOUR - 1, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 10, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 10, 41, ZURICH_TIME_ZONE),
        createDateTime(day, 13, 50, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventC = createEvent(
        createDateTime(day, 14, 37, ZURICH_TIME_ZONE),
        createDateTime(day, 16, 34, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventD = createEvent(
        createDateTime(day, 17, 15, ZURICH_TIME_ZONE),
        createDateTime(day, Scheduler.END_HOUR + 1, Scheduler.END_MINUTE, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    calendarEvents.add(eventA);
    calendarEvents.add(eventB);
    calendarEvents.add(eventC);
    calendarEvents.add(eventD);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 10, 0, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 13, 50, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 16, 34, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void overlappedEvents() {
    // Events : |--A--|      |----C----|   |-E-|
    //          |---B---|       |--D--|      |-F-|
    // Day    : |--------------------------------|
    // Tasks  :         |--|            |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2024, 2, 29);
    Event eventA = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 10, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 11, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventC = createEvent(
        createDateTime(day, 11, 45, ZURICH_TIME_ZONE),
        createDateTime(day, 16, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventD = createEvent(
        createDateTime(day, 12, 15, ZURICH_TIME_ZONE),
        createDateTime(day, 16, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventE = createEvent(
        createDateTime(day, 17, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 17, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventF = createEvent(
        createDateTime(day, 17, 15, ZURICH_TIME_ZONE),
        createDateTime(day, Scheduler.END_HOUR, Scheduler.END_MINUTE, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    // Events inserted not in order
    calendarEvents.add(eventF);
    calendarEvents.add(eventE);
    calendarEvents.add(eventD);
    calendarEvents.add(eventC);
    calendarEvents.add(eventB);
    calendarEvents.add(eventA);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 11, 0, ZURICH_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 16, 30, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void justOneTaskDifferentTimeZones() {
    // Events : |----A----|  |----B---|
    // Day    : |---------------------|
    // Tasks  :           |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2025, 11, 15);
    Event eventA = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 13, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 12, 30, UTC_TIME_ZONE),
        createDateTime(day, Scheduler.END_HOUR, Scheduler.END_MINUTE, UTC_TIME_ZONE),
        UTC_TIME_ZONE);

    calendarEvents.add(eventA);
    calendarEvents.add(eventB);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, ZURICH_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 13, 0, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void consecutiveTasksDifferentTimeZones() {
    // The events are scheduled in the UTC time zone.
    // Events : |-A-|      |--B--|      |-C-|
    // Day    : |------------------------------|
    // Tasks  :     |--||--|     |--||--|   |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2021, 4, 22);
    Event eventA = createEvent(
        createDateTime(day, 17, 0, SHANGHAI_TIME_ZONE),
        createDateTime(day, 18, 0, SHANGHAI_TIME_ZONE),
        SHANGHAI_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 13, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 18, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventC = createEvent(
        createDateTime(day, 10, 0, LOS_ANGELES_TIME_ZONE),
        createDateTime(day, 10, 30, LOS_ANGELES_TIME_ZONE),
        LOS_ANGELES_TIME_ZONE);

    calendarEvents.add(eventA);
    calendarEvents.add(eventB);
    calendarEvents.add(eventC);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, UTC_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 10, 0, UTC_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 10, 30, UTC_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 16, 0, UTC_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 16, 30, UTC_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 17, 30, UTC_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void overlappedEventsDifferentTimeZonesAndDays() {
    // The events are scheduled in the Los Angeles time zone.
    // Events : |--A--|      |----C----|   |-E-|
    //          |---B---|       |--D--|      |-F-|
    // Day    : |--------------------------------|
    // Tasks  :         |--|            |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2024, 2, 29);
    LocalDate dayAfter = LocalDate.of(2024, 3, 1);
    Event eventA = createEvent(
        createDateTime(day, 18, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 19, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 17, 0, UTC_TIME_ZONE),
        createDateTime(day, 19, 0, UTC_TIME_ZONE),
        UTC_TIME_ZONE);
    Event eventC = createEvent(
        createDateTime(day, 20, 45, ZURICH_TIME_ZONE),
        createDateTime(dayAfter, 1, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventD = createEvent(
        createDateTime(day, 20, 15, UTC_TIME_ZONE),
        createDateTime(dayAfter, 0, 0, UTC_TIME_ZONE),
        UTC_TIME_ZONE);
    Event eventE = createEvent(
        createDateTime(dayAfter, 9, 0, SHANGHAI_TIME_ZONE),
        createDateTime(day, 9, 30, SHANGHAI_TIME_ZONE),
        SHANGHAI_TIME_ZONE);
    Event eventF = createEvent(
        createDateTime(day, 17, 15, LOS_ANGELES_TIME_ZONE),
        createDateTime(day, Scheduler.END_HOUR, Scheduler.END_MINUTE, LOS_ANGELES_TIME_ZONE),
        LOS_ANGELES_TIME_ZONE);

    // Events inserted not in order
    calendarEvents.add(eventF);
    calendarEvents.add(eventE);
    calendarEvents.add(eventD);
    calendarEvents.add(eventC);
    calendarEvents.add(eventB);
    calendarEvents.add(eventA);

    List<Task> actualScheduledTasks = scheduleForADay(calendarEvents, sampleTasks, LOS_ANGELES_TIME_ZONE, day);
    List<Task> expectedScheduledTasks = Arrays.asList(
        createTaskWithDue(
            createDateTime(day, 11, 0, LOS_ANGELES_TIME_ZONE)),
        createTaskWithDue(
            createDateTime(day, 16, 30, LOS_ANGELES_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }
}
