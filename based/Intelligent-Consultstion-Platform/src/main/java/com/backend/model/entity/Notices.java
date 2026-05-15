package com.backend.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.sql.Timestamp;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  系统公告实体类。
 *
 * @author 佳尔宇柔
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notices")
public class Notices implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator,value= KeyGenerators.snowFlakeId)
    private Integer noticeId;

    private String title;

    private String content;

    private String author;

    private Timestamp publishDate;

    private String status;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
