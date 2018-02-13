package org.fpwei.line.core.dao;

import org.fpwei.line.core.entity.Post;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PostDao extends PagingAndSortingRepository<Post, Integer> {

    boolean existsByUrl(String url);

}
