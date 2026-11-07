package com.halverson.swappin_messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageSwapTest {

    @Test
    void allArgsConstructorAndGettersExposeValues() {
        MessageSwap swap = new MessageSwap("hello", 3);

        assertEquals("hello", swap.getDescription());
        assertEquals(3, swap.getStopper());
    }

    @Test
    void settersUpdateValues() {
        MessageSwap swap = new MessageSwap();
        assertNull(swap.getDescription());
        assertEquals(0, swap.getStopper());

        swap.setDescription("changed");
        swap.setStopper(7);

        assertEquals("changed", swap.getDescription());
        assertEquals(7, swap.getStopper());
    }

    @Test
    void roundTripsThroughJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(new MessageSwap("hi", 2));
        MessageSwap parsed = mapper.readValue(json, MessageSwap.class);

        assertEquals("hi", parsed.getDescription());
        assertEquals(2, parsed.getStopper());
    }
}
