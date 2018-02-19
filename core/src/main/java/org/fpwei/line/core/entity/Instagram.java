package org.fpwei.line.core.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "instagram")
public class Instagram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String account;

    private int priority;

    private String category;

    @Column(columnDefinition = "CHAR(1)")
    private int status;
}
