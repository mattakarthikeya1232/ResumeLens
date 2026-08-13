package com.resumelens.dto;

/** The adapter itself stays server-configured; this is the user's runtime preference. */
public record UpdateLocalLlmSettings(boolean enabled) { }
