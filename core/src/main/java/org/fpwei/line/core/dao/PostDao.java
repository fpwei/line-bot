package org.fpwei.line.core.dao;

import org.fpwei.line.core.entity.Post;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostDao extends PagingAndSortingRepository<Post, Integer> {

    boolean existsByUrl(String url);

    @Query(value = "SELECT * FROM post ORDER BY RAND() LIMIT :rows", nativeQuery = true)
    List<Post> findPostsByRandom(@Param("rows") int rows);

}
