package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "t_error_item_option_snapshot")
public class ErrorItemOptionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "error_item_id", nullable = false)
    private ErrorItem errorItem;

    private Integer sortOrder;

    @Column(length = 10)
    private String optionKey;

    @Column(length = 1000)
    private String optionContent;

    private Boolean isCorrect;

    private Boolean isSelected;
}
