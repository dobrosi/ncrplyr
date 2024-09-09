package com.github.dobrosi.ncrplyr.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chameleon.playlist.xspf.Link;
import chameleon.playlist.xspf.Meta;
import chameleon.playlist.xspf.Playlist;
import com.github.dobrosi.api.ExtendedDelugeApi;
import com.github.dobrosi.api.WebApi;
import com.github.dobrosi.imdbclient.ImdbClient;
import com.github.dobrosi.ncrplyr.NcoreClient;
import com.github.dobrosi.ncrplyr.NcoreClientNotLoggedInException;
import com.github.dobrosi.ncrplyr.SearchParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.util.CollectionUtils.toMultiValueMap;

@Service
public class VideoStreamService {
    public static final double FILE_PROGRESS_LIMIT = 0.01;
    private static Map<SearchParameters, WebApi.GetTorrentFilesResponse.Result> CACHE_FILE = new HashMap<>();
    private static Map<SearchParameters, Map<Object, WebApi.GetTorrentFilesResponse.Result>> CACHE_FILE_MAP = new HashMap<>();
    private static Map<SearchParameters, Object> CACHE_TORRENT = new HashMap<>();
    private static Map<SearchParameters, List<Object>> CACHE_TORRENT_LIST = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(VideoStreamService.class);

    @Value("${downloadsPath}")
    private String downloadsPath;

    @Value("${waitingTime:15s}")
    private Duration waitingTime;

    @Value("${host}")
    private String host;

    private ExtendedDelugeApi extendedDelugeApi;

    private NcoreClient ncoreClient;

    private ImdbClient imdbClient;

    private StreamService streamService;

    public VideoStreamService(NcoreClient ncoreClient, ImdbClient imdbClient, ExtendedDelugeApi extendedDelugeApi, StreamService streamService) {
        this.ncoreClient = ncoreClient;
        this.imdbClient = imdbClient;
        this.extendedDelugeApi = extendedDelugeApi;
        this.streamService = streamService;
    }

    public List<String> getTorrentUrls(SearchParameters id) {
        try {
            return ncoreClient.getTorrentUrls(id, imdbClient.getMovie(id.imdbid).title);
        } catch (NcoreClientNotLoggedInException e) {
            return getTorrentUrls(id);
        }
    }

    public ResponseEntity<StreamingResponseBody> download(
        SearchParameters id,
        String rangeHeader,
        boolean inline) {
        return download(
            id,
            CACHE_TORRENT.get(id),
            extendedDelugeApi.getLargestFile(getFirstFile(id).contents.values()),
            rangeHeader,
            inline);
    }

    public ResponseEntity<StreamingResponseBody> download(
        SearchParameters id,
        String torrentId,
        int fileIndex,
        String rangeHeader,
        boolean inline) {

        return download(
            id,
            torrentId,
            extendedDelugeApi.getFileByIndex(getFiles(id).get(torrentId), fileIndex),
            rangeHeader,
            inline);
    }

    public ResponseEntity<StreamingResponseBody> playlist(SearchParameters id) {
        ImdbClient.Movie movie = getMovie(id.imdbid);
        Playlist playlist = extendedDelugeApi.createXspfPlaylist(format("%s/play/%s", host, id), getFiles(id));
        playlist.setTitle(movie.title);
        playlist.setImage(movie.image);
        if (movie.year != null) {
            playlist.setDate(new Date(Integer.parseInt(movie.year), 0, 1));
        }
        playlist.setCreator(movie.director);
        addMeta(playlist, "IMDB.id", id.imdbid);
        addMeta(playlist, "IMDB.rating", movie.rating);
        Link link = new Link();
        link.setRel("IMDB");
        link.setContent(movie.link);
        playlist.addLink(link);
        return new ResponseEntity<>(
            outputStream -> {
                try {
                    playlist.writeTo(outputStream, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            toMultiValueMap((Map.of(
                CONTENT_TYPE,
                singletonList("application/xspf+xml"),
                CONTENT_DISPOSITION,
                singletonList(streamService.getContentDisposition(format("%s.xspf", movie.title), false))))),
                                            HttpStatus.OK
        );
    }

    private static void addMeta(Playlist playlist, String rel, String content) {
        Meta m = new Meta();
        m.setRel(rel);
        m.setContent(content);
        playlist.addMeta(m);
    }

    private ResponseEntity<StreamingResponseBody> download(
        SearchParameters id,
        Object torrentId,
        WebApi.GetTorrentFilesResponse.Result.File file,
        String rangeHeader,
        boolean inline) {

        extendedDelugeApi.coreApi.resumeTorrent(torrentId);
        int counter = 0;
        while (counter++ < 5 && needSleep(id, rangeHeader, file)) {
            sleep();
        }
        return streamService.prepareDownloadContent(getFullFilePath(file.path).toFile(), file.size, rangeHeader, inline);
    }

    private boolean needSleep(
        SearchParameters id,
        String rangeHeader,
        WebApi.GetTorrentFilesResponse.Result.File file) {

        return rangeHeader != null &&
            rangeHeader.toLowerCase().startsWith("bytes=0-") &&
            file.progress < FILE_PROGRESS_LIMIT &&
            reloadFile(id, CACHE_TORRENT.get(id), file).progress <= FILE_PROGRESS_LIMIT;
    }

    private WebApi.GetTorrentFilesResponse.Result.File reloadFile(
        SearchParameters id,
        Object torrentId,
        WebApi.GetTorrentFilesResponse.Result.File file) {

        return extendedDelugeApi.getFileByIndex(getFiles(id, false).get(torrentId), file.index);
    }

    private Path getFullFilePath(String filename) {
        return Paths.get(downloadsPath, filename);
    }

    private WebApi.GetTorrentFilesResponse.Result getFirstFile(SearchParameters id) {
        if (CACHE_FILE.containsKey(id)) {
            return CACHE_FILE.get(id);
        }
        WebApi.GetTorrentFilesResponse.Result file = extendedDelugeApi.webApi.getTorrentFiles(addFirstTorrent(id)).result;
        CACHE_FILE.put(id, file);
        return file;
    }

    private Map<Object, WebApi.GetTorrentFilesResponse.Result> getFiles(SearchParameters id) {
        return getFiles(id, true);
    }

    private Map<Object, WebApi.GetTorrentFilesResponse.Result> getFiles(SearchParameters id, boolean useCache) {
        if (useCache && CACHE_FILE_MAP.containsKey(id)) {
            return CACHE_FILE_MAP.get(id);
        }
        Map<Object, WebApi.GetTorrentFilesResponse.Result> files =
            addTorrents(id).stream()
                .collect(toMap(
                    tid -> tid,
                    tid -> extendedDelugeApi.webApi.getTorrentFiles(tid).result));
        CACHE_FILE_MAP.put(id, files);
        CACHE_FILE.put(
            id,
            files.values()
                .iterator()
                .next());
        return files;
    }

    private Object addFirstTorrent(SearchParameters id) {
        if (CACHE_TORRENT.containsKey(id)) {
            return CACHE_TORRENT.get(id);
        }
        if (CACHE_TORRENT_LIST.containsKey(id)) {
            return CACHE_TORRENT_LIST.get(id).get(0);
        }
        WebApi webApi = extendedDelugeApi.webApi;
        Object result = webApi
            .addTorrents(webApi
                             .downloadTorrentFromUrl(getTorrentUrls(id).get(0)))
            .get(0);
        CACHE_TORRENT.put(id, result);
        return result;
    }

    private List<Object> addTorrents(SearchParameters id) {
        if (CACHE_TORRENT_LIST.containsKey(id)) {
            return CACHE_TORRENT_LIST.get(id);
        }
        WebApi webApi = extendedDelugeApi.webApi;
        List<Object> result = webApi
            .addTorrents(getTorrentUrls(id)
                    .stream()
                    .map(webApi::downloadTorrentFromUrl)
                    .toArray(String[]::new));
        if (result.isEmpty()) {
            throw new TorrentNotFound(id);
        }
        CACHE_TORRENT_LIST.put(id, result);
        CACHE_TORRENT.put(id, result.get(0));
        return result;
    }

    private void sleep() {
        try {
            Thread.sleep(waitingTime.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Object> addTorrents(String[] torrentUrls, String[] imdbids) {
        final List<Object> result = new ArrayList<>();
        result.addAll(extendedDelugeApi.webApi.addTorrents(torrentUrls));
        result.addAll(extendedDelugeApi.downloadAndAddTorrentFromUrls(imdbids));
        return result;
    }

    public void removeTorrent(final Object... torrentIds) {
        extendedDelugeApi.coreApi.removeTorrent(torrentIds);
    }

    public void pauseTorrent(final Object... torrentIds) {
        extendedDelugeApi.coreApi.pauseTorrent(torrentIds);
    }

    public void resumeTorrent(final Object... torrentIds) {
        extendedDelugeApi.coreApi.resumeTorrent(torrentIds);
    }

    public ImdbClient.Movie getMovie(final String imdbid) {
        return imdbClient.getMovie(imdbid);
    }
}
