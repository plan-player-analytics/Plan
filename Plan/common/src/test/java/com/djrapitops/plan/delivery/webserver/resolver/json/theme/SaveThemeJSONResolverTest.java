/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.resolver.json.theme;

import com.djrapitops.plan.delivery.domain.datatransfer.ThemeDto;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.exception.MethodNotAllowedException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.google.gson.Gson;
import extension.FullSystemExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.RandomData;
import utilities.TestResources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author AuroraLS3
 */
@ExtendWith({MockitoExtension.class, FullSystemExtension.class})
class SaveThemeJSONResolverTest {

    @Mock
    ResponseFactory responseFactory;
    @Spy
    Gson gson = new Gson();
    @InjectMocks
    SaveThemeJSONResolver saveThemeJSONResolver;

    @Mock
    Request request;

    static Stream<Arguments> invalidThemeModificationsCases() {
        return Stream.of(
                Arguments.arguments("null name", (Consumer<ThemeDto>) themeDto -> themeDto.setName(null)),
                Arguments.arguments("bad char name", (Consumer<ThemeDto>) themeDto -> themeDto.setName("bad-char-?")),
                Arguments.arguments("empty name", (Consumer<ThemeDto>) themeDto -> themeDto.setName("")),
                Arguments.arguments("name mismatch", (Consumer<ThemeDto>) themeDto -> themeDto.setName("not-default")), // Must match theme parameter
                Arguments.arguments("too long name", (Consumer<ThemeDto>) themeDto -> themeDto.setName(RandomData.randomString(101))),
                Arguments.arguments("null colors", (Consumer<ThemeDto>) themeDto -> themeDto.setColors(null)),
                Arguments.arguments("empty colors", (Consumer<ThemeDto>) themeDto -> themeDto.setColors(Map.of())),
                Arguments.arguments("null night colors", (Consumer<ThemeDto>) themeDto -> themeDto.setNightColors(null)),
                Arguments.arguments("empty night colors", (Consumer<ThemeDto>) themeDto -> themeDto.setNightColors(Map.of())),
                Arguments.arguments("null use cases", (Consumer<ThemeDto>) themeDto -> themeDto.setUseCases(null)),
                Arguments.arguments("empty use cases", (Consumer<ThemeDto>) themeDto -> themeDto.setUseCases(Map.of())),
                Arguments.arguments("invalid color 1", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", "--color-that-does-not-start)")),
                Arguments.arguments("invalid color 2", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", "var(--color-that-does-not-end")),
                Arguments.arguments("invalid color 3", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", List.of("var(--color-that-does-not-end)"))),
                Arguments.arguments("invalid color 4", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", "var(--color/that-does-not-exist)")),
                Arguments.arguments("invalid color 5", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", List.of("var(--color/that-does-not-exist)"))),
                Arguments.arguments("invalid color 6", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", List.of(1234L))),
                Arguments.arguments("invalid color 7", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", "")),
                Arguments.arguments("invalid color 8", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", "not-a-css-variable")),
                Arguments.arguments("invalid color 9", (Consumer<ThemeDto>) themeDto -> themeDto.getUseCases().put("wrongColor", 123L)),
                Arguments.arguments("null night cases", (Consumer<ThemeDto>) themeDto -> themeDto.setNightModeUseCases(null)),
                Arguments.arguments("empty night cases", (Consumer<ThemeDto>) themeDto -> themeDto.setNightModeUseCases(Map.of())),
                Arguments.arguments("Invalid n color 1", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", "--color-that-does-not-start)")),
                Arguments.arguments("Invalid n color 2", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", "var(--color-that-does-not-end")),
                Arguments.arguments("Invalid n color 3", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", List.of("var(--color-that-does-not-end)"))),
                Arguments.arguments("Invalid n color 4", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", "var(--color/that-does-not-exist)")),
                Arguments.arguments("Invalid n color 5", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", List.of("var(--color/that-does-not-exist)"))),
                Arguments.arguments("Invalid n color 6", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", List.of(1234L))),
                Arguments.arguments("Invalid n color 7", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", "")),
                Arguments.arguments("Invalid n color 8", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", "not-a-css-variable")),
                Arguments.arguments("Invalid n color 9", (Consumer<ThemeDto>) themeDto -> themeDto.getNightModeUseCases().put("wrongColor", 123L))
        );
    }

    private static byte @NotNull [] readBody() throws IOException {
        return TestResources.getJarResourceAsBytes("/assets/plan/themes/default.json");
    }

    @BeforeEach
    void setUp(PlanFiles files) {
        saveThemeJSONResolver = new SaveThemeJSONResolver(files, responseFactory, gson);
        lenient().when(responseFactory.successResponse()).thenReturn(mock(Response.class));
        lenient().when(responseFactory.internalErrorResponse(any(), any())).thenAnswer(inv -> {
            throw new AssertionError(inv.getArgument(0));
        });
    }

    @Test
    void defaultThemeIsDeemedValid(PlanFiles files) throws IOException {
        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "default")));
        when(request.getRequestBody()).thenReturn(readBody());

        saveThemeJSONResolver.resolve(request);

        verify(responseFactory, times(0)).badRequest(any(), any());
        verify(responseFactory, times(1)).successResponse();

        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));
    }

    @Test
    @DisplayName("Invalid HTTP method for saving theme fails")
    void invalidMethod() {
        when(request.getMethod()).thenReturn("GET");
        assertThrows(MethodNotAllowedException.class, () -> saveThemeJSONResolver.resolve(request));
    }

    @Test
    @DisplayName("Missing theme parameter is invalid")
    void noThemeParameter() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of()));
        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));
    }

    @DisplayName("Theme parameter is invalid")
    @ParameterizedTest(name = "Invalid theme parameter {0}")
    @CsvSource({
            "bad-char-?",
            "space in name",
            "wrong_separator",
            "\\path\\traversal\\attempt",
            "/path/traversal/attempt",
            "C:PathTraversalAttempt",
            "https://xssRedirect",
            "javascript:alert(\"Hello\")",
            "oj6Tv@@g#n=40g@SVHVZ09xYTSPmpV$UZ!$wNpNh$aM8$HM0RonDter$odh5#hbeXY%1TVn#BHfApWbxNfP*0TFhV$rOr+dNPKe01", // Too long
            "8556279762\n3766524417",
            "8556279762\t3766524417"
    })
    void invalidThemeParameter(String themeParameter) {
        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", themeParameter)));
        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));
    }

    @DisplayName("Empty theme parameter is invalid")
    @Test
    void invalidEmptyThemeParameter() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "")));
        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));
    }

    @DisplayName("Theme request body is invalid")
    @ParameterizedTest(name = "Body modification {0}")
    @MethodSource("invalidThemeModificationsCases")
    void invalidBody(String name, Consumer<ThemeDto> modifications) throws IOException {
        System.out.println("Case: " + name);
        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "default")));
        byte[] body = readBodyAndModify(modifications);
        when(request.getRequestBody()).thenReturn(body);
        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));
    }

    private byte[] readBodyAndModify(Consumer<ThemeDto> modifications) throws IOException {
        byte[] read = readBody();
        ThemeDto result = gson.fromJson(new String(read, StandardCharsets.UTF_8), ThemeDto.class);
        modifications.accept(result);
        return gson.toJson(result).getBytes(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("Theme is renamed correctly")
    void themeIsRenamed(PlanFiles files) throws IOException {
        defaultThemeIsDeemedValid(files); // First success response
        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));

        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "changed", "originalName", "default")));
        byte[] body = readBodyAndModify(themeDto -> themeDto.setName("changed"));
        when(request.getRequestBody()).thenReturn(body);

        saveThemeJSONResolver.resolve(request); // 2nd success response

        verify(responseFactory, times(0)).badRequest(any(), any());
        verify(responseFactory, times(2)).successResponse();

        assertFalse(Files.exists(files.getThemeDirectory().resolve("default.json")));
        assertTrue(Files.exists(files.getThemeDirectory().resolve("changed.json")));
    }

    @Test
    @DisplayName("Theme is renamed incorrectly fails (body doesn't reflect name change)")
    void themeIsRenamedIncorrectly(PlanFiles files) throws IOException {
        defaultThemeIsDeemedValid(files);
        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));

        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "changed", "originalName", "default")));
        when(request.getRequestBody()).thenReturn(readBody());

        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));

        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));
    }

    @DisplayName("Theme is renamed incorrectly fails (incorrect original name parameter)")
    @ParameterizedTest(name = "Rename with invalid originalName {0} fails")
    @CsvSource({
            "bad-char-?",
            "space in name",
            "wrong_separator",
            "\\path\\traversal\\attempt",
            "/path/traversal/attempt",
            "C:PathTraversalAttempt",
            "https://xssRedirect",
            "javascript:alert(\"Hello\")",
            "855627976237665244177624378941", // Too long
            "8556279762\n3766524417",
            "8556279762\t3766524417",
            "does-not-exist"
    })
    void invalidOriginalName(String themeParameter, PlanFiles files) throws IOException {
        defaultThemeIsDeemedValid(files);
        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));

        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "changed", "originalName", themeParameter)));
        byte[] body = readBodyAndModify(themeDto -> themeDto.setName("changed"));
        when(request.getRequestBody()).thenReturn(body);

        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));

        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));
        assertTrue(Files.exists(files.getThemeDirectory().resolve("changed.json")));
    }

    @Test
    @DisplayName("Theme is renamed incorrectly fails (empty original name parameter)")
    void invalidEmptyOriginalName(PlanFiles files) throws IOException {
        defaultThemeIsDeemedValid(files);
        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));

        when(request.getMethod()).thenReturn("POST");
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "changed", "originalName", "")));
        byte[] body = readBodyAndModify(themeDto -> themeDto.setName("changed"));
        when(request.getRequestBody()).thenReturn(body);

        assertThrows(BadRequestException.class, () -> saveThemeJSONResolver.resolve(request));

        assertTrue(Files.exists(files.getThemeDirectory().resolve("default.json")));
        assertTrue(Files.exists(files.getThemeDirectory().resolve("changed.json")));
    }
}