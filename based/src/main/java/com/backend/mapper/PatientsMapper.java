package com.backend.mapper;

import com.backend.model.entity.Patients;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 *  患者信息 Mapper 接口。
 *
 * @author 佳尔宇柔
 */
@Mapper
public interface PatientsMapper extends BaseMapper<Patients> {

}
