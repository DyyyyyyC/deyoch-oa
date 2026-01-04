package com.deyoch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeyochPermission {

  @TableId(type = IdType.AUTO)
  private Long id;
  private String permName;
  private String permCode;
  private String permType;
  private Long parentId;
  private String path;
  private String component;
  private String icon;
  private Long sort;
  private Long status;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

}
