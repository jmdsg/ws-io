package com.fiberg.wsio.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.nonNull;

/**
 * Xml adapter to transform zoned date time - java date.
 */
public class ZonedDateTimeAdapter extends XmlAdapter<Date, ZonedDateTime> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime unmarshal(Date date) throws Exception {

        /* Check if date is non-null */
        if (nonNull(date)) {

            /* Return zoned date time with default zone id */
            return date.toInstant().atZone(ZoneId.systemDefault());

        }

        /* Return null by default */
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date marshal(ZonedDateTime date) throws Exception {

        /* Check if date is non-null */
        if (nonNull(date)) {

            /* Return date from instant */
            return Date.from(date.toInstant());

        }

        /* Return null by default */
        return null;

    }

}
