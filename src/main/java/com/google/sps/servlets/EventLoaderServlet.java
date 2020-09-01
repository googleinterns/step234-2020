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
import com.google.sps.api.tasks.TasksClientAdapter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/load_events")
public class EventLoaderServlet extends HttpServlet {

  public EventLoaderServlet() throws IOException {
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    CalendarClientAdapter calendarClientAdapter = new CalendarClientAdapter();
    response.setContentType("text/html");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    PrintWriter writer = response.getWriter();
    List<Event> events = calendarClientAdapter.loadPrimaryCalendarEventsOfTomorrow();
    for (Event event : events) {
      writer.println(event.getSummary());
      writer.println("<br>");
    }
  }
}
