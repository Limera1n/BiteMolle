package dev.cosgy.jmusicbot.playlist;

import com.jagrosh.jmusicbot.BotConfig;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author kosugi_kun
 */
public class PubliclistLoader {
    private final BotConfig config;

    public PubliclistLoader(BotConfig config) {
        this.config = config;
    }

    private static <T> void shuffle(List<T> list) {
        IntStream.range(0, list.size()).forEach(first -> {
            int second = (int) (Math.random() * list.size());
            T tmp = list.get(first);
            list.set(first, list.get(second));
            list.set(second, tmp);
        });
    }

    public List<String> getPlaylistNames() {
        if (folderExists()) {
            File folder = new File(config.getPublistFolder());
            return Arrays.stream(Objects.requireNonNull(folder.listFiles((pathname) -> pathname.getName().endsWith(".txt"))))
                    .map(f -> f.getName().substring(0, f.getName().length() - 4))
                    .collect(Collectors.toList());
        } else {
            createFolder();
            return Collections.emptyList();
        }
    }

    public void createFolder() {
        try {
            Files.createDirectory(OtherUtil.getPath(config.getPublistFolder()));
        } catch (IOException ignore) {
        }
    }

    public boolean folderExists() {
        return Files.exists(OtherUtil.getPath(config.getPublistFolder()));
    }

    public void createPlaylist(String name) throws IOException {
        Files.createFile(OtherUtil.getPath(config.getPublistFolder() + File.separator + name + ".txt"));
    }

    public void deletePlaylist(String name) throws IOException {
        Files.delete(OtherUtil.getPath(config.getPublistFolder() + File.separator + name + ".txt"));
    }

    public void writePlaylist(String name, String text) throws IOException {
        Files.write(OtherUtil.getPath(config.getPublistFolder() + File.separator + name + ".txt"), text.trim().getBytes(StandardCharsets.UTF_8));
    }

    public Playlist getPlaylist(String name) {
        if (!getPlaylistNames().contains(name))
            return null;
        if (!folderExists()) {
            createFolder();
            return null;
        }

        return loadPlaylistFromPath(name, OtherUtil.getPath(config.getPublistFolder() + File.separator + name + ".txt"));
    }

    private Playlist loadPlaylistFromPath(String name, java.nio.file.Path playlistPath) {
        try {
            PlaylistSourceReader.Result source = PlaylistSourceReader.read(playlistPath);
            List<String> list = source.getItems();
            if (source.isShuffle())
                shuffle(list);
            return new Playlist(name, list, source.isShuffle());
        } catch (IOException e) {
            return null;
        }
    }

    public static class PlaylistLoadError {
        private final int number;
        private final String item;
        private final String reason;

        private PlaylistLoadError(int number, String item, String reason) {
            this.number = number;
            this.item = item;
            this.reason = reason;
        }

        public int getIndex() {
            return number;
        }

        public String getItem() {
            return item;
        }

        public String getReason() {
            return reason;
        }
    }

    public class Playlist {
        private final String name;
        private final List<String> items;
        private final boolean shuffle;
        private final List<AudioTrack> tracks = new LinkedList<>();
        private final List<PlaylistLoadError> errors = new LinkedList<>();
        private boolean loaded = false;

        private Playlist(String name, List<String> items, boolean shuffle) {
            this.name = name;
            this.items = items;
            this.shuffle = shuffle;
        }

        public void loadTracks(AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
            if (loaded)
                return;
            loaded = true;
            PlaylistAsyncLoader.loadTracks(manager, name, items, shuffle, config, tracks, errors, consumer, callback,
                    this::shuffleTracks, createErrorFactory());
        }

        private PlaylistAsyncLoader.ErrorFactory<PlaylistLoadError> createErrorFactory() {
            return new PlaylistAsyncLoader.ErrorFactory<>() {
                @Override
                public PlaylistLoadError tooLong(int index, String item) {
                    return new PlaylistLoadError(index, item, "This track exceeds the allowed maximum length");
                }

                @Override
                public PlaylistLoadError noMatches(int index, String item) {
                    return new PlaylistLoadError(index, item, "No matching item was found.");
                }

                @Override
                public PlaylistLoadError loadFailed(int index, String item, com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
                    return new PlaylistLoadError(index, item, "Failed to load the track: " + exception.getLocalizedMessage());
                }
            };
        }

        public void shuffleTracks() {
            shuffle(tracks);
        }

        public String getName() {
            return name;
        }

        public List<String> getItems() {
            return items;
        }

        public List<AudioTrack> getTracks() {
            return tracks;
        }

        public List<PlaylistLoadError> getErrors() {
            return errors;
        }
    }
}