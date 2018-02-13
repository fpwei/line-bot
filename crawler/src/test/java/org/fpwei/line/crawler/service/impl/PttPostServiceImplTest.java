package org.fpwei.line.crawler.service.impl;

import org.fpwei.line.core.dao.PostDao;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Calendar;
import java.util.Date;


@RunWith(PowerMockRunner.class)
@PrepareForTest(PttPostServiceImpl.class)
public class PttPostServiceImplTest {
    @InjectMocks
    private PttPostServiceImpl postService;

    @Mock
    private PostDao postDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testGetPostDate() throws Exception {
        String html = "<div id=\"main-content\" class=\"bbs-screen bbs-content\">" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">作者</span>" +
                "<span class=\"article-meta-value\">malaimomo (malaimomo)</span>" +
                "</div>" +
                "<div class=\"article-metaline-right\">" +
                "<span class=\"article-meta-tag\">看板</span>" +
                "<span class=\"article-meta-value\">Beauty</span>" +
                "</div>" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">標題</span>" +
                "<span class=\"article-meta-value\">[正妹] 冰上女王-金妍兒</span><" +
                "/div>" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">時間</span>" +
                "<span class=\"article-meta-value\">Fri Feb 9 22:28:28 2018</span>" +
                "</div>\n" +
                "這次平昌冬奧開幕式的火炬手" +
                "</div>";
        Document doc = Jsoup.parse(html);

        Date date = Whitebox.invokeMethod(postService, "getPostDate", doc);

        Assert.assertNotNull(date);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Assert.assertEquals(2018, c.get(Calendar.YEAR));
        Assert.assertEquals(2, c.get(Calendar.MONTH) + 1);
        Assert.assertEquals(9, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(6, c.get(Calendar.DAY_OF_WEEK));
        Assert.assertEquals(22, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(28, c.get(Calendar.MINUTE));
        Assert.assertEquals(28, c.get(Calendar.SECOND));

    }

    @Test
    public void testGetPostDate2() throws Exception {
        String html = "<div id=\"main-content\" class=\"bbs-screen bbs-content\">" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">作者</span>" +
                "<span class=\"article-meta-value\">malaimomo (malaimomo)</span>" +
                "</div>" +
                "<div class=\"article-metaline-right\">" +
                "<span class=\"article-meta-tag\">看板</span>" +
                "<span class=\"article-meta-value\">Beauty</span>" +
                "</div>" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">標題</span>" +
                "<span class=\"article-meta-value\">[正妹] 冰上女王-金妍兒</span><" +
                "/div>" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">時間</span>" +
                "<span class=\"article-meta-value\">Mon Feb   12  1:3:5 2018</span>" +
                "</div>\n" +
                "這次平昌冬奧開幕式的火炬手" +
                "</div>";
        Document doc = Jsoup.parse(html);

        Date date = Whitebox.invokeMethod(postService, "getPostDate", doc);

        Assert.assertNotNull(date);

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        Assert.assertEquals(2018, c.get(Calendar.YEAR));
        Assert.assertEquals(2, c.get(Calendar.MONTH) + 1);
        Assert.assertEquals(12, c.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(2, c.get(Calendar.DAY_OF_WEEK));
        Assert.assertEquals(1, c.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(3, c.get(Calendar.MINUTE));
        Assert.assertEquals(5, c.get(Calendar.SECOND));

    }


    @Test
    public void testGetTitle() throws Exception {
        String html = "<div id=\"main-content\" class=\"bbs-screen bbs-content\">" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">作者</span>" +
                "<span class=\"article-meta-value\">malaimomo (malaimomo)</span>" +
                "</div>" +
                "<div class=\"article-metaline-right\">" +
                "<span class=\"article-meta-tag\">看板</span>" +
                "<span class=\"article-meta-value\">Beauty</span>" +
                "</div>" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">標題</span>" +
                "<span class=\"article-meta-value\">[正妹] 冰上女王-金妍兒</span><" +
                "/div>" +
                "<div class=\"article-metaline\">" +
                "<span class=\"article-meta-tag\">時間</span>" +
                "<span class=\"article-meta-value\">Mon Feb 12 1:3:5 2018</span>" +
                "</div>\n" +
                "這次平昌冬奧開幕式的火炬手" +
                "</div>";
        Document doc = Jsoup.parse(html);

        String title = Whitebox.invokeMethod(postService, "getTitle", doc);

        Assert.assertNotNull(title);
        Assert.assertEquals("[正妹] 冰上女王-金妍兒", title);
    }

    @Test
    public void testGetBoard() throws Exception {
        String html = "<div id=\"topbar\" class=\"bbs-content\">\n" +
                "\t\t<a id=\"logo\" href=\"/\">批踢踢實業坊</a>\n" +
                "\t\t<span>›</span>\n" +
                "\t\t<a class=\"board\" href=\"/bbs/Beauty/index.html\"><span class=\"board-label\">看板 </span>Beauty</a>\n" +
                "\t\t<a class=\"right small\" href=\"/about.html\">關於我們</a>\n" +
                "\t\t<a class=\"right small\" href=\"/contact.html\">聯絡資訊</a>\n" +
                "\t</div>";

        Document doc = Jsoup.parse(html);

        String board = Whitebox.invokeMethod(postService, "getBoard", doc);

        Assert.assertNotNull(board);
        Assert.assertEquals("Beauty", board);
    }
}
