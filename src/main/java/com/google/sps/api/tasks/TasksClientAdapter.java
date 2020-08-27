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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.api.authorization.AuthorizationRequester;
import java.io.IOException;
import java.util.List;

/**
 * Provides access to the Tasks API.
 */
public class TasksClientAdapter {
  private final Tasks tasksClient;

  public TasksClientAdapter() throws IOException {
    String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
    Credential credential = AuthorizationRequester.newFlow().loadCredential(userId);
    tasksClient = new Tasks(
      AuthorizationRequester.HTTP_TRANSPORT,
      AuthorizationRequester.JSON_FACTORY,
      credential);
  }

  /**
   * Returns the list of the user task lists.
   */
  public List<TaskList> getTasksList() throws IOException {
    TaskLists tasksLists = tasksClient.tasklists().list().execute();
    return tasksLists.getItems();
  }

  /**
   * Returns the tasks without a date or past their due date
   * belonging to the task list specified.
   */
  public List<Task> getTasks(String tasksListId) throws IOException {
    List<Task> tasks = tasksClient.tasks().list(tasksListId).execute().getItems();
    return TasksClientHelper.filterTasks(tasks);
  }

  /**
   * Returns the tasks belonging to the most recently updated task list.
   */
  public List<Task> getTasksOfMostRecentList() throws IOException {
    return getTasks(TasksClientHelper.getIdMostRecentTaskList(getTasksList()));
  }

  /**
   * Returns the specified task belonging to the task list specified.
   */
  public Task getTask(String tasksListId, String taskId) throws IOException {
    return tasksClient.tasks().get(tasksListId, taskId).execute();
  }

  /**
   * Updates the task.
   */
  public void updateTask(String taskListId, Task task) throws IOException {
    tasksClient.tasks().update(taskListId, task.getId(), task).execute();
  }

  /**
   * Updates the date of the specified task.
   * The date is specified using a RFC 3339 timestamp.
   * @param dueDate due date as an <a href='http://tools.ietf.org/html/rfc3339'>RFC 3339</a> value.
   */
  public void updateDateTimeTask(String taskListId, String taskId, String dueDate) throws IOException {
    Task task = getTask(taskListId, taskId);
    task.setDue(dueDate);
    updateTask(taskListId, task);
  }

  /**
   * Updates the date and time of the specified task.
   * The date is specified using a DateTime object.
   */
  public void updateDateTimeTask(String taskListId, String taskId, DateTime dateTime) throws IOException {
    updateDateTimeTask(taskListId, taskId, dateTime.toStringRfc3339());
  }
}
