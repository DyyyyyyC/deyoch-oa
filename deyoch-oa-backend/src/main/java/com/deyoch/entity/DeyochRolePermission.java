package com.deyoch.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeyochRolePermission {

  private long roleId;
  private long permId;

}
