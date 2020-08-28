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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.tasks.model.Task;
import com.google.sps.api.calendar.CalendarClientAdapter;
import com.google.sps.api.calendar.CalendarClientHelper;
import com.google.sps.api.tasks.TasksClientAdapter;
import com.google.sps.api.tasks.TasksClientHelper;
import com.google.sps.data.ScheduleMessage;
import com.google.sps.scheduler.Scheduler;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Schedules tasks on tomorrow.
 */
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {

  public static final String TASK_ID_LIST_KEY = "taskId";
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!request.getParameterMap().containsKey(TASK_ID_LIST_KEY)) {
      sendJsonResponse(response, "Select some tasks to schedule.");
      return;
    }

    CalendarClientAdapter calendarClientAdapter = new CalendarClientAdapter();
    TasksClientAdapter tasksClientAdapter = new TasksClientAdapter();
    String tasksListId = TasksClientHelper.getIdMostRecentTaskList(
        tasksClientAdapter.getTasksList());

    // Scheduler parameters
    List<Event> calendarEvents = calendarClientAdapter.loadPrimaryCalendarEventsOfTomorrow();
    String timeZone = calendarClientAdapter.getPrimaryCalendarTimeZone();
    LocalDate scheduleDate = calendarClientAdapter.getUsersTomorrowStart().toLocalDate();
    List<Task> tasksToSchedule = getSelectedTasks(
        request.getParameterValues(TASK_ID_LIST_KEY), tasksClientAdapter, tasksListId);

    // Schedules
    List<Task> scheduledTasks = Scheduler.schedule(
        calendarEvents, tasksToSchedule, timeZone, scheduleDate);

    // Updates Tasks and Calendar
    tasksClientAdapter.updateTasks(tasksListId, scheduledTasks);
    calendarClientAdapter.insertEventsToPrimary(
        createEventsFromTasks(scheduledTasks, timeZone));

    sendJsonResponse(response, scheduledTasks.size() + " tasks inserted on " + scheduleDate);
  }

  /**
   * Returns the task objects having the ids contained in the array.
   */
  private List<Task> getSelectedTasks(
      String[] tasksIds, TasksClientAdapter tasksClientAdapter, String tasksListId) {
    List<Task> tasks = new ArrayList<>();

    for (String id : tasksIds) {
      try {
        Task task = tasksClientAdapter.getTask(tasksListId, id);
        tasks.add(task);
      } catch (IOException IoException) {
        // Continue the loop if the id doesn't exist
      }
    }

    return tasks;
  }

  /**
   * Creates a calendar event for each task with the same title, description and
   * start time. The duration is the default one.
   */
  private List<Event> createEventsFromTasks(List<Task> tasks, String timeZone) {
    List<Event> calendarEvents = new ArrayList<>();

    for (Task task : tasks) {
      calendarEvents.add(
          createEventFromTask(task, timeZone));
    }

    return calendarEvents;
  }

  /**
   * Creates a calendar event with the same title, description and
   * start time of the task. The duration is the default one.
   */
  private Event createEventFromTask(Task task, String timeZone) {
    DateTime startTime = new DateTime(task.getDue());
    long endEpoch = startTime.getValue() + Scheduler.DEFAULT_DURATION_IN_MILLISECONDS;
    DateTime endTime = new DateTime(endEpoch);

    return CalendarClientHelper.createPrivateEventWithSummaryAndDescription(
        startTime, endTime, timeZone, task.getTitle(), task.getNotes());
  }

  private void sendJsonResponse(HttpServletResponse response, String responseMessage) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    try {
      ScheduleMessage messageObject = new ScheduleMessage(responseMessage);
      String jsonMessage = objectMapper.writeValueAsString(messageObject);
      response.getWriter().println(jsonMessage);
    } catch (JsonProcessingException exception) {
      throw new IOException(exception);
    }
  }
}
