package com.illumio.flowlogparser.flow;

import com.illumio.flowlogparser.flow.utils.LogLineParser;
import com.illumio.flowlogparser.flow.utils.TagMapExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

public class ParserFlow {

    private static final Logger log = LoggerFactory.getLogger(ParserFlow.class);
    private static final String UNTAGGED = "Untagged";

    private final Path logFilePath;
    private final Path resultFilePath;
    private final Map<TagMapExtractor.TagKey, String> tagMap;

    // Since we process lines in parallel, we use ConcurrentHashMap which has atomic merge operation as explained below.
    private ConcurrentMap<String, Integer> tagCounts = new ConcurrentHashMap<>();
    private ConcurrentMap<TagMapExtractor.TagKey, Integer> scrPortCounts = new ConcurrentHashMap<>();
    private ConcurrentMap<TagMapExtractor.TagKey, Integer> destPortCounts = new ConcurrentHashMap<>();

    public ParserFlow(String logPath, String resultFile, Map<TagMapExtractor.TagKey, String> tagMap) {
        logFilePath = Path.of(logPath);
        resultFilePath = Path.of(resultFile);
        this.tagMap = tagMap;
    }

    private void writeResults() throws IOException {
        Files.writeString(resultFilePath, "Tag Counts\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        int untagged = tagCounts.getOrDefault(UNTAGGED, 0);
        tagCounts.remove(UNTAGGED);
        Files.writeString(resultFilePath, "Tag,Count\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.write(resultFilePath,
                tagCounts.entrySet().stream()
                        .map(entry -> String.format("%s,%d", entry.getKey(), entry.getValue()))
                        .toList(),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.writeString(resultFilePath, String.format("%s,%d\n", UNTAGGED, untagged), StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.writeString(resultFilePath, "\n\nDestination Port/Protocol Combination Counts:\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.writeString(resultFilePath, "Port,Protocol,Count\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.write(resultFilePath,
                destPortCounts.entrySet().stream()
                        .map(entry -> String.format("%d,%s,%d", entry.getKey().port(), entry.getKey().protocol().getName().toLowerCase(), entry.getValue()))
                        .toList(),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.writeString(resultFilePath, "\n\nSource Port/Protocol Combination Counts:\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.writeString(resultFilePath, "Port,Protocol,Count\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        Files.write(resultFilePath,
                scrPortCounts.entrySet().stream()
                        .map(entry -> String.format("%d,%s,%d", entry.getKey().port(), entry.getKey().protocol().getName().toLowerCase(), entry.getValue()))
                        .toList(),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }

    public void run () {
        long start = System.nanoTime();
        try (Stream<String> lines = Files.lines(logFilePath)) {
            lines.parallel()
                    .map(LogLineParser::parseLogLine)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    // merge operation is atomic, allowing thread safe count updates
                    .forEach(attr -> {
                        // Update tag count
                        TagMapExtractor.TagKey destPortKey = new TagMapExtractor.TagKey(attr.destPort(), attr.protocol());
                        String tag = tagMap.getOrDefault(destPortKey, UNTAGGED);
                        tagCounts.merge(tag, 1, Integer::sum);
                        // Update port/protocol counts
                        TagMapExtractor.TagKey srcPortKey = new TagMapExtractor.TagKey(attr.srcPort(), attr.protocol());
                        scrPortCounts.merge(srcPortKey, 1, Integer::sum);
                        destPortCounts.merge(destPortKey, 1, Integer::sum);
                    });
            // Write results
            writeResults();
            long end = System.nanoTime();
            log.info("Analyzed log file {} in {} ms", logFilePath, Duration.ofNanos(end - start).toMillis());
        } catch (IOException e) {
            log.error("Error analyzing log path {}", logFilePath, e);
            throw new RuntimeException(e);
        }
    }
}
