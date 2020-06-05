/*
 * Copyright (c) 2019 CascadeBot. All rights reserved.
 * Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.commands.music;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.MainCommand;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.permissions.CascadePermission;

public class LeaveCommand extends MainCommand {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        VoiceChannel voiceChannel = context.getMusicPlayer().getConnectedChannel();
        if (voiceChannel == null) {
            context.getTypedMessaging().replyDanger(context.i18n("responses.voice_not_connected"));
            return;
        }

        if (!sender.getVoiceState().inVoiceChannel() || !sender.getVoiceState().getChannel().equals(voiceChannel)) {
            if (!context.hasPermission("leave.other")) {
                context.getUiMessaging().sendPermissionError("leave.other");
                return;
            }
        }
        context.getMusicPlayer().leave();
        context.getTypedMessaging().replySuccess(context.i18n("commands.leave.successfully_left", voiceChannel.getName()));
    }

    @Override
    public Module module() {
        return Module.MUSIC;
    }

    @Override
    public String command() {
        return "leave";
    }

    @Override
    public CascadePermission permission() {
        return CascadePermission.of("leave", true);
    }

}
