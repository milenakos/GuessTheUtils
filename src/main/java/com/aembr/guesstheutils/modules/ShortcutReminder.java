package com.aembr.guesstheutils.modules;

import com.aembr.guesstheutils.GTBEvents;
import com.aembr.guesstheutils.GuessTheUtils;
import com.aembr.guesstheutils.Utils;
import com.aembr.guesstheutils.config.GuessTheUtilsConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.datafixers.kinds.IdF;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShortcutReminder extends GTBEvents.Module {
    private static final String GITHUB_BASE_URL = "https://raw.githubusercontent.com/zmh-program/gtb_platform/refs/heads/main/lib/source/";
    private static final String LAST_CHECKED_FILENAME = "shortcuts-last-checked";
    private static final String LAST_UPDATED_FILENAME = "versions.json";
    private static final String TRANSLATIONS_DATA_FILENAME = "translations-data.json";

    private static final Formatting SHORTCUT_COLOR = Formatting.GOLD;
    private static final Formatting THEME_COLOR = Formatting.GREEN;
    private static final Formatting SHOW_ALL_TEXT_COLOR = Formatting.YELLOW;

    private List<TranslationData> translationData;

    String currentTheme = "";
    List<ShortcutEntry> currentShortcuts = new ArrayList<>();
    List<ShortcutEntry> filteredShortcuts = new ArrayList<>();

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

        Optional<TranslationData> optionalResult = translationData.stream()
                .filter(data -> data.theme.equals(currentTheme))
                .findFirst();

        if (optionalResult.isEmpty()) return;
        TranslationData res = optionalResult.get();

        if (res.shortcut != null) currentShortcuts.add(new ShortcutEntry(res.shortcut, List.of(currentTheme)));

        res.multiwords.forEach(mw -> {
            List<String> occurrences = mw.occurrences.stream()
                    .map(Occurrence::theme)
                    .distinct()
                    .toList();

            currentShortcuts.add(new ShortcutEntry(mw.multiword, occurrences));
        });

        filteredShortcuts = filterShortcuts(new ArrayList<>(currentShortcuts),
                GuessTheUtilsConfig.CONFIG.instance().shortcutReminderIncludeLatinOnlyShortcuts);

        if (currentShortcuts.isEmpty()) {
            Utils.sendMessage(Text.literal("No shortcuts for ")
                    .append(Text.literal(currentTheme).formatted(THEME_COLOR))
                    .append(Text.literal(".")));
            return;
        }

        MutableText showAllText = Text.empty();

        if (filteredShortcuts.size() != currentShortcuts.size()) {
            //? if >=1.21.5 {
            HoverEvent hoverEvent = new HoverEvent.ShowText(compileShortcutsText(currentShortcuts));
            //?} else {
            /*HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,compileShortcutsText(currentShortcuts));
             *///?}

            showAllText = Text.literal("[Show All]")
                    .setStyle(Style.EMPTY.withHoverEvent(hoverEvent).withColor(SHOW_ALL_TEXT_COLOR));
        }

        if (filteredShortcuts.isEmpty()) {
            Utils.sendMessage(Text.literal("No suitable shortcuts for ")
                    .append(Text.literal(currentTheme).formatted(THEME_COLOR))
                    .append(Text.literal(" found. "))
                    .append(showAllText));
        } else {
            Utils.sendMessage(Text.literal("Shortcuts for ")
                    .append(Text.literal(currentTheme).formatted(THEME_COLOR))
                    .append(Text.literal(": "))
                    .append(showAllText)
                    .append("\n")
                    .append(compileShortcutsText(filteredShortcuts)));
        }
    }

    public static List<ShortcutEntry> filterShortcuts(List<ShortcutEntry> entries, boolean latinOnly) {
        if (latinOnly) {
            entries = entries.stream()
                    .filter(sc -> isAscii(sc.shortcut))
                    .toList();
        }

        List<ShortcutEntry> result = new ArrayList<>(entries);
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < result.size(); j++) {
                if (i == j) continue;

                ShortcutEntry current = result.get(i);
                ShortcutEntry comparison = result.get(j);
                if (new HashSet<>(comparison.themes).containsAll(current.themes)) {
                    if (comparison.shortcut.length() <= current.shortcut.length()) {
                        result.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private Text compileShortcutsText(List<ShortcutEntry> shortcuts) {
        shortcuts.sort(Comparator.comparingInt(ShortcutEntry::getShortcutLength)
                .thenComparing(Comparator.comparingInt(ShortcutEntry::getThemeCount).reversed()));

        MutableText text = Text.empty();
        for (int i = 0; i < shortcuts.size(); i++) {
            ShortcutEntry entry = shortcuts.get(i);
            String shortcut = entry.shortcut;
            List<String> themes = entry.themes;

            MutableText shortcutText =
                    Text.literal(" • ").formatted(Formatting.GRAY)
                            .append(Text.literal(shortcut)
                                    .formatted(SHORTCUT_COLOR)
                                    .formatted(Formatting.BOLD));

            shortcutText.append(Text.literal(": ").formatted(Formatting.GRAY));

            for (int j = 0; j < themes.size(); j++) {
                MutableText themeText = Text.literal(themes.get(j))
                        .formatted(THEME_COLOR);

                shortcutText.append(themeText);

                if (j < themes.size() - 1) {
                    shortcutText.append(Text.literal(" / ").formatted(Formatting.GRAY));
                }
            }

            if (i < shortcuts.size() - 1) {
                shortcutText.append(Text.literal("\n"));
            }

            text.append(shortcutText);
        }

        return text;
    }

    public void reset() {
        currentTheme = "";
        currentShortcuts = new ArrayList<>();
        filteredShortcuts = new ArrayList<>();
    }

    private static boolean isAscii(String str) {
        return str.chars().allMatch(c -> c < 128);
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
