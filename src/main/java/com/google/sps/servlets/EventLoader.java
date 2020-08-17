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

import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.servlets.Utils;


public class EventLoader {

     Calendar loadCalendarClient() throws IOException {
        String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
        Credential credential = Utils.newFlow().loadCredential(userId);
        return new Calendar.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential).build();
    }
}
