package com.illumio.flowlogparser.flow.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class TagMapExtractor {
    private static final Logger log = LoggerFactory.getLogger(TagMapExtractor.class);

    public record TagKey(int port, Protocol protocol) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TagKey other)) {
                return false;
            }
            return port == other.port && protocol == other.protocol;
        }

        @Override
        public int hashCode() {
            return Objects.hash(port, protocol);
        }
    }

    public static Map<TagKey, String> extractTagMap(String tagFilePath) {
        long start = System.nanoTime();
        try (CSVParser parser = CSVParser.parse(Path.of(tagFilePath), StandardCharsets.UTF_8, CSVFormat.RFC4180.withFirstRecordAsHeader())) {
            Map<TagKey, String> result = parser.getRecords().stream()
                    .collect(Collectors.toMap(r -> new TagKey(Integer.parseInt(r.get("dstport")), Protocol.fromString(r.get("protocol"))),
                            r -> r.get("tag")));
            log.info("Parsed tags from file {} in {}", tagFilePath, Duration.ofNanos(System.nanoTime() - start).toMillis());
            return result;
        } catch (IOException e) {
            log.error("Error parsing Tag file {}", tagFilePath, e);
            throw new RuntimeException(e);
        }
    }
}
