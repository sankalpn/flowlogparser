package com.illumio.flowlogparser.flow.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TagMapExtractorTest {

    @Test
    public void testValidTags() {
        String tagFile = getClass().getResource("/tagFiles/validTags.csv").getFile();
        Map<TagMapExtractor.TagKey, String> tagMap = TagMapExtractor.extractTagMap(tagFile);
        assertEquals(11, tagMap.size());
        assertEquals("sv_P4", tagMap.get(new TagMapExtractor.TagKey(22, Protocol.TCP)));
    }

    @Test
    public void testMissingCsv() {
        String tagFile = getClass().getResource("/tagFiles/validTags.csv").getFile().replace("validTags", "missing");
        RuntimeException e = assertThrows(RuntimeException.class, () -> TagMapExtractor.extractTagMap(tagFile));
        assertNotNull(e.getCause());
        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    public void testDuplicateTags() {
        String tagFile = getClass().getResource("/tagFiles/duplicateTags.csv").getFile();
        RuntimeException e = assertThrows(RuntimeException.class, () -> TagMapExtractor.extractTagMap(tagFile));
        assertNull(e.getCause());
    }
}
