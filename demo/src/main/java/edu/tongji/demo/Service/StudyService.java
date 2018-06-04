package edu.tongji.demo.Service;

import edu.tongji.demo.Model.IntroductionFile;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface StudyService {

    IntroductionFile getFileByName(String name);

    String createStrategy(String name, String brief, List<Map<String, Integer>> codes, Integer user_id) throws SQLException;

    Object getInformation(String name, Integer user_id);
}
