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
import com.google.sps.converter.TimeConverter;
import com.google.sps.data.ExtendedTask;
import com.google.sps.data.ScheduleMessage;
import com.google.sps.scheduler.Scheduler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Schedules tasks on tomorrow.
 */
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {

  private static final String TASK_ID_LIST_KEY = "taskId";
  private static final String TASK_DURATION_LIST_KEY = "taskDuration";
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!request.getParameterMap().containsKey(TASK_ID_LIST_KEY)) {
      sendJsonResponse(response, "Select some tasks to schedule.");
      return;
    }

    // Scheduler parameters

    TasksClientAdapter tasksClientAdapter = new TasksClientAdapter();
    String tasksListId = TasksClientHelper.getMostRecentTaskListId(
        tasksClientAdapter.getTasksLists());

    List<ExtendedTask> tasksToSchedule = createExtendedTasks(
        getSelectedTasks(request.getParameterValues(TASK_ID_LIST_KEY), tasksClientAdapter, tasksListId),
        request.getParameterValues(TASK_DURATION_LIST_KEY));

    CalendarClientAdapter calendarClientAdapter = new CalendarClientAdapter();
    String timeZone = calendarClientAdapter.getPrimaryCalendarTimeZone();
    ZoneId zoneId = ZoneId.of(timeZone);

    String startDateString = request.getParameter("startDate");
    String endDateString = request.getParameter("endDate");
    LocalDate startDate, endDate;
    try {
      startDate = LocalDate.parse(startDateString);
      endDate = LocalDate.parse(endDateString);
    } catch (DateTimeParseException | NullPointerException exception) { //If date was not received or is in wrong format, schedule for tomorrow
      startDate = calendarClientAdapter.getUsersTomorrowStart().toLocalDate();
      endDate = startDate;
    }

    ZonedDateTime zonedStartpoint = startDate.atStartOfDay(zoneId);
    DateTime startDateTime = new DateTime(zonedStartpoint.toInstant().toEpochMilli());
    ZonedDateTime zonedEndpoint = endDate.atStartOfDay(zoneId).plusDays(1);
    DateTime endDateTime = new DateTime(zonedEndpoint.toInstant().toEpochMilli());

    List<Event> calendarEvents = calendarClientAdapter.getAcceptedEventsInTimerange(startDateTime, endDateTime);

    // Schedules
    Scheduler scheduler = new Scheduler(calendarEvents, tasksToSchedule, timeZone);
    List<ExtendedTask> scheduledExtendedTasks = scheduler.scheduleInRange(startDate, endDate);

    List<Task> scheduledTasks = scheduledExtendedTasks.stream().map(ExtendedTask::getTask).collect(Collectors.toList());
    // Updates Tasks and Calendar
    tasksClientAdapter.updateTasks(tasksListId, scheduledTasks);
    calendarClientAdapter.insertEventsToPrimary(
        createEventsFromExtendedTasks(scheduledExtendedTasks, timeZone));

    sendJsonResponse(response, scheduledTasks.size() + " tasks inserted");
  }

  /**
   * Returns the task objects having the ids contained in the array.
   */
  List<Task> getSelectedTasks(
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
   * Extends the tasks into a wrapper class that contains also the duration.
   */
  List<ExtendedTask> createExtendedTasks(List<Task> tasks, String[] tasksDurations) {
    List<ExtendedTask> extendedTasks = new ArrayList<>();

    for (ListIterator<Task> tasksIterator = tasks.listIterator(); tasksIterator.hasNext(); ) {
      int index = tasksIterator.nextIndex();
      ExtendedTask extendedTask = new ExtendedTask(tasksIterator.next(), tasksDurations[index]);
      extendedTasks.add(extendedTask);
    }

    return extendedTasks;
  }

  /**
   * Creates a calendar event for each extended task with the same title, description,
   * duration and start time.
   */
  List<Event> createEventsFromExtendedTasks(List<ExtendedTask> extendedTasks, String timeZone) {
    List<Event> calendarEvents = new ArrayList<>();

    for (ExtendedTask extendedTask : extendedTasks) {
      calendarEvents.add(
          createEventFromExtendedTask(extendedTask, timeZone));
    }

    return calendarEvents;
  }

  /**
   * Creates a calendar event with the same title, description,
   * duration and start time of the extended task.
   */
  Event createEventFromExtendedTask(ExtendedTask extendedTask, String timeZone) {
    DateTime startTime = new DateTime(extendedTask.getDue());
    long endEpoch = startTime.getValue() + extendedTask.getDuration();
    DateTime endTime = TimeConverter.epochToDateTime(endEpoch, timeZone);

    return CalendarClientHelper.createPrivateEventWithSummaryAndDescription(
        startTime, endTime, timeZone, extendedTask.getTitle(), extendedTask.getNotes());
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
