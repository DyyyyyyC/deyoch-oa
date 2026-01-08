package com.deyoch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.deyoch.dto.ContactDto;
import com.deyoch.dto.OrgTreeDto;
import com.deyoch.entity.DeyochUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通讯录数据访问层
 * 
 * @author deyoch
 * @since 2026-01-08
 */
@Mapper
public interface ContactMapper extends BaseMapper<DeyochUser> {
    
    /**
     * 分页查询通讯录
     * 
     * @param page 分页对象
     * @param keyword 搜索关键词
     * @param deptId 部门ID
     * @return 通讯录分页数据
     */
    IPage<ContactDto> selectContactDirectory(Page<ContactDto> page, 
                                           @Param("keyword") String keyword, 
                                           @Param("deptId") Long deptId);
    
    /**
     * 查询所有部门信息（用于构建组织架构树）
     * 
     * @return 部门列表
     */
    List<OrgTreeDto> selectDepartmentTree();
    
    /**
     * 根据部门ID查询用户信息（用于构建组织架构树）
     * 
     * @param deptId 部门ID
     * @return 用户列表
     */
    List<OrgTreeDto> selectUsersByDeptId(@Param("deptId") Long deptId);
    
    /**
     * 查询所有通讯录数据（用于导出）
     * 
     * @return 通讯录列表
     */
    List<ContactDto> selectAllContacts();
    
    /**
     * 根据关键词搜索联系人
     * 
     * @param keyword 搜索关键词
     * @return 联系人列表
     */
    List<ContactDto> searchContacts(@Param("keyword") String keyword);
}