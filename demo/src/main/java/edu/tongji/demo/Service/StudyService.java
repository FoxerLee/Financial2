package edu.tongji.demo.Service;

import edu.tongji.demo.Model.IntroductionFile;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface StudyService {

    IntroductionFile getFileByName(String name);

    Map<String, Object> createStrategy(String name, String brief, List<Map<String, Object>> codes, Integer user_id) throws SQLException;

    Object getInformation();

    List<Map<String, Object>> getCodes(Integer strategy_id);

    Object updateUserStrategy(List<Map<String, Object>> newData,Integer strategy_id);

    Object changeLog(Integer strategy_id, List<Map<String, Object>> oldData, List<Map<String, Object>> newData);

    Object getStrategtDetail(Integer strategy_id);

    Object getSelfStrategy(Integer strategy_id);

    Object getMonth3Data(Integer strategy_id);

    Object getYearData(Integer strategy_id);

    Object getAllData(Integer strategy_id);

    Object getProfileStrategy(Integer user_id);

    void deleteStrategy(Integer id);
}