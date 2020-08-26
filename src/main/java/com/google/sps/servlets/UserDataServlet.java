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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import org.json.JSONObject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Servlet that retrieves the email address of the user.
 */
@WebServlet("/user")
public class UserDataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON);
    response.getWriter().print(
        createJSON());
  }

  /**
   * Returns a JSON string containing the user's email.
   */
  private String createJSON() {
    User user = UserServiceFactory.getUserService().getCurrentUser();
    JSONObject formatter = new JSONObject();

    formatter.put("email", user.getEmail());

    return formatter.toString();
  }
}
