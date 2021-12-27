package am.ik.yavi;

import java.time.LocalDate;

public class CalendarEntryLocalDate {
	private String title;
	private LocalDate date;

	public CalendarEntryLocalDate(String title, LocalDate timepoint) {
		this.title = title;
		this.date = timepoint;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
}
