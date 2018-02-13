package org.fpwei.line.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String path;

    @Column
    private String url;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
