package org.cascadebot.cascadebot.commands.music;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.cascadebot.cascadebot.commandmeta.CommandContext;
import org.cascadebot.cascadebot.commandmeta.ICommandMain;
import org.cascadebot.cascadebot.commandmeta.Module;
import org.cascadebot.cascadebot.data.language.Locale;
import org.cascadebot.cascadebot.permissions.CascadePermission;
import org.cascadebot.cascadebot.utils.FormatUtils;
import org.cascadebot.cascadebot.utils.ParserUtils;

public class SeekCommand implements ICommandMain {

    @Override
    public void onCommand(Member sender, CommandContext context) {
        if (context.getArgs().length == 0) {
            context.getUIMessaging().replyUsage();
            return;
        }

        String time = context.getMessage(0);
        long millis = 0;

        VoiceChannel memberVoiceChannel = context.getMember().getVoiceState().getChannel();
        if (context.getMusicPlayer().getPlayer().getPlayingTrack() == null || !context.getMusicPlayer().getConnectedChannel().equals(memberVoiceChannel)) {
            context.getTypedMessaging().replyDanger(context.i18n("commands.seek.no_music_playing"));
            return;
        }
        if (context.getMusicPlayer().getPlayer().getPlayingTrack().getInfo().isStream) {
            context.getTypedMessaging().replyDanger(context.i18n("commands.seek.music_is_stream"));
            return;
        }


        try {
            millis = ParserUtils.parseTime(time);
        } catch (IllegalArgumentException e) {
            context.getTypedMessaging().replyDanger(context.i18n("commands.seek.bad_format"));
        }

        if (millis < 0) {
            context.getTypedMessaging().replyDanger(context.i18n("commands.seek.negative_value"));
            return;
        } else if (millis > context.getMusicPlayer().getPlayer().getPlayingTrack().getDuration()) {
            context.getTypedMessaging().replyDanger(context.i18n("commands.seek.duration_shorter"));
            return;
        }

        context.getMusicPlayer().getPlayer().seekTo(millis);
        // TODO: Binary make a method to format time using ICU
        String formattedTime = FormatUtils.formatTime(millis, Locale.getDefaultLocale(), true);
        context.getTypedMessaging().replySuccess(context.i18n("commands.seek.success", formattedTime));
    }

    @Override
    public Module getModule() {
        return Module.MUSIC;
    }

    @Override
    public String command() {
        return "seek";
    }

    @Override
    public CascadePermission getPermission() {
        return CascadePermission.of("seek", true);
    }

}
