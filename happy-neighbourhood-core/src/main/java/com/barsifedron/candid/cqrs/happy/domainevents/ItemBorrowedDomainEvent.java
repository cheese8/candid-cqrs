package com.barsifedron.candid.cqrs.happy.domainevents;

import com.barsifedron.candid.cqrs.domainevent.DomainEvent;
import com.barsifedron.candid.cqrs.happy.command.BorrowItemCommandHandler;
import com.barsifedron.candid.cqrs.happy.utils.cqrs.domainevents.DomainEventToLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ItemBorrowedDomainEvent implements DomainEvent , DomainEventToLog {

    public final String email;
    public final String itemId;
    public final String memberId;
    public final String itemName;
    public final String memberFirstname;
    public final String memberSurname;
    public final LocalDate borrowedOn;
    public final LocalDate expectedReturnOn;
    public final LocalDate effectiveReturnOn;
    public final BigDecimal regularDailyRate;
    public final BigDecimal dailyFineWhenLate;
    public final BorrowItemCommandHandler.NOTIFICATION notification;

}
