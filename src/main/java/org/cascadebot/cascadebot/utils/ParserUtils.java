package org.cascadebot.cascadebot.utils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    private static final Pattern TEXT_TIME_PARSER = Pattern.compile("(?<hours>\\d+)h|(?<minutes>\\d+)m|(?<seconds>\\d+)s");
    private static final Pattern DIGITAL_TIME_REGEX = Pattern.compile("(\\d+):(\\d+)(?::(\\d+))?");

    public static long parseTime(String input) {
        try {
            return Long.parseLong(input) * 1000;
        } catch (NumberFormatException e) {
            if (DIGITAL_TIME_REGEX.matcher(input).matches()) {
                return parseDigitalTime(input);
            } else {
                return parseTextTime(input, true);
            }
        }
    }

    public static long parseTextTime(String input, boolean onlyAllowOnce) {
        Matcher matcher = TEXT_TIME_PARSER.matcher(input);

        // These booleans represent whether the unit of time has been found yet
        boolean hoursParsed = false;
        boolean minutesParsed = false;
        boolean secondsParsed = false;

        // The result in milliseconds
        long millis = 0;
        while (matcher.find()) {
            if (matcher.group("hours") != null) {
                if (hoursParsed && onlyAllowOnce) {
                    throw new IllegalArgumentException("Only one of each time unit from the list: h, m, s is allowed!");
                }
                hoursParsed = true;
                millis += TimeUnit.HOURS.toMillis(Long.parseLong(matcher.group("hours")));
            } else if (matcher.group("minutes") != null) {
                if (minutesParsed && onlyAllowOnce) {
                    throw new IllegalArgumentException("Only one of each time unit from the list: h, m, s is allowed!");
                }
                minutesParsed = true;
                millis += TimeUnit.MINUTES.toMillis(Long.parseLong(matcher.group("minutes")));
            } else if (matcher.group("seconds") != null) {
                if (secondsParsed && onlyAllowOnce) {
                    throw new IllegalArgumentException("Only one of each time unit from the list: h, m, s is allowed!");
                }
                secondsParsed = true;
                millis += TimeUnit.SECONDS.toMillis(Long.parseLong(matcher.group("seconds")));
            }
        }
        return millis;
    }

    public static long parseDigitalTime(String input) {
        Matcher matcher = DIGITAL_TIME_REGEX.matcher(input);
        if (matcher.matches()) {
            if (matcher.group(3) == null) {
                return TimeUnit.MINUTES.toMillis(Long.parseLong(matcher.group(1))) +
                        TimeUnit.SECONDS.toMillis(Long.parseLong(matcher.group(2)));
            } else {
                return TimeUnit.HOURS.toMillis(Long.parseLong(matcher.group(1))) +
                        TimeUnit.MINUTES.toMillis(Long.parseLong(matcher.group(2))) +
                        TimeUnit.SECONDS.toMillis(Long.parseLong(matcher.group(3)));
            }
        } else {
            return 0;
        }
    }


}
