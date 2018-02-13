package org.fpwei.line.crawler.task;

import lombok.extern.slf4j.Slf4j;
import org.fpwei.line.core.dao.PostDao;
import org.fpwei.line.core.entity.Post;
import org.fpwei.line.crawler.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class PttBeautyTask {

    private static final String BEAUTY_URL = "https://www.ptt.cc/bbs/Beauty/index.html";

    @Autowired
    private PostService postService;

    @Autowired
    private PostDao postDao;

    @Scheduled(cron = "${org.fpwei.crawler.task.beauty.cron}")
    public void scanBeauty() throws IOException {
        List<Post> posts = postService.getPosts(BEAUTY_URL, 50, "正妹");

        posts.forEach(p -> log.debug(p.toString()));

        postDao.save(posts);
    }


}
