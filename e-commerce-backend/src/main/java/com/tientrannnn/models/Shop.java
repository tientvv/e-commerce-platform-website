package com.tientrannnn.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "shops")
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Nationalized
    @Column(name = "name_shop", nullable = false)
    private String nameShop;

    @Column(name = "avatar_shop", nullable = false, length = 500)
    private String avatarShop;

    @Column(name = "cccd", nullable = false, length = 50)
    private String cccd;

    @Column(name = "cccd_front_url", nullable = false, length = 500)
    private String cccdFrontUrl;

    @Column(name = "cccd_back_url", nullable = false, length = 500)
    private String cccdBackUrl;

    @ColumnDefault("0")
    @Column(name = "status")
    private Integer status;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Nationalized
    @Lob
    @Column(name = "description")
    private String description;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Nationalized
    @Lob
    @Column(name = "reason")
    private String reason;
}