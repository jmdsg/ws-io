package com.fiberg.wsio.handler.time;

import javax.xml.bind.annotation.*;
import java.time.LocalDateTime;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class WsIOInstant {
	private String id;

	private LocalDateTime dateTime;

	public static WsIOInstant of(String id, LocalDateTime dateTime) {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setId(id);
		timeEntry.setDateTime(dateTime);
		return timeEntry;
	}

	public static WsIOInstant unnamed(LocalDateTime dateTime) {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setDateTime(dateTime);
		return timeEntry;
	}

	public static WsIOInstant now(String id) {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setId(id);
		timeEntry.setDateTime(LocalDateTime.now());
		return timeEntry;
	}

	public static WsIOInstant now() {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setDateTime(LocalDateTime.now());
		return timeEntry;
	}

	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlValue
	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		WsIOInstant timeEntry = (WsIOInstant) o;

		if (id != null ? !id.equals(timeEntry.id) : timeEntry.id != null) return false;
		return dateTime != null ? dateTime.equals(timeEntry.dateTime) : timeEntry.dateTime == null;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TimeEntry{" +
				"id='" + id + '\'' +
				", dateTime=" + dateTime +
				'}';
	}

}
