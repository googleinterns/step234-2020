package com.google.sps.api;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.io.*;

public class CalendarInterface implements Serializable {
    public static final String PRIMARY = "primary";
    private Calendar calendarClient;
    public static final ZoneId CET_ZONE_ID = ZoneId.of("Europe/Zurich");

    public CalendarInterface()  throws IOException{
        String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
        //Q: Do we need a new authorization code flow for every request?
        Credential credential = Utils.newFlow().loadCredential(userId);
        calendarClient = new Calendar.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential).build();
    }

    public Calendar getCalendarClient() throws IOException {
        return calendarClient;
    }

    public List<Event> loadPrimaryCalendarEventsOfTomorrow() throws IOException{
        //Todo: get timezone of user's calendar
        LocalDate todayHere = LocalDate.now(CET_ZONE_ID);
        ZonedDateTime todayStart = todayHere.atStartOfDay(CET_ZONE_ID);
        ZonedDateTime tomorrowStart = todayStart.plusDays(1);
        ZonedDateTime tomorrowEnd = todayStart.plusDays(2);
        DateTime startDate = new DateTime(tomorrowStart.toInstant().toEpochMilli());
        DateTime endDate = new DateTime(tomorrowEnd.toInstant().toEpochMilli());
        return getAcceptedEventsInTimerange(startDate, endDate);
    }

    //TODO: check for responseStatus of attendees
    public List<Event> getAcceptedEventsInTimerange(DateTime startTime, DateTime endTime) throws IOException {

        Events events = calendarClient.events().list(PRIMARY)
                .setSingleEvents(true)
                .setTimeMin(startTime)
                .setTimeMax(endTime)
                .execute();
        return events.getItems();
    }

    public void InsertEventToPrimary(Event event) throws IOException{
        calendarClient.events().insert(PRIMARY,event);
    }
}