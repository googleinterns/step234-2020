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

package com.google.sps.api.calendar;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import java.io.Serializable;
import java.util.List;

public class CalendarClientHelper implements Serializable {

  public static final String ACCEPTED = "accepted";
  public static final String BUSY_TRANSPARENCY = "opaque";
  public static final String PRIVATE_VISIBILITY = "private";

  /**
   * Filters for events that are attended by the user (used in CalendarInterface)
   * The EventAttendee's getSelf() method indicates whether this entry represents the calendar on which this copy of the event appears.
   */
  public static boolean isAttending(Event event) {
    List<EventAttendee> attendeeList = event.getAttendees();
    if (attendeeList == null) {
      return true;
    }
    for (EventAttendee attendee : attendeeList) {
      if (attendee.getSelf() != null && attendee.getSelf()) {
        return ACCEPTED.equals(attendee.getResponseStatus());
      }
    }
    return false;
  }

  /**
   * Filters out all-day events that do not have start.DateTime and end.Datetime set.
   */
  public static boolean isDateTimeSet(Event event) {
    return event.getStart().getDateTime() != null && event.getEnd().getDateTime() != null;
  }

  /**
   * Returns true if the event is set to busy, so it blocks time on the calendar,
   * false if the event is set to free.
   */
  public static boolean isBusy(Event event) {
    String transparency = event.getTransparency();
    return transparency == null || transparency.equals(BUSY_TRANSPARENCY);
  }

  /**
   * Returns an event with the given start and end time in the specific time zone.
   */
  public static Event createEvent(DateTime startTime, DateTime endTime, String timeZone) {
    Event event = new Event();

    EventDateTime eventStart = new EventDateTime()
        .setDateTime(startTime)
        .setTimeZone(timeZone);

    EventDateTime eventEnd = new EventDateTime()
        .setDateTime(endTime)
        .setTimeZone(timeZone);

    event.setStart(eventStart)
        .setEnd(eventEnd);

    return event;
  }

  /**
   * Returns an event with the given start and end time in the specific time zone
   * and with a summary.
   */
  public static Event createEventWithSummary(
      DateTime startTime, DateTime endTime, String timeZone, String summary) {
    Event event = createEvent(startTime, endTime, timeZone);

    event.setSummary(summary);

    return event;
  }

  /**
   * Returns a private event with the given start and end time in the specific time zone
   * and with a summary and a description.
   */
  public static Event createPrivateEventWithSummaryAndDescription(
      DateTime startTime, DateTime endTime, String timeZone, String summary, String description) {
    Event event = createEventWithSummary(startTime, endTime, timeZone, summary);

    event.setVisibility(PRIVATE_VISIBILITY);
    event.setDescription(description);

    return event;
  }
}
