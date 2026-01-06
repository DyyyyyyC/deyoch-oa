package com.deyoch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeyochDocument {

  @TableId(type = IdType.AUTO)
  private Long id;
  private String fileName;
  private String filePath;
  private Long fileSize;
  private String fileType;
  private Long userId;
  private Long deptId;
  private Integer status;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  // 非数据库字段，用于前端显示
  @TableField(exist = false)
  private String uploaderName;

}
