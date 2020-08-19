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

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public final class CalendarClientHelperTest {

  private static final String ACCEPTED = "accepted";
  private static final String DECLINED = "declined";
  private EventAttendee ATTENDING_SELF = new EventAttendee();
  private EventAttendee DECLINED_SELF = new EventAttendee();
  private EventAttendee NOT_RESPONDED_SELF = new EventAttendee();
  private EventAttendee ATTENDING_GUEST = new EventAttendee();
  private EventAttendee DECLINED_GUEST = new EventAttendee();
  private EventAttendee NOT_RESPONDED_GUEST = new EventAttendee();



  @Before
  public void setAttendees(){
      ATTENDING_SELF.setSelf(true);
      DECLINED_SELF.setSelf(true);
      NOT_RESPONDED_SELF.setSelf(true);
      ATTENDING_SELF.setResponseStatus(ACCEPTED);
      ATTENDING_GUEST.setResponseStatus(ACCEPTED);
      DECLINED_SELF.setResponseStatus(DECLINED);
      DECLINED_GUEST.setResponseStatus(DECLINED);
    }


  @Test
  public void WhenOneAttendeeAccepted_ReturnsTrue() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(ATTENDING_SELF);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(true, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void WhenOneAttendeeDeclined_ReturnsFalse() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(DECLINED_SELF);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(false, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void WhenNoAttendeesSpecified_ReturnsTrue() throws IOException {
    Event event = new Event();
    Assert.assertEquals(true, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void WhenHasMultipleAttendeesAndSelfAccepted_ReturnsTrue() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(ATTENDING_SELF, ATTENDING_GUEST, DECLINED_GUEST, NOT_RESPONDED_GUEST);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(true, CalendarClientHelper.isAttending(event));
  }

  @Test
  public void WhenHasMultipleAttendeesButSelfDeclined_ReturnsFalse() throws IOException {
    List<EventAttendee> attendeeList = Arrays.asList(DECLINED_SELF, ATTENDING_GUEST, DECLINED_GUEST, NOT_RESPONDED_GUEST);
    Event event = getEventWithAttendees(attendeeList);
    Assert.assertEquals(false, CalendarClientHelper.isAttending(event));
  }

  private Event getEventWithAttendees(List<EventAttendee> attendeeList) {
    Event event = new Event();
    event.setAttendees(attendeeList);
    return event;
  }
}
