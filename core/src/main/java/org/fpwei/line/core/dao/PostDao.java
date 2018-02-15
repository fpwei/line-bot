package org.fpwei.line.core.dao;

import org.fpwei.line.core.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostDao extends PagingAndSortingRepository<Post, Integer> {

    boolean existsByUrl(String url);

    @Query(value = "SELECT id FROM post ORDER BY RAND() LIMIT :rows", nativeQuery = true)
    List<Integer> queryPostIdByRandom(@Param("rows") int rows);

}
