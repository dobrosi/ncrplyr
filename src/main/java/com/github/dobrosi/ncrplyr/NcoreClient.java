package com.github.dobrosi.ncrplyr;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Connection.Method.POST;

@Service
public class NcoreClient {
    private static final Map<SearchParameters, List<String>> CACHE = new HashMap<>();

    @Value("${ncore.url:https://ncore.pro}")
    private String urlPrefix;

    @Value("${ncore.user}")
    private String user;

    @Value("${ncore.password}")
    private String password;

    @Value("${torrentLimit:10}")
    private int torrentLimit;

    private Map<String, String> cookies;

    private String clientKey;

    Pattern keyPattern = Pattern.compile("\\?key=(\\w*)\"");

    Pattern torrentIdPattern = Pattern.compile("torrent\\((\\d*)\\)");

    public List<String> getTorrentUrls(SearchParameters id, String title) {
        try {
            if (CACHE.containsKey(id)) {
                return CACHE.get(id);
            }
            Document doc = call(GET, format("/torrents.php?miszerint=seeders&hogyan=DESC&tipus=%s&mire=%s&miben=name",
                                 id.type,
                                 URLEncoder.encode(title, "UTF-8")));
            List<String> result = getTorrentIds(id.imdbid, doc).stream()
                .map(
                    tid -> format("%s/torrents.php?action=download&key=%s&id=%s", urlPrefix, clientKey, tid))
                .toList();
            CACHE.put(id, result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void login() {
        try {
            Connection.Response r1 = Jsoup.connect(urlPrefix)
                .method(GET)
                .execute();
            cookies = r1.cookies();
            Connection.Response r2 = Jsoup.connect(format("%s/login.php", urlPrefix))
                .method(POST)
                .cookies(cookies)
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                .header("accept-language", "hu-HU,hu;q=0.9")
                .header("cache-control", "max-age=0")
                .header("content-type", "application/x-www-form-urlencoded")
                .header("origin", "https://ncore.pro")
                .header("priority", "u=0, i")
                .header("referer", "https://ncore.pro/login.php")
                .header("sec-ch-ua", "\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Google Chrome\";v=\"126\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Linux\"")
                .header("sec-fetch-dest", "document")
                .header("sec-fetch-mode", "navigate")
                .header("sec-fetch-site", "same-origin")
                .header("sec-fetch-user", "?1")
                .header("upgrade-insecure-requests", "1")
                .header("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .requestBody(format("set_lang=hu&submitted=1&nev=%s&pass=%s&ne_leptessen_ki=1", user, password))
                .execute();
            cookies.putAll(r2.cookies());
            clientKey = getKey(r2.parse());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getKey(final Document doc) {
        Matcher m = keyPattern.matcher(doc.html());
        if(!m.find()) {
            logout();
            throw new NcoreClientNotLoggedInException("NCoreClient not logged in.");
        }
        return m.group(1);
    }

    private void logout() {
        clientKey = null;
        cookies.clear();
    }

    private List<String> getTorrentIds(String imdbid, Document doc) {
        List<String> result = new ArrayList<>();
        doc
            .select("div.torrent_txt")
            .stream()
            .filter(row -> isContains(imdbid, row))
            .limit(torrentLimit)
            .forEach(torrentRow -> {
                Matcher m = torrentIdPattern.matcher(torrentRow.html());
                while (m.find()) {
                    result.add(m.group(1));
                }
            });
        return result;
    }

    private static boolean isContains(final String imdbid, final Element row) {
        return row.html().contains(imdbid);
    }

    private Document call(Connection.Method method, String url) {
        try {
            if (clientKey == null) {
                login();
            }
            return Jsoup.connect(format("%s%s", urlPrefix, url))
                .cookies(cookies) // Use cookies for authentication
                .method(method)
                .execute()
                .parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
