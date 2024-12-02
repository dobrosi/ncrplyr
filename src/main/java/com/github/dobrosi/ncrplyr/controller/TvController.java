package com.github.dobrosi.ncrplyr.controller;

import java.io.IOException;
import java.util.Map;

import com.github.dobrosi.imdbclient.Channel;
import com.github.dobrosi.imdbclient.DigiOnlinePlaywrightClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/tv")
public class TvController {

    private final DigiOnlinePlaywrightClient digiOnlinePlaywrightClient;

    @Autowired
    public TvController(final DigiOnlinePlaywrightClient digiOnlinePlaywrightClient) {
        this.digiOnlinePlaywrightClient = digiOnlinePlaywrightClient;
    }

    @GetMapping
    public ModelAndView index() {
        return new ModelAndView("tv/player");
    }

    @GetMapping("/indexPage")
    public void channels(HttpServletResponse response) throws IOException {
        response.getOutputStream().write(digiOnlinePlaywrightClient.getIndexPage().getBytes());
    }

    @GetMapping("/channels")
    public Map<String, Channel> channels() {
        return digiOnlinePlaywrightClient.getChannels();
    }

    @GetMapping("/link/id/{channelId}")
    public String getLinkById(@PathVariable String channelId) {
        return digiOnlinePlaywrightClient.getLinkById(channelId);
    }

    @GetMapping("/link/name/{name}")
    public String getLinkByName(@PathVariable String name) {
        return digiOnlinePlaywrightClient.getLinkByName(name);
    }

    @GetMapping("/redirect/id/{channelId}")
    public RedirectView redirectById(@PathVariable String channelId) {
        return new RedirectView(getLinkById(channelId));
    }

    @GetMapping("/redirect/name/{name}")
    public RedirectView redirectByName(@PathVariable String name) {
        return new RedirectView(getLinkByName(name));
    }

    @GetMapping("/sessionId")
    public Object getSessionId() {
        return digiOnlinePlaywrightClient.getSessionId();
    }

}
