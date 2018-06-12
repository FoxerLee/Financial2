package edu.tongji.demo.Service;

import edu.tongji.demo.Model.IntroductionFile;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface StudyService {

    IntroductionFile getFileByName(String name);

    Integer createStrategy(String name, String brief, List<Map<String, Double>> codes, Integer user_id) throws SQLException;

    Map<String, Object> getInformation(String name, Integer user_id);

    List<Map<String, Object>> getCodes(String name, Integer user_id);

    Object updateUserStrategy(List<Map<String, Object>> oldData, List<Map<String, Object>> newData,
                                     String strategtName, Integer user_id);
}
