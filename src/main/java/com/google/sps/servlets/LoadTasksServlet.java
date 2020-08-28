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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.tasks.model.Task;
import com.google.sps.api.tasks.TasksClientAdapter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

/**
 * Loads tasks as JSON
 */
@WebServlet("/load_tasks")
public class LoadTasksServlet extends HttpServlet {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON);
    String tasksJson = getTasksJson();
    response.getWriter().println(tasksJson);
  }

  private String getTasksJson() throws IOException {
    TasksClientAdapter tasksInterface = new TasksClientAdapter();
    List<Task> tasks = tasksInterface.getTasksOfMostRecentList();
    String tasksJson = objectMapper.writeValueAsString(tasks);
    return tasksJson;
  }
}
