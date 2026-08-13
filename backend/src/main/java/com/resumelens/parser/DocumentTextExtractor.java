package com.resumelens.parser;

public interface DocumentTextExtractor {
    boolean supports(String filename);
    String extract(byte[] contents);
}
