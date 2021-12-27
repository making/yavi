package am.ik.yavi;

import java.time.ZonedDateTime;

public class CalendarEntryZonedDateTime {
	private String title;
	private ZonedDateTime dateTime;

	public CalendarEntryZonedDateTime(String title, ZonedDateTime timepoint) {
		this.title = title;
		this.dateTime = timepoint;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ZonedDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(ZonedDateTime dateTime) {
		this.dateTime = dateTime;
	}
}
