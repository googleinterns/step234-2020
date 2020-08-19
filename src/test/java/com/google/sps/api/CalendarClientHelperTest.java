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

package com.google.sps.api;

import com.google.sps.servlets.SampleServlet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import javax.management.Attribute;

@RunWith(JUnit4.class)
public final class CalendarClientHelperTest {

  private static final String ACCEPTED = "accepted";
  private static final String DECLINED = "declined";
  private final CalendarClientHelper calendarClientHelper = new CalendarClientHelper();
  private static EventAttendee ATTENDING_SELF = new EventAttendee();
  private static EventAttendee DECLINED_SELF = new EventAttendee();
  private static EventAttendee NOT_RESPONDED_SELF = new EventAttendee();
  private static EventAttendee ATTENDING_GUEST = new EventAttendee();
  private static EventAttendee DECLINED_GUEST = new EventAttendee();
  private static EventAttendee NOT_RESPONDED_GUEST = new EventAttendee();

  public CalendarClientHelperTest() {
    ATTENDING_SELF.setSelf(true);
    DECLINED_SELF.setSelf(true);
    NOT_RESPONDED_SELF.setSelf(true);
    ATTENDING_SELF.setResponseStatus(ACCEPTED);
    ATTENDING_GUEST.setResponseStatus(ACCEPTED);
    DECLINED_SELF.setResponseStatus(DECLINED);
    DECLINED_GUEST.setResponseStatus(DECLINED);
  }


  @Test
  //User attends an event
  public void oneAttendeeAccepted() throws IOException {
    List<EventAttendee> attendeeList = new ArrayList<>();
    attendeeList.add(ATTENDING_SELF);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(calendarClientHelper.isAttending(event), true);
  }

  @Test
  //User has declined an event - should return false
  public void oneAttendeeDeclined() throws IOException {
    List<EventAttendee> attendeeList = new ArrayList<>();
    attendeeList.add(DECLINED_SELF);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(calendarClientHelper.isAttending(event), false);
  }

  @Test
  //If the event has no attendees, it is the user's personal event, which is handled as attended
  public void noAttendeesSpecified() throws IOException {
    Event event = new Event();
    Assert.assertEquals(calendarClientHelper.isAttending(event), true);
  }

  @Test
  //User attends an event with multiple other attendees
  public void multipleAttendeeSelfAccepted() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(ATTENDING_SELF, ATTENDING_GUEST, DECLINED_GUEST, NOT_RESPONDED_GUEST);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(calendarClientHelper.isAttending(event), true);
  }

  @Test
  //User does not attend an event with multiple other attendees
  public void multipleAttendeeSelfDeclined() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(DECLINED_SELF, ATTENDING_GUEST, DECLINED_GUEST, NOT_RESPONDED_GUEST);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(calendarClientHelper.isAttending(event), false);
  }

  private Event getEventWithAttendees(List<EventAttendee> attendeeList) {
    Event event = new Event();
    event.setAttendees(attendeeList);
    return event;
  }
}
