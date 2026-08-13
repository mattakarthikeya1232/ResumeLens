package com.resumelens.dto;

/** Runtime preference for the optional, trusted local LLM adapter. */
public record LocalLlmSettings(boolean adapterAvailable, boolean enabled) { }
