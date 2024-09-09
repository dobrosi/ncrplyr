package com.github.dobrosi.ncrplyr.controller;

import java.util.List;

import com.github.dobrosi.imdbclient.ImdbClient;
import com.github.dobrosi.ncrplyr.SearchParameters;
import com.github.dobrosi.ncrplyr.service.VideoStreamService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class VideoController {
    public static final String IMDB = "/{imdbid}";

    public static final String IMDB_TYPE = IMDB + "/{type}";

    public static final String IMDB_TYPE_TITLE = IMDB_TYPE + "/{title}";

    private final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Value("${ncore.type:all_own}")
    private VideoType type;

    private VideoStreamService videoStreamService;

    @Autowired
    public VideoController(VideoStreamService videoStreamService) {
        this.videoStreamService = videoStreamService;
    }

    @GetMapping({
        "/torrent/getUrl" + IMDB,
        "/torrent/getUrl" + IMDB_TYPE,
        "/torrent/getUrl" + IMDB_TYPE_TITLE})
    public List<String> torrent(SearchParameters searchParameters) {
        return videoStreamService.getTorrentUrls(setDefaults(searchParameters));
    }

    @GetMapping({
        "/download" + IMDB,
        "/download" + IMDB_TYPE,
        "/download" + IMDB_TYPE_TITLE})
    public ResponseEntity<StreamingResponseBody> download(
        SearchParameters searchParameters,
        @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return videoStreamService.download(setDefaults(searchParameters), rangeHeader, false);
    }

    @GetMapping({
        "/play" + IMDB,
        "/play" + IMDB_TYPE,
        "/play" + IMDB_TYPE_TITLE})
    public ResponseEntity<StreamingResponseBody> play(
        SearchParameters searchParameters,
        @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return videoStreamService.download(setDefaults(searchParameters), rangeHeader, true);
    }

    @GetMapping("/play" + IMDB_TYPE + "/{torrentId}/{fileIndex}")
    public ResponseEntity<StreamingResponseBody> playFile(
        SearchParameters searchParameters,
        @PathVariable String torrentId,
        @PathVariable int fileIndex,
        @RequestHeader(value = "Range", required = false) String rangeHeader) {
        return videoStreamService.download(setDefaults(searchParameters), torrentId, fileIndex, rangeHeader, true);
    }

    @GetMapping({
        "/playlist" + IMDB,
        "/playlist" + IMDB_TYPE,
        "/playlist" + IMDB_TYPE_TITLE})
    public ResponseEntity<StreamingResponseBody> playlist(SearchParameters searchParameters) {
        return videoStreamService.playlist(setDefaults(searchParameters));
    }

    @GetMapping("/torrent/add")
    public List<Object> addTorrent(
        @RequestParam(required = false) String[] torrentUrls,
        @RequestParam(required = false) String[] imdbids) {
        return videoStreamService.addTorrents(torrentUrls, imdbids);
    }

    @GetMapping("/torrent/remove")
    public void removeTorrent(
        @RequestParam(required = false) Object... torrentIds) {
        videoStreamService.removeTorrent(torrentIds);
    }

    @GetMapping("/torrent/pause")
    public void pauseTorrent(
        @RequestParam(required = false) Object... torrentIds) {
        videoStreamService.pauseTorrent(torrentIds);
    }

    @GetMapping("/torrent/resume")
    public void resumeTorrent(
        @RequestParam(required = false) Object... torrentIds) {
        videoStreamService.resumeTorrent(torrentIds);
    }

    @GetMapping("/title/{imdbid}")
    public String title(@PathVariable String imdbid) {
        return videoStreamService.getMovie(imdbid).title;
    }

    @GetMapping("/movie/{imdbid}")
    public ImdbClient.Movie movie(@PathVariable String imdbid) {
        return videoStreamService.getMovie(imdbid);
    }

    @ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public String runtimeException(RuntimeException e) {
        logger.error(e.getMessage(), e);
        return e.getMessage();
    }

    private SearchParameters setDefaults(SearchParameters searchParameters) {
        if (searchParameters.type == null) {
            searchParameters.type = type;
        }
        return searchParameters;
    }
}
