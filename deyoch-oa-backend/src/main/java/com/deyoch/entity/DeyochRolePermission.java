package com.deyoch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeyochRolePermission {

  @TableId(type = IdType.AUTO)
  private Long id;
  private Long roleId;
  private Long permId;

}
