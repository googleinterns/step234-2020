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

package com.google.sps.servlets;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;
import com.google.common.collect.ImmutableSet;
import com.google.sps.api.calendar.CalendarClientHelper;
import com.google.sps.api.tasks.TasksClientAdapter;
import com.google.sps.data.ExtendedTask;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.sps.api.calendar.CalendarClientHelper.createEventWithSummary;
import static com.google.sps.api.calendar.CalendarClientHelper.createPrivateEventWithSummaryAndDescription;
import static com.google.sps.api.tasks.TasksClientHelper.createCustomDurationTaskWithDue;
import static com.google.sps.api.tasks.TasksClientHelper.createDefaultDurationTaskWithDue;
import static com.google.sps.converter.TimeConverter.createDateTime;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.*;

public class ScheduleServletTest {
  private final static ImmutableSet<String> TASKS_IDS = ImmutableSet.of(
      "1", "2", "abcd", "RferEhJ65ytas", "656344234", "sdff&$%rewrETwe");
  private final static String TASKS_LIST_ID = "GgwefaUJHTyr34gsd";
  private final static String ZURICH_TIME_ZONE = "Europe/Zurich";
  private final static String UTC_TIME_ZONE = "UTC";

  private ScheduleServlet scheduleServlet;
  private TasksClientAdapter tasksClientAdapter;
  private List<Task> tasks;

  @Before
  public void setUp() throws IOException {
    scheduleServlet = new ScheduleServlet();
    tasks = new ArrayList<>();
    tasksClientAdapter = Mockito.mock(TasksClientAdapter.class);
    for (String taskId : TASKS_IDS) {
      Task task = new Task();
      task.setId(taskId);
      Mockito.when(tasksClientAdapter.getTask(TASKS_LIST_ID, taskId)).thenReturn(task);
      tasks.add(task);
    }
    // Throws an exception whenever the tasks list id is not valid
    Mockito.when(tasksClientAdapter.getTask(not(eq(TASKS_LIST_ID)), anyString()))
        .thenThrow(new IOException());
    // Throws an exception whenever the task id is not valid
    Mockito.when(
        tasksClientAdapter.getTask(anyString(), argThat((taskId) -> !TASKS_IDS.contains(taskId))))
        .thenThrow(new IOException());
  }

  @Test
  public void getSelectedTasks_emptyIds() {
    String[] tasksIds = new String[0];

    List<Task> expectedTasks = Collections.emptyList();
    List<Task> actualTasks = scheduleServlet.getSelectedTasks(
        tasksIds, tasksClientAdapter, TASKS_LIST_ID);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void getSelectedTasks_allExistingTasks() {
    String[] tasksIds = new String[TASKS_IDS.size()];
    TASKS_IDS.toArray(tasksIds);

    List<Task> expectedTasks = new ArrayList<>(tasks);
    List<Task> actualTasks = scheduleServlet.getSelectedTasks(
        tasksIds, tasksClientAdapter, TASKS_LIST_ID);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void getSelectedTasks_someNonExistingTasks() {
    String[] tasksIds = new String[TASKS_IDS.size() + 2];
    int indexTasksIds = 0;
    tasksIds[indexTasksIds++] = "0";
    for (String tasksId : TASKS_IDS) {
      tasksIds[indexTasksIds++] = tasksId;
    }
    tasksIds[indexTasksIds] = "qaz";

    List<Task> expectedTasks = new ArrayList<>(tasks);
    List<Task> actualTasks = scheduleServlet.getSelectedTasks(
        tasksIds, tasksClientAdapter, TASKS_LIST_ID);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void getSelectedTasks_wrongTasksListId() {
    String[] tasksIds = new String[TASKS_IDS.size()];
    TASKS_IDS.toArray(tasksIds);

    List<Task> expectedTasks = Collections.emptyList();
    List<Task> actualTasks = scheduleServlet.getSelectedTasks(
        tasksIds, tasksClientAdapter, TASKS_LIST_ID + "QwErTy");

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void createEventFromExtendedTask_withoutDescription() {
    LocalDate day = LocalDate.of(2020, 11, 9);
    ExtendedTask task = createDefaultDurationTaskWithDue(
        createDateTime(day, 11, 30, ZURICH_TIME_ZONE)
    );
    String title = "Task without description";
    task.getTask().setTitle(title);

    Event expectedEvent = createEventWithSummary(
        createDateTime(day, 11, 30, ZURICH_TIME_ZONE),
        createDateTime(day, 12, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE, title
    );
    expectedEvent.setVisibility(CalendarClientHelper.PRIVATE_VISIBILITY);
    Event actualEvent = scheduleServlet.createEventFromExtendedTask(task, ZURICH_TIME_ZONE);

    Assert.assertEquals(expectedEvent, actualEvent);
  }

  @Test
  public void createEventFromExtendedTask_withDescription() {
    LocalDate day = LocalDate.of(2022, 4, 9);
    ExtendedTask task = createDefaultDurationTaskWithDue(
        createDateTime(day, 15, 0, UTC_TIME_ZONE)
    );
    String title = "Task with description";
    task.getTask().setTitle(title);
    String description = "Description of the task";
    task.getTask().setNotes(description);

    Event expectedEvent = createPrivateEventWithSummaryAndDescription(
        createDateTime(day, 15, 0, UTC_TIME_ZONE),
        createDateTime(day, 15, 30, UTC_TIME_ZONE),
        UTC_TIME_ZONE, title, description
    );
    Event actualEvent = scheduleServlet.createEventFromExtendedTask(task, UTC_TIME_ZONE);

    Assert.assertEquals(expectedEvent, actualEvent);
  }

  @Test
  public void createEventFromExtendedTask_duration15mins() {
    LocalDate day = LocalDate.of(2023, 6, 12);
    ExtendedTask task = createCustomDurationTaskWithDue(
        createDateTime(day, 18, 0, UTC_TIME_ZONE),
        TimeUnit.MINUTES.toMillis(15)
    );
    String title = "Task 15 mins";
    task.getTask().setTitle(title);
    String description = "Task with a duration of 15 minutes";
    task.getTask().setNotes(description);

    Event expectedEvent = createPrivateEventWithSummaryAndDescription(
        createDateTime(day, 18, 0, UTC_TIME_ZONE),
        createDateTime(day, 18, 15, UTC_TIME_ZONE),
        UTC_TIME_ZONE, title, description
    );
    Event actualEvent = scheduleServlet.createEventFromExtendedTask(task, UTC_TIME_ZONE);

    Assert.assertEquals(expectedEvent, actualEvent);
  }

  @Test
  public void createEventFromExtendedTask_duration1hr() {
    LocalDate day = LocalDate.of(2025, 2, 22);
    ExtendedTask task = createCustomDurationTaskWithDue(
        createDateTime(day, 12, 0, ZURICH_TIME_ZONE),
        TimeUnit.HOURS.toMillis(1)
    );
    String title = "Task 1 hr";
    task.getTask().setTitle(title);
    String description = "Task with a duration of 1 hour";
    task.getTask().setNotes(description);

    Event expectedEvent = createPrivateEventWithSummaryAndDescription(
        createDateTime(day, 12, 0, ZURICH_TIME_ZONE),
        createDateTime(day, 13, 0, ZURICH_TIME_ZONE),
        ZURICH_TIME_ZONE, title, description
    );
    Event actualEvent = scheduleServlet.createEventFromExtendedTask(task, ZURICH_TIME_ZONE);

    Assert.assertEquals(expectedEvent, actualEvent);
  }

  @Test
  public void createEventFromExtendedTask_duration1hr30mins() {
    LocalDate day = LocalDate.of(2026, 11, 2);
    ExtendedTask task = createCustomDurationTaskWithDue(
        createDateTime(day, 8, 0, UTC_TIME_ZONE),
        TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30)
    );
    String title = "Task 1.5 hrs";
    task.getTask().setTitle(title);
    String description = "Task with a duration of 1 hour and 30 minutes";
    task.getTask().setNotes(description);

    Event expectedEvent = createPrivateEventWithSummaryAndDescription(
        createDateTime(day, 8, 0, UTC_TIME_ZONE),
        createDateTime(day, 9, 30, UTC_TIME_ZONE),
        UTC_TIME_ZONE, title, description
    );
    Event actualEvent = scheduleServlet.createEventFromExtendedTask(task, UTC_TIME_ZONE);

    Assert.assertEquals(expectedEvent, actualEvent);
  }
}
