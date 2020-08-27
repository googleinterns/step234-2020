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
import com.google.sps.api.calendar.CalendarClientAdapter;
import com.google.sps.scheduler.Scheduler;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Servlet that schedules tasks on tomorrow.
 */
@WebServlet("/schedule")
public class ScheduleServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    CalendarClientAdapter calendarClientAdapter = new CalendarClientAdapter();
    List<Event> calendarEvents = calendarClientAdapter.loadPrimaryCalendarEventsOfTomorrow();
    String timeZone = calendarClientAdapter.getPrimaryCalendarTimeZone();
    LocalDate tomorrow = calendarClientAdapter.getUsersTomorrowStart().toLocalDate();
    List<Event> tasksEvent = Scheduler.schedule(calendarEvents, timeZone, tomorrow);
    for (Event event : tasksEvent) {
      calendarClientAdapter.insertEventToPrimary(event);
    }

    response.setContentType(MediaType.TEXT_PLAIN);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().println(tasksEvent.size() + " tasks inserted on " + tomorrow);
  }
}
