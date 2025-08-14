package com.aembr.guesstheutils.modules;

import com.aembr.guesstheutils.config.GuessTheUtilsConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ShortcutReminderTest {
    @Test
    void testShortcutFilteringTie() {
        List<ShortcutReminder.ShortcutEntry> input = List.of(
                new ShortcutReminder.ShortcutEntry("lampas", List.of("Genie Lamp", "Lamp", "Lantern")),
                new ShortcutReminder.ShortcutEntry("lanterna", List.of("Flashlight", "Lantern")),
                new ShortcutReminder.ShortcutEntry("latarnia", List.of("Streetlamp", "Lantern")),
                new ShortcutReminder.ShortcutEntry("linterna", List.of("Flashlight", "Lantern"))
        );
        List<ShortcutReminder.ShortcutEntry> expected = List.of(
                new ShortcutReminder.ShortcutEntry("lampas", List.of("Genie Lamp", "Lamp", "Lantern")),
                new ShortcutReminder.ShortcutEntry("latarnia", List.of("Streetlamp", "Lantern")),
                new ShortcutReminder.ShortcutEntry("linterna", List.of("Flashlight", "Lantern"))
        );
        List<ShortcutReminder.ShortcutEntry> actual = ShortcutReminder.filterShortcuts(input, GuessTheUtilsConfig.ShortcutFilterType.CJK);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testShortcutFilteringKeepShort() {
        List<ShortcutReminder.ShortcutEntry> input = List.of(
                new ShortcutReminder.ShortcutEntry("lampas", List.of("Genie Lamp", "Lamp", "Lantern")),
                new ShortcutReminder.ShortcutEntry("lampa", List.of("Genie Lamp", "Lamp")),
                new ShortcutReminder.ShortcutEntry("lampada", List.of("Lamp", "Lightbulb"))
        );
        List<ShortcutReminder.ShortcutEntry> expected = List.of(
                new ShortcutReminder.ShortcutEntry("lampas", List.of("Genie Lamp", "Lamp", "Lantern")),
                new ShortcutReminder.ShortcutEntry("lampa", List.of("Genie Lamp", "Lamp")),
                new ShortcutReminder.ShortcutEntry("lampada", List.of("Lamp", "Lightbulb"))
        );
        List<ShortcutReminder.ShortcutEntry> actual = ShortcutReminder.filterShortcuts(input, GuessTheUtilsConfig.ShortcutFilterType.CJK);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testShortcutFilteringAnotherShort() {
        List<ShortcutReminder.ShortcutEntry> input = List.of(
                new ShortcutReminder.ShortcutEntry("zene", List.of("Music")),
                new ShortcutReminder.ShortcutEntry("musik", List.of("Music", "Musical")),
                new ShortcutReminder.ShortcutEntry("musiikki", List.of("Music", "Musical")),
                new ShortcutReminder.ShortcutEntry("музика", List.of("Music", "Musical")),
                new ShortcutReminder.ShortcutEntry("музыка", List.of("Music", "Musical"))
        );
        List<ShortcutReminder.ShortcutEntry> expected = List.of(
                new ShortcutReminder.ShortcutEntry("zene", List.of("Music")),
                new ShortcutReminder.ShortcutEntry("musik", List.of("Music", "Musical"))
        );
        List<ShortcutReminder.ShortcutEntry> actual = ShortcutReminder.filterShortcuts(input, GuessTheUtilsConfig.ShortcutFilterType.CJK);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testShortcutFilteringCJK() {
        List<ShortcutReminder.ShortcutEntry> input = List.of(
                new ShortcutReminder.ShortcutEntry("財布", List.of("Purse", "Wallet")),
                new ShortcutReminder.ShortcutEntry("钱包", List.of("Purse", "Wallet")),
                new ShortcutReminder.ShortcutEntry("지갑", List.of("Purse", "Wallet")),
                new ShortcutReminder.ShortcutEntry("リグマーボールズ", new ArrayList<>()),
                new ShortcutReminder.ShortcutEntry("hello", new ArrayList<>()),
                new ShortcutReminder.ShortcutEntry("кубикрубика", new ArrayList<>()),
                new ShortcutReminder.ShortcutEntry("Čtyřkolka", new ArrayList<>())
        );
        List<ShortcutReminder.ShortcutEntry> expected = List.of(
                new ShortcutReminder.ShortcutEntry("hello", new ArrayList<>()),
                new ShortcutReminder.ShortcutEntry("кубикрубика", new ArrayList<>()),
                new ShortcutReminder.ShortcutEntry("Čtyřkolka", new ArrayList<>())
        );
        List<ShortcutReminder.ShortcutEntry> actual = ShortcutReminder.filterEntriesByType(input, GuessTheUtilsConfig.ShortcutFilterType.CJK);
        Assertions.assertEquals(expected, actual);
    }
}
