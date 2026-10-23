package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

public class App {

    static final String[][] JOKES = {
        {"Why do programmers prefer dark mode?", "Because light attracts bugs."},
        {"Why did the developer go broke?", "Because he used up all his cache."},
        {"How many programmers does it take to change a light bulb?", "None. That's a hardware problem."},
        {"Why do Java developers wear glasses?", "Because they don't C#."},
        {"What's a programmer's favorite hangout place?", "Foo Bar."},
        {"Why was the function so calm?", "It had no side effects."},
        {"Why did the build tool break up with the IDE?", "It needed more space to resolve its dependencies."},
        {"What do you call a group of 8 hobbits?", "A hobbyte."},
        {"Why did the JSON file go to therapy?", "It had too many unresolved objects."},
        {"Why are keyboards always tired?", "Because they have two shifts."}
    };

    static final String[] REACTIONS = {
        "*polite chuckle*", "*groans loudly*", "*slow clap*", "*snorts coffee*", "*rimshot*"
    };

    static final String BANNER_TITLE = "Joke time!";
    private static final int BANNER_WIDTH = 60;
    private static final int MAX_GROAN = 5;

    public static Map<String, Object> randomJoke(Random random) {
        String[] joke = JOKES[random.nextInt(JOKES.length)];
        int groan = 1 + random.nextInt(MAX_GROAN);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("setup", joke[0]);
        result.put("punchline", joke[1]);
        result.put("groanMeter", StringUtils.repeat('*', groan) + StringUtils.repeat('.', MAX_GROAN - groan));
        result.put("reaction", REACTIONS[random.nextInt(REACTIONS.length)]);
        return result;
    }

    public static String banner() {
        String border = StringUtils.repeat('=', BANNER_WIDTH);
        return String.join(System.lineSeparator(),
            border,
            StringUtils.center(BANNER_TITLE, BANNER_WIDTH),
            border);
    }

    public static String toJson(Map<String, Object> joke) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(joke);
    }

    public static void main(String[] args) {
        Map<String, Object> joke = randomJoke(new Random());
        System.out.println(banner());
        System.out.println("Q: " + joke.get("setup"));
        System.out.println("A: " + joke.get("punchline"));
        System.out.println();
        System.out.println(toJson(joke));
    }
}
