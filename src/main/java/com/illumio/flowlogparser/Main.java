package com.illumio.flowlogparser;

import com.illumio.flowlogparser.flow.ParserFlow;
import com.illumio.flowlogparser.flow.utils.TagMapExtractor;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String tagFile = System.getProperty("tag.file");
        String logFile = System.getProperty("log.file");
        String resultFile = System.getProperty("result.file");
        Map<TagMapExtractor.TagKey, String> tagMap = TagMapExtractor.extractTagMap(tagFile);
        new ParserFlow(logFile, resultFile, tagMap).run();
    }
}
