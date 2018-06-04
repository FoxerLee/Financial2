package edu.tongji.demo.ServiceImpl;

import edu.tongji.demo.Model.News;
import edu.tongji.demo.DAO.IntroductionFileMapper;
import edu.tongji.demo.Model.IntroductionFile;
import edu.tongji.demo.Service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudyServiceImpl implements StudyService{

    @Autowired
    private IntroductionFileMapper introductionFileMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public IntroductionFile getFileByName(String name){
        try{
            IntroductionFile introductionFile = introductionFileMapper.getMModelByName(name);
            return introductionFile;
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public String createStrategy(String name, String brief,
                                 List<Map<String, Integer>> codes, Integer user_id) throws SQLException{
        String sql = "select count(*) from strategy where user_id = " + user_id +
                " and strategy_name = \'" + name + "\'";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql);
        if (rs.next()){
            // 如果已经有了这个策略
            if (rs.getInt(1) > 0){
                return "already have this strategy";
            }
        }
        String createStrategySql = "INSERT INTO strategy(user_id, strategy_name, brief) VALUES ("
                + user_id + ", \'" + name + "\'," + "\'" + brief+ "\' " + ")";
        jdbcTemplate.execute(createStrategySql);
        for (int i = 0; i < codes.size(); i++){
            String getReadDataSQL = "select close_value from data_real_time where code = " +
                    "\'" + codes.get(i).keySet().toArray()[0] + "\'";
            SqlRowSet realData = jdbcTemplate.queryForRowSet(getReadDataSQL);
            if (realData.next()){
                double value = realData.getDouble("close_value");
                String createUserStrategySQL =
                        "INSERT INTO user_strategy(strategy_name, user_id, code_id, stock_num, initial_value) " +
                                "VALUES ("+ "\'" + name +"\', " + user_id +","+ "\'" +  codes.get(i).keySet().toArray()[0]
                                + "\'," + codes.get(i).values().toArray()[0] +","+ value +");";
                jdbcTemplate.execute(createUserStrategySQL);
                System.out.println(createUserStrategySQL);
            }
        }
        return "successfully";
    }

    @Override
    public Object getInformation(String name, Integer user_id){
        String queryInfo = "SELECT * FROM strategy WHERE strategy_name = \'" + name + "\' and user_id=" + user_id;
        SqlRowSet rs = jdbcTemplate.queryForRowSet(queryInfo);
        Map<String, Object> result = new HashMap<>();
        if (rs.next()){
            String s_name = rs.getString("strategy_name");
            String s_brief = rs.getString("brief");
            result.put("name", s_name);
            result.put("brief", s_brief);
        }
        String queryCodes = "SELECT * FROM user_strategy WHERE strategy_name = \'" + name + "\' and user_id=" + user_id;
        rs = jdbcTemplate.queryForRowSet(queryCodes);
        List result_data = new ArrayList();
        while(rs.next()){
            Map<String, Object> data = new HashMap<>();
            data.put("code_id", rs.getString("code_id"));
            data.put("number", rs.getDouble("stock_num"));
            result_data.add(data);
        }
        result.put("data", result_data);
        return result;
    }

}
