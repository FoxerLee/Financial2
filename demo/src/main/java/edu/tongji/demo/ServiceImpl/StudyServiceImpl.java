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
import sun.plugin.javascript.navig.LinkArray;

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
                                 List<Map<String, Double>> codes, Integer user_id) throws SQLException{
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
    public Map<String, Object> getInformation(String name, Integer user_id){
        String queryInfo = "SELECT * FROM strategy WHERE strategy_name = \'" + name + "\' and user_id=" + user_id;
        SqlRowSet rs = jdbcTemplate.queryForRowSet(queryInfo);
        Map<String, Object> result = new HashMap<>();
        if (rs.next()){
            String s_name = rs.getString("strategy_name");
            String s_brief = rs.getString("brief");
            result.put("name", s_name);
            result.put("brief", s_brief);
        }
        List<Map<String, Object>> result_data = getCodes(name, user_id);
        result.put("data", result_data);
        return result;
    }

    @Override
    public List<Map<String, Object>> getCodes(String name, Integer user_id){
        String queryCodes = "SELECT * FROM user_strategy WHERE strategy_name = \'" + name + "\' and user_id=" + user_id;
        SqlRowSet rs = jdbcTemplate.queryForRowSet(queryCodes);
        List result_data = new ArrayList();
        while(rs.next()){
            Map<String, Object> data = new HashMap<>();
            data.put("code_id", rs.getString("code_id"));
            data.put("number", rs.getDouble("stock_num"));
            result_data.add(data);
        }
        return result_data;
    }

    @Override
    public Object updateUserStrategy(List<Map<String, Object>> oldData, List<Map<String, Object>> newData,
                                     String strategtName, Integer user_id){
        List<Map<String, Object>> log = new ArrayList<>();
        for (int i = 0; i < newData.size(); i++){
            boolean tag  = false;
            double old_value = 0;
            for (int j = 0; j < oldData.size(); j++){
                if (newData.get(i).get("code_id").equals(oldData.get(j).get("code_id"))){
                    tag = true;
                    old_value = Double.parseDouble(oldData.get(j).get("number").toString());
                    break;
                }
            }
            if (tag) {
                if (Double.parseDouble(newData.get(i).get("number").toString()) != old_value) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("code_id", newData.get(i).get("code_id"));
                    update.put("new_num", newData.get(i).get("number"));
                    update.put("old_num", old_value);
                    SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT close_value from data_real_time WHERE code = " +
                            "\'" + newData.get(i).get("code_id") + "\'");
                    double value = 0.0;
                    if (rs.next())
                        value = rs.getDouble("close_value");
                    jdbcTemplate.execute("UPDATE user_strategy SET initial_value=" + value
                            + ", stock_num=" + newData.get(i).get("number") + " WHERE strategy_name = " +
                            "\'" + strategtName + "\'" + "and user_id = " + user_id + " AND code_id = \'" + newData.get(i).get("code_id") + "\'");
                    log.add(update);
                }
            } else {
                Map<String, Object> insert = new HashMap<>();
                insert.put("code_id", newData.get(i).get("code_id"));
                insert.put("new_num", newData.get(i).get("number"));
                insert.put("old_num", 0);
                SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT close_value from data_real_time WHERE code = " +
                        "\'" + newData.get(i).get("code_id") + "\'");
                double value = 0.0;
                if (rs.next())
                    value = rs.getDouble("close_value");
                String sql = "INSERT INTO user_strategy(strategy_name, user_id, code_id, stock_num, initial_value)" +
                        " VALUES (\'"+ strategtName + "\', "+user_id +",\'" + newData.get(i).get("code_id") + "\'," + newData.get(i).get("number") + "," + value;

                jdbcTemplate.execute("INSERT INTO user_strategy(strategy_name, user_id, code_id, stock_num, initial_value)" +
                        " VALUES (\'"+ strategtName + "\', "+user_id +",\'" + newData.get(i).get("code_id") + "\'," + newData.get(i).get("number") + "," + value + ")");

                log.add(insert);
            }
        }
        for (int i = 0; i < oldData.size(); i++){
            boolean emerge = false;
            for (int j= 0; j < newData.size(); j++){
                if (oldData.get(i).get("code_id").equals(newData.get(j).get("code_id"))){
                    emerge = true;
                    break;
                }
            }
            if (!emerge){
                Map<String, Object> delete = new HashMap<>();
                delete.put("code_id", oldData.get(i).get("code_id"));
                delete.put("new_num", 0);
                delete.put("old_num", oldData.get(i).get("number"));
                jdbcTemplate.execute("DELETE FROM user_strategy WHERE strategy_name=\'" +strategtName+ "\' and " +
                        "user_id=" + user_id + " and code_id= \'" + oldData.get(i).get("code_id")+ "\'");
                log.add(delete);
            }
        }
        for (int i = 0; i < log.size(); i++){
//            SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT close_value from data_real_time WHERE code = " +
//                    "\'" + log.get(i).get("code_id") + "\'");
//            double value = 0.0;
//            if (rs.next())
//                value = rs.getDouble("close_value");
            jdbcTemplate.execute("INSERT INTO strategy_change(user_id, strategy_name, code_id, new_num, old_num) VALUES " +
                    "("+user_id+", \'"+strategtName+"\', \'"+ log.get(i).get("code_id")+"\', "+log.get(i).get("new_num")+","+ log.get(i).get("old_num")+")");
        }
        return null;
    }
}
