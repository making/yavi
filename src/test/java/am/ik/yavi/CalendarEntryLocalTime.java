package am.ik.yavi;

import java.time.LocalTime;

public class CalendarEntryLocalTime {
	private String title;
	private LocalTime time;

	public CalendarEntryLocalTime(String title, LocalTime timepoint) {
		this.title = title;
		this.time = timepoint;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}
}
