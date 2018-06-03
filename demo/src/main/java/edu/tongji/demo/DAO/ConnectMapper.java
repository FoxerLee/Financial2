package edu.tongji.demo.dao;

import edu.tongji.demo.model.Connect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.ArrayList;

@Mapper
public interface ConnectMapper {
    @Select("select code, name from stock_connect where code = \'${code}\'")
    ArrayList<Connect> getDataByCode(@Param("code") String code);

    @Select("select * from stock_connect where name = \'${name}\'")
    ArrayList<Connect> getDataByName(@Param("name") String name);

    @Select("select * from stock_connect where c_name = \'${c_name}\' or c_name = \'${c_name}\n\'")
    ArrayList<Connect> getDataByCName(@Param("c_name")String c_name);

    @Select("select name from stock_connect where code = \'${code}\'")
    String getName(@Param(value = "code")String code);

    @Select("select res from stock_connect where code = \'${code}\'")
    Integer getPredict(@Param(value = "code")String code);
}