package com.halverson.swappin_messages;

import com.halverson.swappin_messages.entity.MessageStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageStorageTest {

    @Test
    void constructorsAndAccessors() {
        MessageStorage empty = new MessageStorage();
        assertNull(empty.getMessage());
        assertNull(empty.getId());

        MessageStorage stored = new MessageStorage("{\"a\":1}");
        stored.setId(5);
        assertEquals("{\"a\":1}", stored.getMessage());
        assertEquals(5, stored.getId());
    }

    @Test
    void equalsHashCodeAndToStringFollowFields() {
        MessageStorage a = new MessageStorage("m");
        MessageStorage b = new MessageStorage("m");
        MessageStorage c = new MessageStorage("other");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertTrue(a.toString().contains("message=m"));
    }
}
