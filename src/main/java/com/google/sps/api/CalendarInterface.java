package com.google.sps.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CalendarInterface implements Serializable {
    public static final String PRIMARY = "primary";
    private final CalendarClientHelper calendarClientHelper = new CalendarClientHelper();
    private Calendar calendarClient;
    public static final ZoneId CET_ZONE_ID = ZoneId.of("Europe/Zurich");

    public CalendarInterface()  throws IOException{
        String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
        Credential credential = Utils.newFlow().loadCredential(userId);
        calendarClient = new Calendar.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential).build();
    }

    public Calendar getCalendarClient() throws IOException {
        return calendarClient;
    }

    public List<Event> loadPrimaryCalendarEventsOfTomorrow() throws IOException{
        LocalDate todayHere = LocalDate.now(CET_ZONE_ID);
        ZonedDateTime todayStart = todayHere.atStartOfDay(CET_ZONE_ID);
        ZonedDateTime tomorrowStart = todayStart.plusDays(1);
        ZonedDateTime tomorrowEnd = todayStart.plusDays(2);
        DateTime startDate = new DateTime(tomorrowStart.toInstant().toEpochMilli());
        DateTime endDate = new DateTime(tomorrowEnd.toInstant().toEpochMilli());
        return getAcceptedEventsInTimerange(startDate, endDate);
    }


    public List<Event> getAcceptedEventsInTimerange(DateTime startTime, DateTime endTime) throws IOException {

        Events events = calendarClient.events().list(PRIMARY)
                .setSingleEvents(true)
                .setTimeMin(startTime)
                .setTimeMax(endTime)
                .execute();
        return events.getItems().stream()
                .filter(event -> calendarClientHelper.isAttending(event))
                .collect(Collectors.toList());
    }

    public void InsertEventToPrimary(Event event) throws IOException{
        calendarClient.events().insert(PRIMARY,event);
    }
}
