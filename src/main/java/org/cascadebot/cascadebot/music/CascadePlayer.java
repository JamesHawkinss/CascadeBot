/*
 * Copyright (c) 2020 CascadeBot. All rights reserved.
 *  Licensed under the MIT license.
 */

package org.cascadebot.cascadebot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lavalink.client.player.IPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.cascadebot.cascadebot.CascadeBot;
import org.cascadebot.cascadebot.data.managers.PlaylistManager;
import org.cascadebot.cascadebot.data.objects.LoadPlaylistResult;
import org.cascadebot.cascadebot.data.objects.LoopMode;
import org.cascadebot.cascadebot.data.objects.Playlist;
import org.cascadebot.cascadebot.data.objects.PlaylistType;
import org.cascadebot.cascadebot.data.objects.SavePlaylistResult;
import org.cascadebot.cascadebot.utils.StringsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface CascadePlayer extends IPlayer {

    Queue<AudioTrack> queue = new LinkedList<>();

    AtomicLong guildId = new AtomicLong();

    AtomicReference<LoopMode> loopMode = new AtomicReference<>(LoopMode.DISABLED);
    AtomicBoolean shuffleEnabled = new AtomicBoolean(false);

    default double getQueueLength() {
        double queueLength = getPlayingTrack().getDuration() - getTrackPosition();
        for (AudioTrack track : queue) {
            queueLength += track.getDuration();
        }
        return queueLength;
    }

    /**
     * Gets the progress bar for the current track
     *
     * @return The progress bar for the current track
     */
    default String getTrackProgressBar(boolean embed) {
        float process = (100f / getPlayingTrack().getDuration() * getTrackPosition());
        if (embed) {
            return StringsUtil.getProgressBarEmbed(process);
        } else {
            return StringsUtil.getProgressBar(process);
        }
    }

    default String getArtwork() {
        if (getPlayingTrack().getSourceManager().getSourceName().equals(CascadeBot.INS.getMusicHandler().getYoutubeSourceName())) {
            return "https://img.youtube.com/vi/" + getPlayingTrack().getIdentifier() + "/hqdefault.jpg";
        }
        if (getPlayingTrack().getSourceManager().getSourceName().equals(CascadeBot.INS.getMusicHandler().getTwitchSourceName())) {
            String[] split = getPlayingTrack().getInfo().identifier.split("/");
            return "https://static-cdn.jtvnw.net/previews-ttv/live_user_" + split[split.length - 1] + "-500x400.jpg";
        }
        return null;
    }

    default void addTrack(AudioTrack track) {
        if (getPlayingTrack() != null) {
            queue.add(track);
        } else {
            playTrack(track);
        }
    }

    default void addTracks(Collection<AudioTrack> tracks) {
        tracks.forEach(this::addTrack);
    }

    default void loopMode(LoopMode loopMode) {
        this.loopMode.set(loopMode);
    }

    default boolean toggleShuffleOnRepeat() {
        boolean current = shuffleEnabled.get();
        shuffleEnabled.set(!current);
        return current;
    }

    default void shuffle() {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        Collections.shuffle(tracks);
        queue.clear();
        queue.addAll(tracks);
    }

    default boolean isShuffleEnabled() {
        return shuffleEnabled.get();
    }

    default LoopMode getLoopMode() {
        return loopMode.get();
    }

    default Queue<AudioTrack> getQueue() {
        return queue;
    }

    default void skip() {
        stopTrack();
    }

    void join(VoiceChannel channel);

    void leave();

    VoiceChannel getConnectedChannel();

    default Guild getGuild() {
        return CascadeBot.INS.getShardManager().getGuildById(guildId.get());
    }

    default void stop() {
        queue.clear();
        loopMode.set(LoopMode.DISABLED);
        stopTrack();
    }

    default void loadLink(String input, TrackData trackData, Consumer<String> noMatchConsumer, Consumer<FriendlyException> exceptionConsumer, Consumer<List<AudioTrack>> resultTracks) {
        CascadeBot.INS.getMusicHandler().getPlayerManager().loadItem(input, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                audioTrack.setUserData(trackData);
                resultTracks.accept(Collections.singletonList(audioTrack));
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                List<AudioTrack> tracks = new ArrayList<>();
                for (AudioTrack track : audioPlaylist.getTracks()) {
                    track.setUserData(track);
                    tracks.add(track);
                }
                resultTracks.accept(Collections.unmodifiableList(tracks));
            }

            @Override
            public void noMatches() {
                noMatchConsumer.accept(input);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                exceptionConsumer.accept(e);
            }
        });
    }

    default void loadPlaylist(String name, TrackData trackData, BiConsumer<LoadPlaylistResult, List<AudioTrack>> consumer) {
        Playlist guild = PlaylistManager.getPlaylistByName(getGuild().getIdLong(), PlaylistType.GUILD, name);
        Playlist user = PlaylistManager.getPlaylistByName(trackData.getUserId(), PlaylistType.USER, name);
        if (guild != null && user != null) {
            consumer.accept(LoadPlaylistResult.EXISTS_IN_ALL_SCOPES, null);
        } else if (guild != null) {
            loadLoadedPlaylist(guild, trackData, tracks -> {
                consumer.accept(LoadPlaylistResult.LOADED_GUILD, tracks);
            });
        } else if (user != null) {
            loadLoadedPlaylist(user, trackData, tracks -> {
                consumer.accept(LoadPlaylistResult.LOADED_USER, tracks);
            });
        } else {
            consumer.accept(LoadPlaylistResult.DOESNT_EXIST, null);
        }
    }

    default void loadPlaylist(String name, TrackData trackData, PlaylistType scope, BiConsumer<LoadPlaylistResult, List<AudioTrack>> consumer) {
        LoadPlaylistResult result = LoadPlaylistResult.DOESNT_EXIST;
        long owner = 0;
        switch (scope) {
            case GUILD:
                result = LoadPlaylistResult.LOADED_GUILD;
                owner = getGuild().getIdLong();
                break;
            case USER:
                result = LoadPlaylistResult.LOADED_USER;
                owner = trackData.getUserId();
                break;
        }
        Playlist playlist = PlaylistManager.getPlaylistByName(owner, scope, name);
        if (playlist == null) {
            consumer.accept(LoadPlaylistResult.DOESNT_EXIST, null);
            return;
        }

        LoadPlaylistResult loadPlaylistResult = result;
        loadLoadedPlaylist(playlist, trackData, tracks -> {
            consumer.accept(loadPlaylistResult, tracks);
        });
    }

    default void loadLoadedPlaylist(Playlist playlist, TrackData trackData, Consumer<List<AudioTrack>> loadedConsumer) {
        List<AudioTrack> tracks = new ArrayList<>();
        for (String url : playlist.getTracks()) {
            loadLink(url, trackData, noMatch -> {
                playlist.removeTrack(url);
            }, exception -> {
                playlist.removeTrack(url);
            }, loadedTracks -> {
                tracks.addAll(loadedTracks);
                if (tracks.size() == playlist.getTracks().size()) {
                    loadedConsumer.accept(tracks);
                }
            });
        }
    }

    default SavePlaylistResult saveCurrentPlaylist(long owner, PlaylistType scope, String name, boolean overwrite) {
        List<AudioTrack> tracks = new ArrayList<>();
        tracks.add(getPlayingTrack());
        tracks.addAll(this.queue);

        List<String> ids = new ArrayList<>();
        for (AudioTrack track : tracks) {
            ids.add(track.getIdentifier());
        }

        Playlist search = PlaylistManager.getPlaylistByName(owner, scope, name);
        if (search != null) {
            if (overwrite) {
                search.setTracks(ids);
                PlaylistManager.replacePlaylist(search);
                return SavePlaylistResult.OVERWRITE;
            } else {
                return SavePlaylistResult.ALREADY_EXISTS;
            }
        } else {
            PlaylistManager.savePlaylist(new Playlist(owner, name, scope, ids));
            return SavePlaylistResult.NEW;
        }
    }

    default void removeTrack(int index) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        tracks.remove(index);
        queue.clear();
        queue.addAll(tracks);
    }

    default void moveTrack(int track, int pos) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        AudioTrack trackToMove = tracks.get(track);
        if (pos >= tracks.size()) {
            // Moved to end of array
            tracks.remove(track);
            tracks.add(trackToMove);
        }

        tracks.set(track, tracks.get(pos));
        tracks.set(pos, trackToMove);
        queue.clear();
        queue.addAll(tracks);
    }

    default void setGuild(Guild guild) {
        guildId.set(guild.getIdLong());
    }

    default void setQueue(Queue<AudioTrack> newQueue) {
        queue.clear();
        queue.addAll(newQueue);
    }

    @Override
    void setVolume(int i);


}
