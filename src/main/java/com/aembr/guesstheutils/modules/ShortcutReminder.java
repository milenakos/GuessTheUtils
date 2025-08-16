package com.aembr.guesstheutils.modules;

import com.aembr.guesstheutils.GTBEvents;
import com.aembr.guesstheutils.utils.TranslationData;
import com.aembr.guesstheutils.utils.Utils;
import com.aembr.guesstheutils.config.GuessTheUtilsConfig;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ShortcutReminder extends GTBEvents.Module {
    private static final Formatting SHORTCUT_COLOR = Formatting.GOLD;
    private static final Formatting THEME_COLOR = Formatting.GREEN;
    private static final Formatting SHOW_ALL_TEXT_COLOR = Formatting.YELLOW;

    String currentTheme = "";
    List<ShortcutEntry> currentShortcuts = new ArrayList<>();
    List<ShortcutEntry> filteredShortcuts = new ArrayList<>();

    public ShortcutReminder(GTBEvents events) {
        super(events);
        events.subscribe(GTBEvents.ThemeUpdateEvent.class, this::onThemeUpdate, this);
        events.subscribe(GTBEvents.RoundStartEvent.class, e -> this.reset(), this);
        events.subscribe(GTBEvents.RoundEndEvent.class, e -> this.reset(), this);
    }

    public void onThemeUpdate(GTBEvents.ThemeUpdateEvent event) {
        if (!GuessTheUtilsConfig.CONFIG.instance().enableShortcutReminderModule) return;
        if (event.theme().contains("_") || event.theme().equals(currentTheme)) return;

        currentTheme = event.theme();

        Optional<TranslationData.TranslationDataEntry> optionalResult = TranslationData.entries.stream()
                .filter(data -> data.theme().equals(currentTheme))
                .findFirst();

        if (optionalResult.isEmpty()) return;
        TranslationData.TranslationDataEntry res = optionalResult.get();

        if (res.shortcut() != null) currentShortcuts.add(new ShortcutEntry(res.shortcut(), List.of(currentTheme)));

        res.multiwords().forEach(mw -> {
            List<String> occurrences = mw.occurrences().stream()
                    .map(TranslationData.Occurrence::theme)
                    .distinct()
                    .toList();

            currentShortcuts.add(new ShortcutEntry(mw.multiword(), occurrences));
        });

        filteredShortcuts = filterShortcuts(new ArrayList<>(currentShortcuts),
                GuessTheUtilsConfig.CONFIG.instance().shortcutReminderFilterType);

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

    public static List<ShortcutEntry> filterShortcuts(List<ShortcutEntry> entries,
                                                      GuessTheUtilsConfig.ShortcutFilterType filterType) {
        List<ShortcutEntry> filteredEntries = filterEntriesByType(entries, filterType);

        List<ShortcutEntry> result = new ArrayList<>(filteredEntries);
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

    static List<ShortcutEntry> filterEntriesByType(List<ShortcutEntry> entries,
                                                   GuessTheUtilsConfig.ShortcutFilterType filterType) {
        return switch (filterType) {
            case CJK -> entries.stream()
                    .filter(entry -> !containsCJK(entry.shortcut))
                    .collect(Collectors.toList());
            case NON_ASCII -> entries.stream()
                    .filter(entry -> isAscii(entry.shortcut))
                    .collect(Collectors.toList());
            default -> entries;
        };
    }

    private static boolean containsCJK(String input) {
        return input.matches(".*[\\u4E00-\\u9FFF\\u3400-\\u4DBF\\uF900-\\uFAFF\\u3040-\\u309F\\u30A0-\\u30FF\\uAC00-\\uD7AF].*");
    }

    private static boolean isAscii(String shortcut) {
        return shortcut.codePoints()
                .allMatch(cp -> cp < 128);
    }

    public record ShortcutEntry(String shortcut, List<String> themes) {
        public int getThemeCount() {
                return themes.size();
            }
        public int getShortcutLength() {
                return shortcut.length();
            }
    }
}
