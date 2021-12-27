package am.ik.yavi;

import java.time.LocalDateTime;

public class CalendarEntryLocalDateTime {
	private String title;
	private LocalDateTime dateTime;

	public CalendarEntryLocalDateTime(String title, LocalDateTime timepoint) {
		this.title = title;
		this.dateTime = timepoint;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}
