package com.fiberg.wsio.adapter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.util.Objects.nonNull;

/**
 * Xml adapter to transform zoned date time - java date.
 */
public class LocalDateTimeAdapter extends XmlAdapter<Date, LocalDateTime> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime unmarshal(Date date) throws Exception {

        /* Check if date is non-null */
        if (nonNull(date)) {

            /* Return zoned date time with default zone id */
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());

        }

        /* Return null by default */
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date marshal(LocalDateTime date) throws Exception {

        /* Check if date is non-null */
        if (nonNull(date)) {

            /* Return date from instant */
            return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());

        }

        /* Return null by default */
        return null;

    }

}
