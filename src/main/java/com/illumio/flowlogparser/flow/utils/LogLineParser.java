package com.illumio.flowlogparser.flow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogLineParser {
    private static final Logger log = LoggerFactory.getLogger(LogLineParser.class);

    /**
     * Regex for log line with assumptions mentioned in the readme
     */
    private static final Pattern LOG_LINE = Pattern.compile(
            "^(?<logVersion>[0-9]+)[\\h]+" +
            "(?<accountId>[0-9a-zA-Z]+)[\\h]+" +
            "(?<interfaceId>[a-z0-9\\-]+)[\\h]+" +
            "(?<srcIp>[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+)[\\h+]" +
            "(?<destIp>[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+\\.[0-9]{1,3}+)[\\h]+" +
            "(?<srcPort>[0-9]{1,5}+)[\\h]+" +
            "(?<destPort>[0-9]{1,5}+)[\\h]+" +
            "(?<protocol>[0-9]{1,3}+)[\\h]+" +
            "(?<packets>[0-9]+)[\\h]+" +
            "(?<bytes>[0-9]+)[\\h]+" +
            "(?<start>[0-9]+)[\\h]+" +
            "(?<end>[0-9]+)[\\h]+" +
            "(?<action>[A-Z]+)[\\h]+" +
            "(?<logStatus>[A-Z]+)");

    public record LogAttributes(int srcPort, int destPort, Protocol protocol) {}

    public static Optional<LogAttributes> parseLogLine(String line) {
        Matcher matcher = LOG_LINE.matcher(line);
        try {
            if (matcher.find()) {
                return Optional.of(new LogAttributes(Integer.parseInt(matcher.group("srcPort")),
                        Integer.parseInt(matcher.group("destPort")),
                        Protocol.fromCode(Integer.parseInt(matcher.group("protocol")))));
            } else {
                log.error("Line [{}] does not match log line pattern", line);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error parsing line [{}]", line, e);
            return Optional.empty();
        }
    }
}
