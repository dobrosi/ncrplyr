package com.github.dobrosi.ncrplyr.service;

import java.io.File;
import java.io.RandomAccessFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.ACCEPT_RANGES;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_RANGE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@Service
public class StreamService {
    private final Logger logger = LoggerFactory.getLogger(StreamService.class);

    public static final String BYTES = "bytes";

    public static final int CHUNK_SIZE = 2 * 1024 * 1024; //2Mb

    public ResponseEntity<StreamingResponseBody> prepareDownloadContent(
        File file,
        Long fileSize,
        String range,
        boolean inline) {

        logger.info(format("prepareContent,%s,%d,%s", file.getName(), fileSize, range));

        long rangeStart;
        long rangeEnd;

        HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;
        if (range == null) {
            rangeStart = 0;
            rangeEnd = fileSize;
            httpStatus = HttpStatus.OK;
        } else {
            String[] ranges = range.split("-");
            rangeStart = Long.parseLong(ranges[0].split("=")[1]);
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
                if (rangeEnd >= fileSize) {
                    httpStatus = HttpStatus.OK;
                    rangeEnd = fileSize;
                }
            } else {
                rangeEnd = fileSize;
            }
        }
        long rangeSize = rangeEnd - rangeStart;
        return ResponseEntity
            .status(httpStatus)
            .contentLength(rangeSize)
            //.contentType(APPLICATION_OCTET_STREAM)
            .header(CONTENT_TYPE, "video/x-matroska")
            .header(CONTENT_DISPOSITION, getContentDisposition(file.getName(), inline))
            .header(ACCEPT_RANGES, BYTES)
            .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
            .body(out -> {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                    randomAccessFile.seek(rangeStart);
                    byte[] buffer = new byte[(int) Math.min(CHUNK_SIZE, rangeSize)];
                    while (randomAccessFile.read(buffer) != -1) {
                        StreamUtils.copy(buffer, out);
                    }
                }
            });
    }

    public String getContentDisposition(
        String filename,
        boolean inline
    ) {
        return format("%s; filename=\"%s\"", inline ? "inline" : "attachment", filename);
    }

}
