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
import com.google.sps.api.tasks.TasksProvider;
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
import java.time.ZoneId;
import java.util.Arrays;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servlet that schedules tasks on tomorrow.
 */
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {

  public static final String TASK_ID_LIST_KEY = "taskId";
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    TasksProvider taskProvider = new TasksProvider();
    List<Task> tasks = taskProvider.getTasks();
    if (!request.getParameterMap().containsKey(TASK_ID_LIST_KEY)) {
      sendJsonResponse(response, "Select some tasks to schedule.");
      return;
    }
    Set<String> selectedIds = Arrays.stream(request.getParameterValues(TASK_ID_LIST_KEY)).collect(Collectors.toSet());
    List<String> titlesOfTasksToSchedule = filterSelectedTaskTitles(selectedIds, tasks);

    CalendarClientAdapter calendarClientAdapter = new CalendarClientAdapter();
    String timeZone = calendarClientAdapter.getPrimaryCalendarTimeZone();
    ZoneId zoneId = ZoneId.of(timeZone);

    String startDateString = request.getParameter("startDate");
    String endDateString = request.getParameter("endDate");
    LocalDate startDate, endDate;
    try {
      startDate = LocalDate.parse(startDateString);
      endDate = LocalDate.parse(endDateString);
    } catch(DateTimeParseException | NullPointerException exception) { //If date was not received or is in wrong format, schedule for tomorrow
      startDate = calendarClientAdapter.getUsersTomorrowStart().toLocalDate();
      endDate = startDate;
    }

    ZonedDateTime zonedStartpoint = startDate.atStartOfDay(zoneId);
    DateTime startDateTime = new DateTime(zonedStartpoint.toInstant().toEpochMilli());
    ZonedDateTime zonedEndpoint = endDate.atStartOfDay(zoneId).plusDays(1);
    DateTime endDateTime = new DateTime(zonedEndpoint.toInstant().toEpochMilli());

    List<Event> calendarEvents = calendarClientAdapter.getAcceptedEventsInTimerange(startDateTime, endDateTime);
    //TODO: Schedule to an interval of days, from startDate to endDate
    List<Event> tasksEvent = Scheduler.schedule(calendarEvents, timeZone, startDate);

    for (Event event : tasksEvent) {
      calendarClientAdapter.insertEventToPrimary(event);
    }

    sendJsonResponse(response, tasksEvent.size() + " tasks inserted on " + startDate);
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

  List<String> filterSelectedTaskTitles(Set<String> selectedIds, List<Task> tasks) {
    return tasks.stream().filter((task) -> selectedIds.contains(task.getId())).map(Task::getTitle).collect(Collectors.toList());
  }
}
