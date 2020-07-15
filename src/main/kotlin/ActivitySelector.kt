fun main() {
    rollForEvent()
}

fun activitySelector(activitiesList: List<String>): String = activitiesList.random()

val activities = listOf(
    "Hagan's Wall",
    "Upper Y Traverse"
)

fun rollForEvent() {
    val activity = activitySelector((activities))
    val calendar = CalendarStart()
    val website = AutomateWebsite()

    website.openWebsite(activity)

    val calendarService = calendar.createCalendarAPIService()
    val event = calendar.createActivityEvent(activity)
    calendar.insertEventIntoCalendar(event, calendarService, "primary")
}

