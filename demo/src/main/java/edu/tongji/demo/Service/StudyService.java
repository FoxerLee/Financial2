package edu.tongji.demo.Service;

import edu.tongji.demo.Model.IntroductionFile;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface StudyService {

    IntroductionFile getFileByName(String name);

    Map<String, Object> createStrategy(String name, String brief, List<Map<String, Object>> codes, Integer user_id) throws SQLException;

    Map<String, Object> getInformation(String name, Integer user_id);

    List<Map<String, Object>> getCodes(Integer strategy_id);

    Object updateUserStrategy(List<Map<String, Object>> newData,Integer strategy_id);
}
