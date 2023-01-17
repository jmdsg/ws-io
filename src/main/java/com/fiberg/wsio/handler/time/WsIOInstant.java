package com.fiberg.wsio.handler.time;

import com.fiberg.wsio.adapter.ZonedDateTimeAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.ZonedDateTime;
import java.util.Objects;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class WsIOInstant {
	private String id;

	private ZonedDateTime dateTime;

	public static WsIOInstant of(String id, ZonedDateTime dateTime) {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setId(id);
		timeEntry.setDateTime(dateTime);
		return timeEntry;
	}

	public static WsIOInstant unnamed(ZonedDateTime dateTime) {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setDateTime(dateTime);
		return timeEntry;
	}

	public static WsIOInstant now(String id) {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setId(id);
		timeEntry.setDateTime(ZonedDateTime.now());
		return timeEntry;
	}

	public static WsIOInstant now() {
		WsIOInstant timeEntry = new WsIOInstant();
		timeEntry.setDateTime(ZonedDateTime.now());
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
	@XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
	public ZonedDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(ZonedDateTime dateTime) {
		this.dateTime = dateTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsIOInstant that = (WsIOInstant) o;
		return Objects.equals(id, that.id) && Objects.equals(dateTime, that.dateTime);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, dateTime);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "WsIOInstant{" +
				"id='" + id + '\'' +
				", dateTime=" + dateTime +
				'}';
	}

}
