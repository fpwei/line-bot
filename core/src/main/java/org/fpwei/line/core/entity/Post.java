package org.fpwei.line.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String title;

    @Column
    private String url;

    @Column
    private String board;

    @Column
    private String category;

    @Column
    private String author;

    @Column(name = "post_date")
    private Date date;

    @Transient
    private String nrec;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> images;


    @Override
    public String toString() {
        return String.format("%s %s %s", title, author, date);
    }
}
