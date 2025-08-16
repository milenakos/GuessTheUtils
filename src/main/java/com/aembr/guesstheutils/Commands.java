package com.aembr.guesstheutils;

import com.aembr.guesstheutils.utils.Message;
import com.aembr.guesstheutils.utils.Scheduler;
import com.aembr.guesstheutils.utils.TranslationData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Commands {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("guesstheutils")
                .then(ClientCommandManager.literal("replay")
                        .then(ClientCommandManager.literal("save")
                            .executes((command) -> {
                                GuessTheUtils.replay.save();
                                return 1;
                            }))

                        .then(ClientCommandManager.literal("open")
                            .executes((command) -> {
                                Util.getOperatingSystem().open(Replay.replayDir);
                                return 1;
                            })))

                .then(ClientCommandManager.literal("livetest")
                        .executes((command) -> {
                            GuessTheUtils.testing = !GuessTheUtils.testing;
                            if (GuessTheUtils.testing) GuessTheUtils.liveE2ERunner.currentTick = 0;
                            return 1;
                        }))

                .then(ClientCommandManager.literal("config")
                        .executes((command) -> {
                            GuessTheUtils.openConfig = true;
                            return 1;
                        }))
        );

        dispatcher.register(ClientCommandManager.literal("translate")
                .executes(command -> {
                    Message.displayMessage(Text.literal("/translate usage:\n")
                            .append("/translate <language> <theme> for a specific language translation.\n")
                            .append("/translate all <theme> for all translations.\n")
                            .append("<theme> accepts shortcuts, lowercase, or without spaces."));
                    return 1;
                })
                .then(ClientCommandManager.argument("language", StringArgumentType.string())
                        .then(ClientCommandManager.argument("theme", StringArgumentType.greedyString())
                                .executes(command -> {
                                    String lang = StringArgumentType.getString(command, "language");
                                    String theme = StringArgumentType.getString(command, "theme");
                                    printTranslation(lang, theme);
                                    return 1;
                                }))));

        dispatcher.register(ClientCommandManager.literal("qgtb")
                .executes(command -> {
                    Message.sendMessage("/queue build_battle_guess_the_build");
                    return 1;
                }));

        dispatcher.register(ClientCommandManager.literal("lrj")
                .executes(command -> {
                    Message.sendMessage("/hub");
                    Scheduler.schedule(20, () -> Message.sendMessage("/back"));
                    return 1;
                }));
    }

    private static void printTranslation(String lang, String theme) {
        TranslationData.TranslationDataEntry entry = TranslationData.entries.stream()
                .filter(e -> e.theme().equalsIgnoreCase(theme)
                        || e.theme().replace(" ", "").equalsIgnoreCase(theme.replace(" ", ""))
                        || Objects.equals(e.shortcut(), theme)).findAny().orElse(null);

        if (entry == null) {
            Message.displayMessage(Text.literal("Theme not found!").formatted(Formatting.RED));
            return;
        }

        if (lang.equals("all")) {
            MutableText result = Text.empty();
            Map<String, TranslationData.Translation> translations = entry.translations();
            for (Map.Entry<String, TranslationData.Translation> translation : translations.entrySet()) {
                if (!translation.getValue().isApproved()) continue;

                //? if >=1.21.5 {
                ClickEvent clickEvent = new ClickEvent.CopyToClipboard(translation.getValue().translation());
                //?} else {
                /*ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                        translation.getValue().translation());
                 *///?}

                result.append(Text.literal("\n • ").formatted(Formatting.GRAY))
                        .append(Text.literal(translation.getKey()).formatted(Formatting.AQUA))
                        .append(Text.literal(": ").formatted(Formatting.GRAY))
                        .append(Text.literal(translation.getValue().translation())
                                .formatted(Formatting.GOLD).formatted(Formatting.BOLD))
                        .append(Text.literal(" [Copy]").setStyle(
                                Style.EMPTY.withClickEvent(clickEvent).withColor(Formatting.YELLOW)));
            }

            Message.displayMessage(Text.empty().append(Text.literal("All translations for theme ").formatted(Formatting.GRAY))
                    .append(Text.literal(entry.theme()).formatted(Formatting.GREEN))
                    .append(":").formatted(Formatting.GRAY)
                    .append(result));
            return;
        }

        TranslationData.Translation translation = entry.translations().get(lang.toLowerCase());
        if (translation == null) {
            List<String> validLanguages = entry.translations().entrySet().stream()
                            .filter(e -> e.getValue().isApproved())
                    .map(Map.Entry::getKey).toList();

            Message.displayMessage(Text.literal("Language not found. Try /translate <theme> for " +
                            "all translations, or pick a language from the following list:\n")
                    .append(Text.literal(validLanguages.toString())).formatted(Formatting.RED));
            return;
        }

        if (!translation.isApproved()) {
            Message.displayMessage(Text.empty().append(Text.literal(entry.theme()).formatted(Formatting.GREEN))
                    .append(Text.literal(" has no approved ").formatted(Formatting.RED))
                    .append(Text.literal(lang.toLowerCase()).formatted(Formatting.AQUA))
                    .append(Text.literal(" translation.").formatted(Formatting.RED)));
            return;
        }

        //? if >=1.21.5 {
        ClickEvent clickEvent = new ClickEvent.CopyToClipboard(translation.translation());
        //?} else {
        /*ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
                translation.translation());
         *///?}

        Message.displayMessage(Text.empty().append(Text.literal(lang.toLowerCase()).formatted(Formatting.AQUA))
                .append(" translation for ").formatted(Formatting.GRAY)
                .append(Text.literal(entry.theme()).formatted(Formatting.GREEN))
                .append(": ").formatted(Formatting.GRAY)
                .append(Text.literal(translation.translation())
                        .formatted(Formatting.GOLD).formatted(Formatting.BOLD))
                .append(Text.literal(" [Copy]").setStyle(
                        Style.EMPTY.withClickEvent(clickEvent).withColor(Formatting.YELLOW))));
    }
}