package com.github.dobrosi.ncrplyr;

import java.util.List;

import com.github.dobrosi.ncrplyr.controller.VideoType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NcoreClientTest {

    @Autowired
    NcoreClient ncoreClient;

    @Test
    void getTorrentUrls() {
        assertEquals(
            List.of("https://ncore.pro/torrents.php?action=download&key=53af43571fb5a886b9e019eb4714c18b&id=1860125"),
            ncoreClient
                .getTorrentUrls(
                    new SearchParameters(
                        "Terminator",
                        "tt0088247",
                        VideoType.xvid),
                    "Terminator"));
    }
}