package com.illumio.flowlogparser.flow;

import com.illumio.flowlogparser.flow.utils.TagMapExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserFlowTest {

    @Test
    public void testBasicFlow() throws IOException {
        String tagFile = getClass().getResource("/tagFiles/validTags.csv").getFile();
        String logFile = getClass().getResource("/logFiles/flowLog.txt").getFile();
        String resultFile = getClass().getResource("/resultFiles/testResult.txt").getFile();
        String expectedResultFile = getClass().getResource("/resultFiles/expected.txt").getFile();
        Map<TagMapExtractor.TagKey, String> tagMap = TagMapExtractor.extractTagMap(tagFile);
        new ParserFlow(logFile, resultFile, tagMap).run();
        Set<String> expectedLines = new HashSet<>(Files.readAllLines(Path.of(expectedResultFile)));
        List<String> resultLines = Files.readAllLines(Path.of(resultFile));
        resultLines.forEach(line -> assertTrue(expectedLines.contains(line)));
    }
}
