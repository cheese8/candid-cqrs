package com.barsifedron.candid.cqrs.springboot;

import com.barsifedron.candid.cqrs.command.CommandResponse;
import com.barsifedron.candid.cqrs.springboot.sample.QueryBusFactory;
import com.barsifedron.candid.cqrs.springboot.sample.writeside.command.PleaseDoSomethingCommand;
import com.barsifedron.candid.cqrs.springboot.sample.readside.GetSomethingWasDoneCounterValueQuery;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@RequestMapping("/dosomething")
public class DoSomethingController {

    private final CommandBusFactory commandBus;
    private final QueryBusFactory queryBus;

    @Inject
    public DoSomethingController(CommandBusFactory commandBus, QueryBusFactory queryBus) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
    }

    /**
     * Try different path params to test the validating middleware.
     * <p>
     * Allowed ranges are : 3 < value1 < 20 and 1 < value2 <10
     * <p>
     * You can see them in the command.
     *
     * @return
     */
    @RequestMapping(value = "/with/{value1}/and/{value2}", method = RequestMethod.POST)
    public String greeting(@PathVariable Integer value1, @PathVariable Integer value2, @RequestParam(value = "name", defaultValue = "World") String name) {

        CommandResponse<String> commandResponse = commandBus.simpleBus().dispatch(new PleaseDoSomethingCommand(
                value1,
                value2)
        );
        return commandResponse.result;
    }

    @RequestMapping(value = "/done", method = RequestMethod.GET)
    public Long get(@RequestParam(value = "name", defaultValue = "World") String name) {

        Long response = queryBus.simpleBus().dispatch(new GetSomethingWasDoneCounterValueQuery());
        return response;
    }
}