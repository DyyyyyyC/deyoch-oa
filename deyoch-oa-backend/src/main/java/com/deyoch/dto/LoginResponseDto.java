package com.deyoch.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    private String token;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private Long deptId;

    private Long roleId;

    private java.util.List<String> roles;

    private java.util.List<String> permissions;

}
