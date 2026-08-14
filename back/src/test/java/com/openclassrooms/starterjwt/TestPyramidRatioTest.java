package com.openclassrooms.starterjwt;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPyramidRatioTest {

    private static final double MINIMUM_INTEGRATION_RATIO = 0.30;
    private static final Pattern TEST_METHOD_PATTERN = Pattern.compile("@Test\\b|@ParameterizedTest\\b");

    @Test
    public void testSuite_integrationTestRatio_isAtLeast30Percent() throws IOException {
        Path testSourceRoot = Paths.get("src", "test", "java");

        int[] counts = {0, 0};
        try (Stream<Path> files = Files.walk(testSourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        int testMethods = countTestMethods(path);
                        counts[0] += testMethods;
                        if (path.toString().endsWith("IntegrationTest.java")) {
                            counts[1] += testMethods;
                        }
                    });
        }

        int totalTestMethods = counts[0];
        int integrationTestMethods = counts[1];
        double ratio = (double) integrationTestMethods / totalTestMethods;

        System.out.printf(
                "Integration test ratio: %d/%d = %.1f%% (minimum required: %.0f%%)%n",
                integrationTestMethods, totalTestMethods, ratio * 100, MINIMUM_INTEGRATION_RATIO * 100
        );

        assertThat(ratio)
                .withFailMessage(
                        "Integration test ratio too low: %d/%d = %.1f%%, expected at least %.0f%%",
                        integrationTestMethods, totalTestMethods, ratio * 100, MINIMUM_INTEGRATION_RATIO * 100
                )
                .isGreaterThanOrEqualTo(MINIMUM_INTEGRATION_RATIO);
    }

    private int countTestMethods(Path javaFile) {
        try {
            String content = Files.readString(javaFile);
            Matcher matcher = TEST_METHOD_PATTERN.matcher(content);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
