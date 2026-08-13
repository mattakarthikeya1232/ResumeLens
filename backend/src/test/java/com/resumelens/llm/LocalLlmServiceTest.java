package com.resumelens.llm;

import com.resumelens.config.ResumeLensProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalLlmServiceTest {
    @Test
    void runtimePolicyCanDisableAndReenableAnAvailableTrustedAdapter() {
        var service = new LocalLlmService(configuration(true, "/bin/cat"));

        assertTrue(service.settings().adapterAvailable());
        assertTrue(service.settings().enabled());

        assertFalse(service.updateEnabled(false).enabled());
        assertFalse(service.isAvailable());

        assertTrue(service.updateEnabled(true).enabled());
        assertTrue(service.isAvailable());
    }

    @Test
    void unavailableAdapterCannotBeEnabledByTheRuntimePreference() {
        var service = new LocalLlmService(configuration(false, ""));

        assertFalse(service.updateEnabled(true).adapterAvailable());
        assertFalse(service.settings().enabled());
    }

    private ResumeLensProperties configuration(boolean enabled, String command) {
        return new ResumeLensProperties(8_388_608, 0.62, 20,
                new ResumeLensProperties.Embedding("", "", "all-MiniLM-L6-v2"),
                new ResumeLensProperties.Llm(enabled, command, "Test local adapter"));
    }
}
