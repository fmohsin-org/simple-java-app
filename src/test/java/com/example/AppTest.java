package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void sameSeedGivesSameJoke() {
        assertEquals(App.randomJoke(new Random(42)), App.randomJoke(new Random(42)));
    }

    @Test
    void jokesAlwaysComeFromTheCatalog() {
        Random random = new Random(7);
        for (int i = 0; i < 100; i++) {
            Map<String, Object> joke = App.randomJoke(random);
            boolean known = Arrays.stream(App.JOKES)
                .anyMatch(j -> j[0].equals(joke.get("setup")) && j[1].equals(joke.get("punchline")));
            assertTrue(known);
            assertTrue(Arrays.asList(App.REACTIONS).contains(joke.get("reaction")));
        }
    }

    @Test
    void groanMeterIsFiveCharsWithAtLeastOneStar() {
        Random random = new Random(1);
        for (int i = 0; i < 100; i++) {
            String meter = (String) App.randomJoke(random).get("groanMeter");
            assertTrue(meter.matches("\\*{1,5}\\.{0,4}"));
            assertEquals(5, meter.length());
        }
    }

    @Test
    void bannerShowsTitle() {
        assertTrue(App.banner().contains(App.BANNER_TITLE));
    }

    @Test
    void jsonHasAllFields() {
        String json = App.toJson(App.randomJoke(new Random(3)));
        for (String field : new String[] {"setup", "punchline", "groanMeter", "reaction"}) {
            assertTrue(json.contains("\"" + field + "\""));
        }
    }
}
