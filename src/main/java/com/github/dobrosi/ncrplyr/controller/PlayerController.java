package com.github.dobrosi.ncrplyr.controller;

import java.util.Map;

import com.github.dobrosi.ncrplyr.SearchParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import static com.github.dobrosi.ncrplyr.controller.VideoController.IMDB;
import static com.github.dobrosi.ncrplyr.controller.VideoController.IMDB_TYPE;
import static com.github.dobrosi.ncrplyr.controller.VideoController.IMDB_TYPE_TITLE;
import static java.lang.String.format;

@RestController
public class PlayerController {

    @Value("${host}")
    private String host;

    @GetMapping({
        "/player" + IMDB,
        "/player" + IMDB_TYPE,
        "/player" + IMDB_TYPE_TITLE})
    public ModelAndView player(SearchParameters searchParameters) {
        return new ModelAndView("player", Map.of(
            "url",
            format("%s/play/%s", host, searchParameters)));
    }
}
