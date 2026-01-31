package com.aembr.guesstheutils;

import com.aembr.guesstheutils.utils.Message;
import com.aembr.guesstheutils.utils.Scheduler;
import com.aembr.guesstheutils.utils.TranslationData;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Map;

public class Commands {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("guesstheutils")
                .then(ClientCommandManager.literal("replay")
                        .then(ClientCommandManager.literal("save")
                            .executes((command) -> {
                                GuessTheUtils.replay.save();
                                return Command.SINGLE_SUCCESS;
                            }))

                        .then(ClientCommandManager.literal("open")
                            .executes((command) -> {
                                Util.getOperatingSystem().open(Replay.replayDir);
                                return Command.SINGLE_SUCCESS;
                            })))

//                .then(ClientCommandManager.literal("livetest")
//                        .executes((command) -> {
//                            GuessTheUtils.testing = !GuessTheUtils.testing;
//                            if (GuessTheUtils.testing) GuessTheUtils.liveE2ERunner.currentTick = 0;
//                            return Command.SINGLE_SUCCESS;
//                        }))

                .then(ClientCommandManager.literal("config")
                        .executes((command) -> {
                            GuessTheUtils.openConfig = true;
                            return Command.SINGLE_SUCCESS;
                        }))
        );

        dispatcher.register(ClientCommandManager.literal("gettranslation")
                .then(ClientCommandManager.argument("theme", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            TranslationData.entries.stream()
                                    .map(TranslationData.TranslationDataEntry::theme)
                                    .map(theme -> theme.replace(" ", "_"))
                                    .filter(theme -> theme.toLowerCase().contains(builder.getRemainingLowerCase()))
                                    .forEach(builder::suggest);
                            return builder.buildFuture();
                        })
                        .then(ClientCommandManager.argument("language", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String themeWithUnderscores = StringArgumentType.getString(ctx, "theme");
                                    String theme = themeWithUnderscores.replace("_", " ");

                                    TranslationData.entries.stream()
                                            .filter(entry -> entry.theme().equals(theme))
                                            .findFirst()
                                            .ifPresent(entry -> {entry.translations().entrySet().stream()
                                                        .filter(langEntry -> langEntry.getValue().isApproved())
                                                        .map(Map.Entry::getKey)
                                                        .filter(langCode -> langCode.toLowerCase().contains(builder.getRemainingLowerCase()))
                                                        .forEach(builder::suggest);
                                            });
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String themeWithUnderscores = StringArgumentType.getString(ctx, "theme");
                                    String theme = themeWithUnderscores.replace("_", " ");
                                    String languageCode = StringArgumentType.getString(ctx, "language");
                                    printTranslation(languageCode, theme);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("qgtb")
                .executes(command -> {
                    Message.sendMessage("/queue build_battle_guess_the_build");
                    return Command.SINGLE_SUCCESS;
                }));

        dispatcher.register(ClientCommandManager.literal("lrj")
                .executes(command -> {
                    Message.sendMessage("/hub");
                    Scheduler.schedule(20, () -> Message.sendMessage("/back"));
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static void printTranslation(String lang, String theme) {
        TranslationData.entries.stream()
                .filter(entry -> entry.theme().equals(theme))
                .findFirst()
                .ifPresent(entry -> {
                    TranslationData.Translation translation = entry.translations().get(lang);

                    if (translation != null && translation.isApproved()) {
                        //? if >=1.21.5 {
                        ClickEvent clickEvent = new ClickEvent.SuggestCommand("➤ " + translation.translation());
                        //?} else {
                        /*ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "➤ " + translation.translation());
                         *///?}

                        //? if >=1.21.5 {
                        HoverEvent hoverEvent = new HoverEvent.ShowText(Text.literal("Click to draft this translation"));
                        //?} else {
                        /*HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to draft this translation"));
                         *///?}

                        Text draftButtonText = Text.literal(" [Draft]").setStyle(Style.EMPTY
                                .withClickEvent(clickEvent)
                                .withHoverEvent(hoverEvent)
                                .withColor(Formatting.YELLOW));

                        Message.displayMessage(Text.empty()
                                .append(Text.literal(theme).formatted(Formatting.GREEN))
                                .append(Text.literal(" in ").formatted(Formatting.GRAY))
                                .append(Text.literal(lang).formatted(Formatting.AQUA))
                                .append(Text.literal(": ").formatted(Formatting.GRAY))
                                .append(Text.literal(translation.translation()).formatted(Formatting.GOLD).formatted(Formatting.BOLD))
                                .append(draftButtonText));
                    } else {
                        Message.displayMessage(Text.empty()
                                .append(Text.literal("No approved translation found for ").formatted(Formatting.RED))
                                .append(Text.literal(theme).formatted(Formatting.GREEN))
                                .append(Text.literal(" in ").formatted(Formatting.RED))
                                .append(Text.literal(lang).formatted(Formatting.AQUA))
                                .append(Text.literal(".").formatted(Formatting.RED)));
                    }
                });
    }
}
