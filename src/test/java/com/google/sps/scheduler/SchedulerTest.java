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
import com.google.sps.data.ExtendedTask;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import sun.jvm.hotspot.opto.HaltNode;
import javax.print.attribute.HashAttributeSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.sps.api.calendar.CalendarClientHelper.createEvent;
import static com.google.sps.api.tasks.TasksClientHelper.createCustomDurationTaskWithDue;
import static com.google.sps.api.tasks.TasksClientHelper.createDefaultDurationTaskWithDue;
import static com.google.sps.converter.TimeConverter.createDateTime;
import static com.google.sps.data.ExtendedTask.getExtendedTaskWithDuration;
import static com.google.sps.scheduler.Scheduler.scheduleForADay;
import static com.google.sps.scheduler.Scheduler.scheduleInRange;

@RunWith(JUnit4.class)
public class SchedulerTest {
  private final long WORKING_HOURS =  TimeUnit.MINUTES.toMillis((Scheduler.END_HOUR - Scheduler.START_HOUR)*60 + Scheduler.END_MINUTE - Scheduler.START_MINUTE);
  private final long SIX_HOURS =  TimeUnit.HOURS.toMillis(6);
  private final long TWO_HOURS =  TimeUnit.HOURS.toMillis(2);
  private final long AN_HOUR_AND_A_HALF =  TimeUnit.MINUTES.toMillis(90);
  private final long ONE_HOUR =  TimeUnit.HOURS.toMillis(1);
  private final long HALF_AN_HOUR =  TimeUnit.MINUTES.toMillis(30);
  private final long TEN_MINS =  TimeUnit.MINUTES.toMillis(10);
  private final long FIVE_MINS =  TimeUnit.MINUTES.toMillis(5);

  private final static String ZURICH_TIME_ZONE = "Europe/Zurich";
  private final static String UTC_TIME_ZONE = "UTC";
  private final static String LOS_ANGELES_TIME_ZONE = "America/Los_Angeles";
  private final static String SHANGHAI_TIME_ZONE = "Asia/Shanghai";
  private final static int TOTAL_SAMPLES = 5;

  private ExtendedTask workingHourslong;
  private ExtendedTask sixHoursTask;
  private ExtendedTask twoHoursTask;
  private ExtendedTask secondTwoHoursTask;
  private ExtendedTask ninetyMinsTask;
  private ExtendedTask secondNinetyMinsTask;
  private ExtendedTask oneHourTask;
  private ExtendedTask halfAnHourTask;
  private ExtendedTask tenMinsTask;
  private ExtendedTask fiveMinsTask;


  private List<ExtendedTask> defaultDurationSample;
  private List<ExtendedTask> varyingDurationSample;


  @Before
  public void setUp() {
    defaultDurationSample = new ArrayList<>();
    for (int i = 0; i < TOTAL_SAMPLES; i++) {
      defaultDurationSample.add(ExtendedTask.getExtendedTaskWithDuration(Scheduler.DEFAULT_DURATION_IN_MILLISECONDS));
    }

    workingHourslong = ExtendedTask.getExtendedTaskWithDuration(WORKING_HOURS);
    sixHoursTask = ExtendedTask.getExtendedTaskWithDuration(SIX_HOURS);
    twoHoursTask = ExtendedTask.getExtendedTaskWithDuration(TWO_HOURS);
    secondTwoHoursTask = ExtendedTask.getExtendedTaskWithDuration(TWO_HOURS);
    ninetyMinsTask = ExtendedTask.getExtendedTaskWithDuration(AN_HOUR_AND_A_HALF);
    secondNinetyMinsTask = ExtendedTask.getExtendedTaskWithDuration(AN_HOUR_AND_A_HALF);
    oneHourTask = ExtendedTask.getExtendedTaskWithDuration(ONE_HOUR);
    halfAnHourTask = ExtendedTask.getExtendedTaskWithDuration(HALF_AN_HOUR);
    tenMinsTask = ExtendedTask.getExtendedTaskWithDuration(TEN_MINS);
    fiveMinsTask = ExtendedTask.getExtendedTaskWithDuration(FIVE_MINS);
    varyingDurationSample = Arrays.asList(workingHourslong, sixHoursTask, twoHoursTask, secondTwoHoursTask, ninetyMinsTask, secondNinetyMinsTask, oneHourTask, halfAnHourTask, tenMinsTask, fiveMinsTask);

  }

  @Test
  public void noTasks(){
    // Events : |----A----|  |----B---|
    // Day    : |---------------------|
    // Tasks  :
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, Collections.emptyList(), ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Collections.emptyList();

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 13, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 13, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 13, 50, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 11, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, UTC_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 0, UTC_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 30, UTC_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 16, 0, UTC_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 16, 30, UTC_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, LOS_ANGELES_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 11, 0, LOS_ANGELES_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 16, 30, LOS_ANGELES_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void scheduleForMultipleDays() {
    // Events : |--A--|      |----C----|   |-E-|    |--G--|
    //          |---B---|       |--D--|      |-F-|        |--H--|  |--J--|
    // Days   : |--------------------------------|--------------------------------|
    // Tasks  :         |--|            |--|     |--|           |--|     |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2023, 12, 31);
    LocalDate nextDay = LocalDate.of(2024, 1, 1);
    LocalDate farAhead = LocalDate.of(2026, 1, 1);
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
    Event eventG = createEvent(
        createDateTime(nextDay, Scheduler.START_HOUR, Scheduler.START_MINUTE + 45, ZURICH_TIME_ZONE),
        createDateTime(nextDay, 10, 15, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventH = createEvent(
        createDateTime(nextDay, 10, 0, ZURICH_TIME_ZONE),
        createDateTime(nextDay, 10, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventJ = createEvent(
        createDateTime(nextDay, 11, 0, ZURICH_TIME_ZONE),
        createDateTime(nextDay, 11, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    // Events inserted not in order
    calendarEvents.add(eventH);
    calendarEvents.add(eventF);
    calendarEvents.add(eventE);
    calendarEvents.add(eventD);
    calendarEvents.add(eventC);
    calendarEvents.add(eventB);
    calendarEvents.add(eventA);
    calendarEvents.add(eventG);
    calendarEvents.add(eventJ);

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, farAhead);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 11, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 16, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(nextDay, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(nextDay, 10, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(nextDay, 11, 30, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void notEnoughRoomMultipleDays() {
    // Events : |--A--|      |----C----|   |-E-| |---G---|
    //          |---B---|       |--D--|      |-F-|       |--H--|  |-------J-------|
    // Days   : |--------------------------------|--------------------------------|
    // Tasks  :         |--|            |--|                   |--|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2023, 12, 31);
    LocalDate nextDay = LocalDate.of(2024, 1, 1);
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
    Event eventG = createEvent(
        createDateTime(nextDay, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(nextDay, 10, 15, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventH = createEvent(
        createDateTime(nextDay, 10, 0, ZURICH_TIME_ZONE),
        createDateTime(nextDay, 10, 30, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventJ = createEvent(
        createDateTime(nextDay, 11, 0, ZURICH_TIME_ZONE),
        createDateTime(nextDay, Scheduler.END_HOUR, Scheduler.END_MINUTE, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    // Events inserted not in order
    calendarEvents.add(eventH);
    calendarEvents.add(eventF);
    calendarEvents.add(eventE);
    calendarEvents.add(eventD);
    calendarEvents.add(eventC);
    calendarEvents.add(eventB);
    calendarEvents.add(eventA);
    calendarEvents.add(eventG);
    calendarEvents.add(eventJ);

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, nextDay);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 11, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 16, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(nextDay, 10, 30, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void scheduleInRangeWhenStartDateEqualsEndDate() {
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

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, defaultDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 10, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 13, 0, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 13, 30, ZURICH_TIME_ZONE)),
        createDefaultDurationTaskWithDue(
            createDateTime(day, 14, 0, ZURICH_TIME_ZONE)));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void fitSingleWorkhourslong() {
    // Events :
    // Day    : |-------------------------------|
    // Tasks  : |-------------------------------|
    List<Event> calendarEvents = Collections.emptyList();
    LocalDate day = LocalDate.of(2048, 6, 23);

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, varyingDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createCustomDurationTaskWithDue(createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE), WORKING_HOURS)
    );

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

  @Test
  public void varyingDurations() {
    // Events : |-A-|  |-B-|                  |-C-|
    // Day    : |--------------------------------------|
    // Tasks  :     |1h|    |-2h-|30m|10m|5m|     |-2h-|
    List<Event> calendarEvents = new ArrayList<>();
    LocalDate day = LocalDate.of(2048, 6, 23);
    Event eventA = createEvent(
        createDateTime(day, Scheduler.START_HOUR, Scheduler.START_MINUTE, ZURICH_TIME_ZONE),
        createDateTime(day, 10, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventB = createEvent(
        createDateTime(day, 11, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 12, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);
    Event eventC = createEvent(
        createDateTime(day, 15, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 16, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE);

    calendarEvents.add(eventA);
    calendarEvents.add(eventB);
    calendarEvents.add(eventC);

    List<ExtendedTask> actualScheduledTasks = scheduleInRange(calendarEvents, varyingDurationSample, ZURICH_TIME_ZONE, day, day);
    List<ExtendedTask> expectedScheduledTasks = Arrays.asList(
        createCustomDurationTaskWithDue(
            createDateTime(day, 10, 0, ZURICH_TIME_ZONE), ONE_HOUR),
        createCustomDurationTaskWithDue(
            createDateTime(day, 12, 0, ZURICH_TIME_ZONE), TWO_HOURS),
        createCustomDurationTaskWithDue(
            createDateTime(day, 14, 0, ZURICH_TIME_ZONE), HALF_AN_HOUR),
        createCustomDurationTaskWithDue(
            createDateTime(day, 14, 30, ZURICH_TIME_ZONE), TEN_MINS),
        createCustomDurationTaskWithDue(
            createDateTime(day, 14, 40, ZURICH_TIME_ZONE), FIVE_MINS),
        createCustomDurationTaskWithDue(
            createDateTime(day, 16, 0, ZURICH_TIME_ZONE), TWO_HOURS));

    Assert.assertEquals(expectedScheduledTasks, actualScheduledTasks);
  }

}
