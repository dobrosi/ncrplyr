import java.io.IOException;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImdbTest {
    @Test
    void testTitle() throws IOException {
        assertEquals(
            "Párizs, Texas (1984)",
            new String(Jsoup.connect("https://www.imdb.com/title/tt0087884/?ref_=nv_sr_srsg_1_tt_7_nm_1_in_0_q_texas").get().selectXpath("//meta[@property=\"og:title\"]").attr("content").getBytes(), "UTF8").split(" ⭐")[0]
        );
    }
}
