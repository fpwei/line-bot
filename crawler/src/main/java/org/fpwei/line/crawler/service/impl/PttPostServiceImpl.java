package org.fpwei.line.crawler.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.fpwei.line.core.dao.PostDao;
import org.fpwei.line.core.entity.Image;
import org.fpwei.line.core.entity.Post;
import org.fpwei.line.crawler.service.PostService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PttPostServiceImpl implements PostService {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d H:m:s yyyy");

    private static final int DEFAULT_NREC = 50;
    private static final int TOLERANCE_TIMES = 5;
    private static final String POST_CLASS = "r-ent";

    @Autowired
    private PostDao postDao;

    @Value("${org.fpwei.crawler.ptt.image.download.path}")
    private String imagesDownloadDirectory;

    @Override
    public List<Post> getPosts(String url, int size, String category) throws IOException {
        List<Post> posts = getPosts0(url, size, category, 0);


        posts.parallelStream()
                .forEach(post -> {
                    Document document;

                    try {
                        document = Jsoup.connect(post.getUrl()).get();
                    } catch (IOException e) {
                        log.error(ExceptionUtils.getStackTrace(e));
                        posts.remove(post);
                        return;
                    }

                    List<String> imageUrls = getImagesUrl(document);
                    if (imageUrls.isEmpty()) {
                        posts.remove(post);
                        return;
                    }

                    List<Image> images = imageUrls.parallelStream()
                            .map(u -> {
                                Image image = new Image();
                                image.setUrl(u);
                                image.setPost(post);
                                image.setPath(downloadImage(u, getDownloadFolder(post)));

                                return image;
                            })
                            .collect(Collectors.toList());
                    post.setImages(images);

                    post.setBoard(getBoard(document));
                    post.setTitle(getTitle(document));
                    post.setAuthor(getAuthor(document));
                    post.setDate(getPostDate(document));

                });

        return posts;
    }


    private List<Post> getPosts0(String url, int size, String category, int currentToleranceCount) throws IOException {
        Document document = Jsoup.connect(url).get();

        Elements elements = document.getElementsByClass(POST_CLASS);
        Collections.reverse(elements);  //the latest post will be the first element

        String formatCategory = "[" + category + "]";
        List<String> tempUrls = elements.stream()
                .filter(e -> compare(e.child(0).text(), DEFAULT_NREC))
                .filter(e -> e.child(2).selectFirst("a") != null)       //the post haven't been removed
                .filter(e -> e.child(2).text().contains(formatCategory))
                .map(e -> e.child(2).selectFirst("a").attr("abs:href"))
                .collect(Collectors.toList());

        AtomicInteger toleranceCount = new AtomicInteger(currentToleranceCount);

        tempUrls = tempUrls.stream()
                .filter(u -> {
                    if (toleranceCount.get() <= TOLERANCE_TIMES) {
                        if (postDao.existsByUrl(u)) {
                            toleranceCount.getAndIncrement();
                            return false;
                        } else {
                            toleranceCount.set(0);
                            return true;
                        }
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        List<Post> posts = tempUrls.stream()
                .map(u -> {
                    Post post = new Post();
                    post.setUrl(u);
                    post.setCategory(category);
                    return post;
                }).collect(Collectors.toList());


        if (toleranceCount.get() > TOLERANCE_TIMES) {
            return posts;
        }

        if (posts.size() < size) {
            posts.addAll(getPosts0(getPreviousPageUrl(document), size - posts.size(), category, toleranceCount.get()));
            return posts;
        } else if (posts.size() == size) {
            return posts;
        } else {
            return posts.subList(0, size);
        }
    }

    private File getDownloadFolder(Post post) {
        String url = post.getUrl();
        String folderName = url.substring(url.lastIndexOf("/"), url.indexOf(".html"));

        return FileUtils.getFile(imagesDownloadDirectory, folderName);
    }

    private String downloadImage(String url, final File downloadFolder) {
        File file = FileUtils.getFile(downloadFolder, url.substring(url.lastIndexOf("/")));

        try {
            FileUtils.copyURLToFile(new URL(url), file);
        } catch (IOException e) {
            log.warn("Download image from {} failed.\n{}", url, ExceptionUtils.getStackTrace(e));
            return null;
        }

        return file.getAbsolutePath();
    }

    private String getBoard(Document document) {
        Element element = document.selectFirst(".board");
        return element == null ? null : element.ownText();
    }

    private String getTitle(Document document) {
        Iterator<Element> iterator = document.getElementsMatchingOwnText("標題").iterator();

        if (iterator.hasNext()) {
            Element element = iterator.next().nextElementSibling();
            return element == null ? null : element.text();
        } else {
            return null;
        }
    }

    private String getAuthor(Document document) {
        Iterator<Element> iterator = document.getElementsMatchingOwnText("作者").iterator();

        if (iterator.hasNext()) {
            Element element = iterator.next().nextElementSibling();
            return element == null ? null : element.text();
        } else {
            return null;
        }
    }

    private Date getPostDate(Document document) {
        Iterator<Element> iterator = document.getElementsMatchingOwnText("時間").iterator();

        String date = null;

        if (iterator.hasNext()) {
            Element element = iterator.next().nextElementSibling();
            date = element.text().replaceAll("(\\s){2,}", " ").trim();
        }

        return StringUtils.isBlank(date) ? null : Date.from(LocalDateTime.parse(date, formatter).atZone(ZoneId.of("Asia/Taipei")).toInstant());
    }

    private boolean compare(String s, Integer cNrec) {
        int nrec;

        if (s.equals("爆")) {
            nrec = 100;
        } else if (s.equals("XX")) {
            nrec = -100;
        } else if (s.contains("X")) {
            nrec = Integer.valueOf(s.replace("X", "-"));
        } else if (s.equals("")) {
            nrec = 0;
        } else {
            nrec = Integer.valueOf(s);
        }

        return nrec >= cNrec;
    }

    private List<String> getImagesUrl(Document document) {
        List<String> urls = document.getElementById("main-content").select("> a").eachAttr("abs:href");

        return urls.parallelStream()
                .filter(u -> StringUtils.endsWithIgnoreCase(u, ".jpg"))
                .collect(Collectors.toList());
    }

    private String getPreviousPageUrl(Document document) {
        Elements elements = document.select(".btn-group-paging > a:nth-child(2)");

        if (elements.first() != null) {
            return elements.first().attr("abs:href");
        } else {
            return null;
        }
    }

}
