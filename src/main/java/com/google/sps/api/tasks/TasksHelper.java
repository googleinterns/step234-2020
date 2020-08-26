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
import com.google.sps.converter.TimeConverter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides some helper methods for tasks.
 */
public class TasksHelper {
  /**
   * Returns a list of tasks containing only those without a date or past their due date.
   */
  public static List<Task> filterTasks(List<Task> tasks) {
    DateTime dateTimeNow = new DateTime(
        new Date());
    long epochNow = dateTimeNow.getValue();

    return tasks.stream()
        .filter(task -> task.getDue() == null || TimeConverter.dateToEpoch(task.getDue()) < epochNow)
        .collect(Collectors.toList());
  }

  /**
   * Returns the task list ID of the most recent updated list.
   */
  public static String getIdMostRecentTaskList(List<TaskList> taskList) {
    return taskList.stream()
        .max(Comparator.comparingLong(tasksList -> TimeConverter.dateToEpoch(tasksList.getUpdated())))
        .map(TaskList::getId)
        .get();
  }
}
