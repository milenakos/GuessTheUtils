package com.aembr.guesstheutils.utils;

import com.aembr.guesstheutils.GuessTheUtils;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Message {
    public static void sendMessage(String message) {
        if (GuessTheUtils.CLIENT.player != null) {
            if (message.startsWith("/")) {
                GuessTheUtils.CLIENT.player.networkHandler.sendChatCommand(message.substring(1));
            } else {
                GuessTheUtils.CLIENT.inGameHud.getChatHud().addToMessageHistory(message);
                GuessTheUtils.CLIENT.player.networkHandler.sendChatMessage(message);
            }
        }
    }

    public static void displayMessage(String message) {
        if (GuessTheUtils.CLIENT == null || GuessTheUtils.CLIENT.player == null) return;
        GuessTheUtils.CLIENT.player.sendMessage(GuessTheUtils.prefix.copy()
                .append(Text.literal(message).formatted(Formatting.GRAY)), false);
    }

    public static void displayMessage(Text message) {
        if (GuessTheUtils.CLIENT == null || GuessTheUtils.CLIENT.player == null) return;
        GuessTheUtils.CLIENT.player.sendMessage(GuessTheUtils.prefix.copy().append(message), false);
    }
}
