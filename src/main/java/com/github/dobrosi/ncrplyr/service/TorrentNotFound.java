package com.github.dobrosi.ncrplyr.service;

public class TorrentNotFound extends RuntimeException {
    public TorrentNotFound(Object id) {
        super("Torrent not found by " + id);
    }
}
