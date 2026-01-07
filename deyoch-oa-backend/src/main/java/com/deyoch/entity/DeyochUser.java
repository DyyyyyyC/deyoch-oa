package com.deyoch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体类")
public class DeyochUser {

  @Schema(description = "主键ID")
  @TableId(type = IdType.AUTO)
  private Long id;
  @Schema(description = "用户名")
  private String username;
  @Schema(description = "密码")
  private String password;
  @Schema(description = "昵称")
  private String nickname;
  @Schema(description = "邮箱")
  private String email;
  @Schema(description = "电话")
  private String phone;
  @Schema(description = "头像")
  private String avatar;
  @Schema(description = "部门ID")
  private Long deptId;
  @Schema(description = "角色ID")
  private Long roleId;
  @Schema(description = "角色名称")
  @TableField(exist = false)
  private String roleName;
  @Schema(description = "状态：0-禁用，1-启用")
  private Integer status;
  @Schema(description = "创建时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;
  @Schema(description = "更新时间")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

}
