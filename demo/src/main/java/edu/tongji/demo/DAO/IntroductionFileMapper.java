package edu.tongji.demo.DAO;

import edu.tongji.demo.Model.IntroductionFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IntroductionFileMapper {

    @Select("select * from introduction_file where name = \'${f_name}\'")
    IntroductionFile getMModelByName(@Param(value="f_name") String f_name);

}
