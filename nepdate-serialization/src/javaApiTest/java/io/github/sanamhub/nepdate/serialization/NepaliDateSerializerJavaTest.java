package io.github.sanamhub.nepdate.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Calls the artifact from Java, as a Java user would (ADR-0004 gate 6). */
class NepaliDateSerializerJavaTest {
    @Test
    void descriptorNamesTheDate() {
        assertEquals("io.github.sanamhub.nepdate.NepaliDate", NepaliDateSerializer.INSTANCE.getDescriptor().getSerialName());
    }
}
