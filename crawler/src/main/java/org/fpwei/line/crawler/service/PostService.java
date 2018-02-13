package org.fpwei.line.crawler.service;


import org.fpwei.line.core.entity.Post;

import java.io.IOException;
import java.util.List;

public interface PostService {
    List<Post> getPosts(String url, int size, String category) throws IOException;
}
