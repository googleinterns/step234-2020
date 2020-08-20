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

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.sps.api.calendar.CalendarClientHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public final class CalendarClientHelperTest {

  private static final String ACCEPTED = "accepted";
  private static final String DECLINED = "declined";
  public static final String DATE_WITHOUT_TIME = "2019-06-20";
  public static final String RFC3339_WITH_TIME = "2019-10-12T07:20:50.52Z";
  private EventAttendee ATTENDING_SELF = new EventAttendee();
  private EventAttendee DECLINED_SELF = new EventAttendee();
  private EventAttendee NOT_RESPONDED_SELF = new EventAttendee();
  private EventAttendee ATTENDING_GUEST = new EventAttendee();
  private EventAttendee DECLINED_GUEST = new EventAttendee();
  private EventAttendee NOT_RESPONDED_GUEST = new EventAttendee();
  private List<EventAttendee> allSelfAttendeeds = Arrays.asList(ATTENDING_SELF, DECLINED_SELF, NOT_RESPONDED_SELF);
  public static final long EPOCH_TIMEPOINT = 1619827200;

  @Before
  public void setAttendees() {
    ATTENDING_SELF = new EventAttendee();
    DECLINED_SELF = new EventAttendee();
    NOT_RESPONDED_SELF = new EventAttendee();
    ATTENDING_GUEST = new EventAttendee();
    DECLINED_GUEST = new EventAttendee();
    NOT_RESPONDED_GUEST = new EventAttendee();
    allSelfAttendeeds.forEach((attendee) -> attendee.setSelf(true));
    ATTENDING_SELF.setResponseStatus(ACCEPTED);
    ATTENDING_GUEST.setResponseStatus(ACCEPTED);
    DECLINED_SELF.setResponseStatus(DECLINED);
    DECLINED_GUEST.setResponseStatus(DECLINED);
  }


  @Test
  public void isAttending_WhenOneAttendeeAccepted_ReturnsTrue() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(ATTENDING_SELF);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(true, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void isAttending_WhenOneAttendeeDeclined_ReturnsFalse() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(DECLINED_SELF);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(false, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void isAttending_WhenNoAttendeesSpecified_ReturnsTrue() throws IOException {
    Event event = new Event();
    Assert.assertEquals(true, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void isAttending_WhenHasMultipleAttendeesAndSelfAccepted_ReturnsTrue() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(ATTENDING_SELF, ATTENDING_GUEST, DECLINED_GUEST, NOT_RESPONDED_GUEST);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(true, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void isAttending_WhenHasMultipleAttendeesButSelfDeclined_ReturnsFalse() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(DECLINED_SELF, ATTENDING_GUEST, DECLINED_GUEST, NOT_RESPONDED_GUEST);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(false, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void isDateTimeSet_WhenEventHasNoStartTime_ReturnsFalse(){
    Event event = new Event();
    DateTime dateOnly = new DateTime(DATE_WITHOUT_TIME);
    EventDateTime start = new EventDateTime();
    start.setDate(dateOnly);
    event.setStart(start);
    Assert.assertEquals(false, CalendarClientHelper.isDateTimeSet(event));
  }

  @Test
  public void isDateTimeSet_WhenEventHasStartTime_ReturnsTrue(){
    Event event = new Event();
    DateTime dateWithTime = new DateTime(RFC3339_WITH_TIME);
    EventDateTime start = new EventDateTime();
    start.setDateTime(dateWithTime);
    event.setStart(start);
    Assert.assertEquals(true, CalendarClientHelper.isDateTimeSet(event));
  }

  private Event getEventWithAttendees(List<EventAttendee> attendeeList) {
    Event event = new Event();
    event.setAttendees(attendeeList);
    return event;
  }
}
