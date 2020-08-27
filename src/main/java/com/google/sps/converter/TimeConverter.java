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

package com.google.sps.converter;

import com.google.api.client.util.DateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Class that allows to convert time between different representations.
 */
public class TimeConverter {
  /**
   * Returns the epoch in milliseconds of the given date and time.
   */
  public static long epochInMilliseconds(LocalDate date, LocalTime time, String timeZone) {
    ZonedDateTime day = ZonedDateTime.of(date, time, ZoneId.of(timeZone));
    long epochSeconds = day.toEpochSecond();
    return TimeUnit.SECONDS.toMillis(epochSeconds);
  }

  /**
   * Returns a DateTime object representing the given epoch in the time zone.
   */
  public static DateTime epochToDateTime(long epoch, String timeZone) {
    long timeZoneShiftInMilliseconds = TimeZone.getTimeZone(timeZone).getOffset(epoch);
    return new DateTime(
        epoch, (int) TimeUnit.MILLISECONDS.toMinutes(timeZoneShiftInMilliseconds));
  }

  /**
   * Returns the epoch of the given date (a RFC 3339 timestamp).
   * @param date an <a href='http://tools.ietf.org/html/rfc3339'>RFC 3339</a> date/time value.
   */
  public static long dateToEpoch(String date) {
    DateTime dateTime = new DateTime(date);
    return dateTime.getValue();
  }
}
