package com.aembr.guesstheutils.modules;

import com.aembr.guesstheutils.GTBEvents;
import com.aembr.guesstheutils.GuessTheUtils;
import com.aembr.guesstheutils.Utils;
import com.aembr.guesstheutils.config.GuessTheUtilsConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ShortcutReminder extends GTBEvents.Module {
    private static final String GITHUB_BASE_URL = "https://raw.githubusercontent.com/zmh-program/gtb_platform/refs/heads/main/lib/source/";
    private static final String LAST_CHECKED_FILENAME = "shortcuts-last-checked";
    private static final String LAST_UPDATED_FILENAME = "versions.json";
    private static final String TRANSLATIONS_DATA_FILENAME = "translations-data.json";

    private static final Formatting SHORTCUT_COLOR = Formatting.GOLD;
    private static final Formatting THEME_COLOR = Formatting.GREEN;

    private List<TranslationData> translationData;

    String currentTheme = "";
    List<ShortcutEntry> currentShortcuts = new ArrayList<>();

    public ShortcutReminder(GTBEvents events) {
        super(events);
        events.subscribe(GTBEvents.ThemeUpdateEvent.class, this::onThemeUpdate, this);
        events.subscribe(GTBEvents.RoundStartEvent.class, e -> this.reset(), this);
        events.subscribe(GTBEvents.RoundEndEvent.class, e -> this.reset(), this);
    }

    public void init() {
        try {
            String lastCheckedDate = readFile(LAST_CHECKED_FILENAME);
            if (!lastCheckedDate.equals(LocalDate.now().toString())) {
                GuessTheUtils.LOGGER.info("Checking for shortcut list updates...");
                String lastUpdatedContent = downloadFile(GITHUB_BASE_URL + LAST_UPDATED_FILENAME);
                String localLastUpdatedContent = readFile(LAST_UPDATED_FILENAME);

                if (!lastUpdatedContent.equals(localLastUpdatedContent)) {
                    GuessTheUtils.LOGGER.info("Shortcut list update available! Downloading...");
                    writeFile(LAST_UPDATED_FILENAME, lastUpdatedContent);
                    String shortcutsContent = downloadFile(GITHUB_BASE_URL + TRANSLATIONS_DATA_FILENAME);
                    writeFile(TRANSLATIONS_DATA_FILENAME, shortcutsContent);
                }

                writeFile(LAST_CHECKED_FILENAME, LocalDate.now().toString());
            } else {
                GuessTheUtils.LOGGER.info("Skipping shortcut list update check, as we already " +
                        "checked today");
            }

            translationData = loadConfig(TRANSLATIONS_DATA_FILENAME);
            GuessTheUtils.LOGGER.info("Shortcut list loaded!");

        } catch (IOException e) {
            GuessTheUtils.LOGGER.error(e.getMessage());
        }
    }

    public void onThemeUpdate(GTBEvents.ThemeUpdateEvent event) {
        if (!GuessTheUtilsConfig.CONFIG.instance().enableShortcutReminderModule) return;
        if (event.theme().contains("_") || event.theme().equals(currentTheme)) return;

        currentTheme = event.theme();

        Optional<TranslationData> result = translationData.stream()
                .filter(data -> data.theme.equals(currentTheme))
                .findFirst();

        if (result.isEmpty()) return;

        Map<List<String>, String> occurrencesMap = new HashMap<>();

        result.get().multiwords.forEach(multiword -> {
            List<String> occurrences = multiword.occurrences.stream()
                    .map(Occurrence::theme)
                    .distinct()
                    .collect(Collectors.toList());

            if (GuessTheUtilsConfig.CONFIG.instance().shortcutReminderIncludeLatinOnlyShortcuts
                    && !isAscii(multiword.multiword)) {
                return;
            }

            if (!occurrences.isEmpty()) {
                occurrencesMap.merge(occurrences, multiword.multiword, (existing, newMultiword) ->
                        newMultiword.length() < existing.length() ? newMultiword : existing);
            }
        });

        occurrencesMap.forEach((themes, shortcut) ->
                currentShortcuts.add(new ShortcutEntry(shortcut, themes)));

        if (result.get().shortcut != null && isAscii(result.get().shortcut) && currentShortcuts.stream()
                .noneMatch(sc -> sc.shortcut.equals(result.get().shortcut))) {
            currentShortcuts.add(new ShortcutEntry(result.get().shortcut, List.of(currentTheme)));
        }

        if (!currentShortcuts.isEmpty()) {
            currentShortcuts.sort(Comparator.comparingInt(ShortcutEntry::getShortcutLength)
                    .thenComparing(Comparator.comparingInt(ShortcutEntry::getThemeCount).reversed()));
            printShortcutReminder(currentShortcuts);
        }
    }

    private boolean isAscii(String str) {
        return str.chars().allMatch(c -> c < 128);
    }

    private void printShortcutReminder(List<ShortcutEntry> shortcuts) {
        MutableText reminderMessage = Text.empty();
        for (int i = 0; i < shortcuts.size(); i++) {
            ShortcutEntry entry = shortcuts.get(i);
            String shortcut = entry.shortcut;
            List<String> themes = entry.themes;

            MutableText shortcutText =
                    Text.literal(" · ").formatted(Formatting.GRAY)
                            .append(Text.literal(shortcut)
                                    .formatted(SHORTCUT_COLOR)
                                    .formatted(Formatting.BOLD));

            reminderMessage.append(shortcutText)
                    .append(Text.literal(": ").formatted(Formatting.GRAY));

            for (int j = 0; j < themes.size(); j++) {
                MutableText themeText = Text.literal(themes.get(j))
                        .formatted(THEME_COLOR);

                reminderMessage.append(themeText);

                if (j < themes.size() - 1) {
                    reminderMessage.append(Text.literal(", ").formatted(Formatting.GRAY));
                }
            }

            if (i < shortcuts.size() - 1) {
                reminderMessage.append(Text.literal("\n"));
            }
        }
        Utils.sendMessage(Text.literal("Shortcuts for "+ currentTheme + ":\n")
                .append(reminderMessage));
    }

    public void reset() {
        currentTheme = "";
        currentShortcuts = new ArrayList<>();
    }

    private String readFile(String filename) throws IOException {
        File file = new File(GuessTheUtilsConfig.CONFIG_PATH.toFile(), filename);
        return file.exists() ? new String(Files.readAllBytes(file.toPath())).trim() : "";
    }

    private void writeFile(String filename, String content) throws IOException {
        Files.write(new File(GuessTheUtilsConfig.CONFIG_PATH.toFile(), filename).toPath(), content.getBytes());
    }

    @SuppressWarnings("SameParameterValue")
    private String downloadFile(String fileUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to download file: " + fileUrl);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             StringWriter writer = new StringWriter()) {
            String line;
            while ((line = in.readLine()) != null) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
            return writer.toString().trim();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private List<TranslationData> loadConfig(String filename) {
        File configFile = new File(GuessTheUtilsConfig.CONFIG_PATH.toFile(), filename);
        Gson gson = new Gson();
        Type shortcutListType = new TypeToken<List<TranslationData>>() {}.getType();

        try (FileReader reader = new FileReader(configFile)) {
            return gson.fromJson(reader, shortcutListType);
        } catch (IOException e) {
            GuessTheUtils.LOGGER.error(e.getMessage());
            return List.of();
        }
    }

    public record ShortcutEntry(String shortcut, List<String> themes) {
        public int getThemeCount() {
                return themes.size();
            }

            public int getShortcutLength() {
                return shortcut.length();
            }
        }

    public record TranslationData(
            int id,
            String theme,
            String shortcut,
            List<Multiword> multiwords,
            Map<String, Translation> translations
    ) {}

    public record Multiword(
            String multiword,
            List<Occurrence> occurrences
    ) {}

    public record Occurrence(
            String theme,
            String reference
    ) {}

    public record Translation(
            String translation,
            boolean isApproved,
            String approvedAt
    ) {}
}
