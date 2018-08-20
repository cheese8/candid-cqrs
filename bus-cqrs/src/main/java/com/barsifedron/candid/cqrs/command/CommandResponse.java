package com.barsifedron.candid.cqrs.command;

import com.barsifedron.candid.cqrs.event.Event;

import java.util.List;

/**
 * Commands should not really return data. At the most, the result field should
 * only return an Id or a status. This class would make for a nice Tuple. But
 * when you have lemons...
 * The events describe facts that happened in your system as the result of your command.
 * Example: if your command was "Update user address", your events could be something like
 * "USER_ADDRESS_UPDATED".
 * Event listeners will react to those to update the READ model or handle side effects
 * that do not really have to do with the core of your domain (which was only to update the user address),
 * for example send an email, add a log somewhere or inform the sales team....
 */
public class CommandResponse<K> {
    public final K result;
    public final List<Event> events;

    public CommandResponse(K result, List<Event> events) {
        this.result = result;
        this.events = events;
    }
}