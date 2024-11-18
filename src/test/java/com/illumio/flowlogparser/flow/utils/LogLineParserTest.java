package com.illumio.flowlogparser.flow.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LogLineParserTest {

    @Test
    public void testSourcePort() {
        for (String line : List.of(
                "2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 12345 49153 6 25 20000 1620140761 1620140821 ACCEPT OK",
                "2 123456789012 eni-4d3c2b1a 192.168.1.100 203.0.113.101 12345 49154 6 15 12000 1620140761 1620140821 REJECT OK ",
                "2 123456789012 eni-5e6f7g8h 192.168.1.101 198.51.100.3 12345 49155 6 10 8000 1620140761 1620140821 ACCEPT OK ")) {
            assertEquals(12345, LogLineParser.parseLogLine(line).get().srcPort());
        }
    }

    @Test
    public void testDestPort() {
        for (String line : List.of(
                "2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 8080 6 25 20000 1620140761 1620140821 ACCEPT OK",
                "2 123456789012 eni-4d3c2b1a 192.168.1.100 203.0.113.101 23 8080 6 15 12000 1620140761 1620140821 REJECT OK ",
                "2 123456789012 eni-5e6f7g8h 192.168.1.101 198.51.100.3 25 8080 6 10 8000 1620140761 1620140821 ACCEPT OK ")) {
            assertEquals(8080, LogLineParser.parseLogLine(line).get().destPort());
        }
    }

    @Test
    public void testProtocol() {
        for (String line : List.of(
                "2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 49153 6 25 20000 1620140761 1620140821 ACCEPT OK",
                "2 123456789012 eni-4d3c2b1a 192.168.1.100 203.0.113.101 23 49154 6 15 12000 1620140761 1620140821 REJECT OK ",
                "2 123456789012 eni-5e6f7g8h 192.168.1.101 198.51.100.3 25 49155 6 10 8000 1620140761 1620140821 ACCEPT OK ")) {
            assertEquals(Protocol.TCP, LogLineParser.parseLogLine(line).get().protocol());
        }
    }

    @Test
    public void testNonConformingLine() {
        String line = "2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 - 6 25 - 1620140761 1620140821 ACCEPT OK";
        assertTrue(LogLineParser.parseLogLine(line).isEmpty());
    }

    @Test
    public void testBadProtocol() {
        String line = "2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 49153 300 25 20000 1620140761 1620140821 ACCEPT OK";
        assertEquals(Protocol.UNKNOWN, LogLineParser.parseLogLine(line).get().protocol());
    }
}
