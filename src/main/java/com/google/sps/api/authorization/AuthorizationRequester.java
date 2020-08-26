/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.google.sps.api.authorization;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.api.services.tasks.TasksScopes;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class AuthorizationRequester {

  /**
   * Global instance of the HTTP transport.
   */
  public static final HttpTransport HTTP_TRANSPORT = new UrlFetchTransport();
  /**
   * Global instance of the JSON factory.
   */
  public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  public static final String CLIENT_SECRETS_PATH = "/client_secrets.json";
  public static final String OFFLINE_ACCESS_TYPE = "offline";
  /**
   * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
   * globally shared instance across your application.
   */
  private static final AppEngineDataStoreFactory DATA_STORE_FACTORY =
      AppEngineDataStoreFactory.getDefaultInstance();
  private static GoogleClientSecrets clientSecrets = null;

  /**
   * Loads the application's client secrets from the resources/client_secrets.json file
   */
  public static GoogleClientSecrets getClientCredential() throws IOException {
    if (clientSecrets == null) {
      try {
        InputStreamReader inputStreamReader = new InputStreamReader(AuthorizationRequester.class.getResourceAsStream(CLIENT_SECRETS_PATH));
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, inputStreamReader);
      } catch (NullPointerException exception) {
        throw new IOException("Download client_secrets.json file from the Google Cloud Dashboard Credentials into /src/main/resources/client_secrets.json");
      }
    }
    return clientSecrets;
  }

  public static String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  /**
   * Gets a new OAuth2 authorization code flow for a request.
   */
  public static GoogleAuthorizationCodeFlow newFlow() throws IOException {
    return new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        getClientCredential(), Arrays.asList(CalendarScopes.CALENDAR_EVENTS, TasksScopes.TASKS)).setDataStoreFactory(
        DATA_STORE_FACTORY).setAccessType(OFFLINE_ACCESS_TYPE).build();
  }

  /**
   * Returns the user's email.
   */
  public static String getUserEmail() {
    User user = UserServiceFactory.getUserService().getCurrentUser();
    return user.getEmail();
  }
}
