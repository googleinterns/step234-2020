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

package com.google.sps.api.tasks;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.sps.converter.TimeConverter.epochInMilliseconds;
import static com.google.sps.converter.TimeConverter.epochToDateTime;

@RunWith(JUnit4.class)
public class TasksHelperTest {
  private final static String ZURICH_TIME_ZONE = "Europe/Zurich";
  private final static String UTC_TIME_ZONE = "UTC";

  @Test
  public void filterTasks_noTask() {
    // There is no task, so it should return an empty list.
    List<Task> tasks = new ArrayList<>();

    List<Task> expectedTasks = new ArrayList<>();
    List<Task> actualTasks = TasksHelper.filterTasks(tasks);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void filterTasks_allNoDueDates() {
    // All tasks don't have a due date, so all of them shouldn't be filtered.
    Task taskA = new Task();
    Task taskB = new Task();
    Task taskC = new Task();

    List<Task> tasks = new ArrayList<>();
    tasks.add(taskA);
    tasks.add(taskB);
    tasks.add(taskC);

    List<Task> expectedTasks = new ArrayList<>(tasks);
    List<Task> actualTasks = TasksHelper.filterTasks(tasks);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void filterTasks_allPastDates() {
    // All tasks have a due date that is passed, so all of them shouldn't be filtered.
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(UTC_TIME_ZONE));
    Task taskA = new Task();
    taskA.setDue(
        createDateTime(now.minusDays(1)).toStringRfc3339());
    Task taskB = new Task();
    taskB.setDue(
        createDateTime(now.minusHours(3)).toStringRfc3339());
    Task taskC = new Task();
    taskC.setDue(
        createDateTime(now.minusMinutes(5)).toStringRfc3339());

    List<Task> tasks = new ArrayList<>();
    tasks.add(taskA);
    tasks.add(taskB);
    tasks.add(taskC);

    List<Task> expectedTasks = new ArrayList<>(tasks);
    List<Task> actualTasks = TasksHelper.filterTasks(tasks);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void filterTasks_allFutureDates() {
    // All tasks have a due date in the future, so all of them should be filtered.
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(UTC_TIME_ZONE));
    Task taskA = new Task();
    taskA.setDue(
        createDateTime(now.plusDays(1)).toStringRfc3339());
    Task taskB = new Task();
    taskB.setDue(
        createDateTime(now.plusHours(3)).toStringRfc3339());
    Task taskC = new Task();
    taskC.setDue(
        createDateTime(now.plusMinutes(15)).toStringRfc3339());

    List<Task> tasks = new ArrayList<>();
    tasks.add(taskA);
    tasks.add(taskB);
    tasks.add(taskC);

    List<Task> expectedTasks = new ArrayList<>();
    List<Task> actualTasks = TasksHelper.filterTasks(tasks);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void filterTasks_mixedDates() {
    // Tasks represent all possible state: no due date,
    // due date in the past and due date in the future.
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZURICH_TIME_ZONE));
    Task taskA = new Task();
    taskA.setDue(
        createDateTime(now.minusDays(6)).toStringRfc3339());
    Task taskB = new Task();
    taskB.setDue(
        createDateTime(now.minusHours(9)).toStringRfc3339());
    Task taskC = new Task();
    taskC.setDue(
        createDateTime(now.plusMinutes(45)).toStringRfc3339());
    Task taskD = new Task();
    taskD.setDue(
        createDateTime(now.plusWeeks(2)).toStringRfc3339());
    Task taskE = new Task();
    Task taskF = new Task();

    List<Task> tasks = new ArrayList<>();
    tasks.add(taskA);
    tasks.add(taskB);
    tasks.add(taskC);
    tasks.add(taskD);
    tasks.add(taskE);
    tasks.add(taskF);

    List<Task> expectedTasks = Arrays.asList(taskA, taskB, taskE, taskF);
    List<Task> actualTasks = TasksHelper.filterTasks(tasks);

    Assert.assertEquals(expectedTasks, actualTasks);
  }

  @Test
  public void getIdMostRecentTaskList_oneList() {
    // There is one task list, so it should return its id.
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(UTC_TIME_ZONE));
    TaskList taskListA = new TaskList();
    taskListA.setId("A");
    taskListA.setUpdated(
        createDateTime(now).toStringRfc3339());

    List<TaskList> taskList = new ArrayList<>();
    taskList.add(taskListA);

    String expectedId = "A";
    String actualId = TasksHelper.getIdMostRecentTaskList(taskList);

    Assert.assertEquals(expectedId, actualId);
  }

  @Test
  public void getIdMostRecentTaskList_mixedDates() {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(ZURICH_TIME_ZONE));
    TaskList taskListA = new TaskList();
    taskListA.setId("A");
    taskListA.setUpdated(
        createDateTime(now.minusDays(6)).toStringRfc3339());
    TaskList taskListB = new TaskList();
    taskListB.setId("B");
    taskListB.setUpdated(
        createDateTime(now.minusHours(9)).toStringRfc3339());
    TaskList taskListC = new TaskList();
    taskListC.setId("C");
    taskListC.setUpdated(
        createDateTime(now.minusMinutes(45)).toStringRfc3339());
    TaskList taskListD = new TaskList();
    taskListD.setId("D");
    taskListD.setUpdated(
        createDateTime(now.minusWeeks(2)).toStringRfc3339());

    List<TaskList> taskList = new ArrayList<>();
    taskList.add(taskListA);
    taskList.add(taskListB);
    taskList.add(taskListC);
    taskList.add(taskListD);

    String expectedId = "C";
    String actualId = TasksHelper.getIdMostRecentTaskList(taskList);

    Assert.assertEquals(expectedId, actualId);
  }

  /**
   * Returns a DateTime object representing the given date and time.
   */
  private DateTime createDateTime(ZonedDateTime zonedDateTime) {
    String timeZone = zonedDateTime.getZone().toString();
    long epoch = epochInMilliseconds(
        zonedDateTime.toLocalDate(),
        zonedDateTime.toLocalTime(),
        timeZone);
    return epochToDateTime(epoch, timeZone);
  }
}
