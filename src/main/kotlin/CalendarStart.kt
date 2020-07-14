import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventAttendee
import com.google.api.services.calendar.model.EventDateTime
import com.google.api.services.calendar.model.Events
import okhttp3.internal.format
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.time.LocalDateTime


class CalendarStart {
    companion object {
        const val CREDENTIALS_FILE_PATH = "/credentials.json"
        val JSON_FACTORY: JacksonFactory = JacksonFactory.getDefaultInstance()
        val SCOPES = listOf(CalendarScopes.CALENDAR)
        const val TOKENS_DIRECTORY_PATH = "tokens"
        const val APPLICATION_NAME = "Google Calendar Service"
    }


    @Throws(IOException::class)
    fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        val credIn = CalendarStart::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH) ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(credIn))
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT,
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        ).setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun createCalendarAPIService(): Calendar {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        return Calendar.Builder(
            httpTransport,
            JSON_FACTORY,
            getCredentials(httpTransport))
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    fun createActivityEvent(activity: String): Event {
        val event =  Event()
            .setSummary("Time to climb $activity")
            .setLocation(currentUrl)
            .setDescription("Look at the time... it's Send O' Clock!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        val startDate = LocalDateTime.now().plusDays(3).toLocalDate()

        val startDateTime = DateTime("${startDate}T17:30:00-06:00")
        val start = EventDateTime().setDateTime(startDateTime).setTimeZone(timeZone)

        val endDateTime = DateTime("${startDate}T20:30:00-06:00")
        val end = EventDateTime().setDateTime(endDateTime).setTimeZone(timeZone)

        val attendeeList = mutableListOf<EventAttendee>()
        for(attendee in attendees) {
            attendeeList.add(EventAttendee().setEmail(attendee))
        }

        event.attendees = attendeeList
        event.start = start
        event.end = end

        return event
    }

    fun insertEventIntoCalendar(
        event: Event,
        service: Calendar,
        calendarId: String
    ) {
        println("Sending calendar invite . . .")
        service.events().insert(calendarId, event).setSendUpdates("all").execute()
        println("Event was successfully added!!")
    }

    fun checkNext10Events(service: Calendar) {
        val now = DateTime(System.currentTimeMillis())
        val events: Events? = service.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()
        val items = events?.items
        if(items?.isEmpty()!!) {
            println("No upcoming events found.")
        } else {
            println("Upcoming Events")
            for (event in items) {
                var start = event.start.dateTime
                if(start == null) {
                    start = event.start.dateTime
                }
                println(format("%s (%s)\n", event.summary, start))
            }
        }
    }
}
