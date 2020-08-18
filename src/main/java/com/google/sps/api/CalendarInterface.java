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
import java.util.Date;
import java.util.List;

public class CalendarInterface implements Serializable {
    public static final String PRIMARY = "primary";
    private Calendar calendarClient;

    public CalendarInterface()  throws IOException{
        String userId = UserServiceFactory.getUserService().getCurrentUser().getUserId();
        //Q: Do we need a new authorization code flow for every request?
        Credential credential = Utils.newFlow().loadCredential(userId);
        calendarClient = new Calendar.Builder(Utils.HTTP_TRANSPORT, Utils.JSON_FACTORY, credential).build();
    }

    public Calendar getCalendarClient() throws IOException {
        return calendarClient;
    }


    //Todo: actually querry next day, try to use LocalDate instead as well
    public List<Event> loadPrimaryCalendarEventsOfNext24Hour() throws IOException {
        Date currentDate = new Date();
        long unix = currentDate.getTime();
        Date todayEndDate = new Date();
        todayEndDate.setTime(unix + 86400);
        DateTime startDate = new DateTime(currentDate);
        DateTime endDate = new DateTime(todayEndDate);
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