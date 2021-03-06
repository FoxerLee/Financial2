package edu.tongji.demo.ServiceImpl;

import edu.tongji.demo.DAO.IntroductionFileMapper;
import edu.tongji.demo.Model.IntroductionFile;
import edu.tongji.demo.Service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudyServiceImpl implements StudyService {

    @Autowired
    private IntroductionFileMapper introductionFileMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public IntroductionFile getFileByName(String name) {
        try {
            IntroductionFile introductionFile = introductionFileMapper.getMModelByName(name);
            return introductionFile;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Map<String, Object> createStrategy(String name, String brief,
                                              List<Map<String, Object>> codes, Integer user_id) {

        double left_storage = 1;
        List<Double> present_value = new ArrayList<>();
        List<Double> storage = new ArrayList<>();
        List<String> code_ids = new ArrayList<>();
        for (int i = 0; i < codes.size(); i++) {
            String code_id = codes.get(i).get("code_id").toString();
            code_ids.add(code_id);

            storage.add(Double.parseDouble(codes.get(i).get("num").toString()));
            String getValue = "SELECT close_value FROM data_real_time WHERE code = \'" + code_id + "\'";
            SqlRowSet close_value = jdbcTemplate.queryForRowSet(getValue);
            if (close_value.next()) {
                present_value.add(close_value.getDouble("close_value"));
            }
            left_storage -= Double.parseDouble(codes.get(i).get("num").toString());
        }
        String createStrategy = "INSERT INTO strategy (name, brief, user_id, left_storage, fund) values (\'" +
                name + "\',\'" + brief + "\'," + user_id + "," + left_storage + "," + 10000 + ");";
        jdbcTemplate.execute(createStrategy);
        SqlRowSet strategy_id = jdbcTemplate.queryForRowSet("SELECT id FROM strategy WHERE name=\'" + name + "\' and " +
                "user_id=" + user_id + " order by time DESC ");
        int id = 0;
        if (strategy_id.next()) {
            id = strategy_id.getInt("id");
        }
        jdbcTemplate.execute("INSERT INTO strategy_log(strategy_id, present_amount) VALUES (" + id + ", 0)");
        for (int i = 0; i < codes.size(); i++) {
            String createDetail = "INSERT INTO strategy_detail(strategy_id, code_id, storage_number, present_value, trading_storage) VALUES (" +
                    id + ", \'" + code_ids.get(i) + "\', " + storage.get(i) + "," + present_value.get(i) + "," + 10000 * storage.get(i) / present_value.get(i) + ");";
            jdbcTemplate.execute(createDetail);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("left_storage", left_storage);
        result.put("strategy_id", id);
        return result;
    }


    @Override
    public Object getInformation() {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT t1.id, t1.name, t1.brief, t2.user_id, t2.name as user_name, t3.present_amount FROM (strategy t1 left join user_info t2 ON t1.user_id = t2.user_id) left join strategy_log t3 on t1.id = t3.strategy_id where t3.time = (select max(strategy_log.time) from strategy_log where strategy_log.strategy_id = t1.id);");
        ArrayList<Map<String, Object>> data = new ArrayList<>();
        while (sqlRowSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("strategy_id", sqlRowSet.getInt("id"));
            map.put("strategy_name", sqlRowSet.getString("name"));
            map.put("brief", sqlRowSet.getString("brief"));
            map.put("user_id", sqlRowSet.getInt("user_id"));
            map.put("user_name", sqlRowSet.getString("user_name"));
            map.put("present_amount", sqlRowSet.getDouble("present_amount"));
            data.add(map);
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> getCodes(Integer strategy_id) {
        String queryCodes = "SELECT * FROM strategy_detail WHERE strategy_id = " + strategy_id + ";";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(queryCodes);
        List result_data = new ArrayList();
        while (rs.next()) {
            Map<String, Object> data = new HashMap<>();
            data.put("code_id", rs.getString("code_id"));
            data.put("number", rs.getDouble("storage_number"));
            result_data.add(data);
        }
        return result_data;
    }

    @Override
    public Object updateUserStrategy(List<Map<String, Object>> newData, Integer strategy_id) {
        SqlRowSet old_score_row_set = jdbcTemplate.queryForRowSet("SELECT present_value, trading_storage FROM strategy_detail WHERE strategy_id=" + strategy_id);
        Double old_value = 0.0;
        while (old_score_row_set.next()) {
            old_value += old_score_row_set.getDouble("present_value") * old_score_row_set.getDouble("trading_storage");
        }
        SqlRowSet old_score_left = jdbcTemplate.queryForRowSet("SELECT fund, left_storage FROM strategy WHERE id=" + strategy_id);
        if (old_score_left.next()) {
            old_value += old_score_left.getDouble("left_storage") * old_score_left.getDouble("fund");
        }
        jdbcTemplate.execute("DELETE FROM strategy_detail WHERE strategy_id = " + strategy_id + ";");
        Double tag = 1.0;
        for (int i = 0; i < newData.size(); i++) {
            String code_id = newData.get(i).get("code_id").toString();
            Double num = Double.parseDouble(newData.get(i).get("number").toString());
            SqlRowSet present_value_row_set = jdbcTemplate.queryForRowSet("SELECT close_value FROM data_real_time WHERE code = \'" + code_id + "\';");
            double present = 0.0;
            if (present_value_row_set.next()) {
                present = present_value_row_set.getDouble("close_value");
            }
            String createDetail = "INSERT INTO strategy_detail(strategy_id, code_id, storage_number, present_value, trading_storage) VALUES (" +
                    strategy_id + ", \'" + code_id + "\', " + num + "," + present + "," + old_value * num / present + ");";
            jdbcTemplate.execute(createDetail);
            tag -= num;
        }
        jdbcTemplate.update("UPDATE strategy SET fund = " + old_value + " WHERE id=" + strategy_id);
        jdbcTemplate.update("UPDATE strategy SET left_storage = " + tag + " WHERE id=" + strategy_id);
        return null;
    }


    @Override
    public Object changeLog(Integer strategy_id, List<Map<String, Object>> oldData, List<Map<String, Object>> newData) {
        List<Map<String, Object>> log = new ArrayList<>();
        for (int i = 0; i < oldData.size(); i++) {
            Boolean tag = false;
            for (int j = 0; j < newData.size(); j++) {
                if (newData.get(j).get("code_id").toString().equals(oldData.get(i).get("code_id").toString())) {
                    tag = true;
                    if (Double.parseDouble(newData.get(j).get("number").toString()) ==
                            Double.parseDouble(oldData.get(i).get("number").toString()))
                        continue;
                    else {
                        Map<String, Object> newLog = new HashMap<>();
                        newLog.put("code_id", newData.get(j).get("code_id").toString());
                        newLog.put("new", Double.parseDouble(newData.get(j).get("number").toString()));
                        newLog.put("old", Double.parseDouble(oldData.get(i).get("number").toString()));
                        log.add(newLog);
                        break;
                    }
                }
            }
            if (!tag) {
                Map<String, Object> newLog = new HashMap<>();
                newLog.put("code_id", oldData.get(i).get("code_id").toString());
                newLog.put("old", Double.parseDouble(oldData.get(i).get("number").toString()));
                newLog.put("new", 0.0);
                log.add(newLog);
            }
        }
        for (int i = 0; i < newData.size(); i++) {
            boolean tag = false;
            for (int j = 0; j < oldData.size(); j++) {
                if (newData.get(i).get("code_id").toString().equals(oldData.get(j).get("code_id").toString())) {
                    tag = true;
                    break;
                }
            }
            if (!tag) {
                Map<String, Object> newLog = new HashMap<>();
                newLog.put("code_id", newData.get(i).get("code_id").toString());
                newLog.put("new", Double.parseDouble(newData.get(i).get("number").toString()));
                newLog.put("old", 0.0);
                log.add(newLog);
            }
        }
        for (int i = 0; i < log.size(); i++) {
            jdbcTemplate.execute("INSERT INTO strategy_change(strategy_id, code_id, new, old) VALUES (" + strategy_id + ",\'" +
                    log.get(i).get("code_id").toString() + "\'," + Double.parseDouble(log.get(i).get("new").toString()) + "," +
                    Double.parseDouble(log.get(i).get("old").toString()) + ");");
        }
        return log;
    }

    @Override
    public Object getStrategtDetail(Integer strategy_id) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT t1.name as strategy_name, t2.name as user_name, t1.brief as brief FROM strategy t1 " +
                "left join user_info t2 on t1.user_id = t2.user_id where t1.id = " + strategy_id + ";");
        String user_name = "";
        String strategy_name = "";
        String brief = "";
        if (sqlRowSet.next()) {
            user_name = sqlRowSet.getString("user_name");
            strategy_name = sqlRowSet.getString("strategy_name");
            brief = sqlRowSet.getString("brief");
        }
        sqlRowSet = jdbcTemplate.queryForRowSet("select * from strategy_detail where strategy_id = " + strategy_id + ";");
        List<Map<String, Object>> code_data = new ArrayList<>();
        while (sqlRowSet.next()) {
            Map<String, Object> temp_data = new HashMap<>();
            temp_data.put("code_id", sqlRowSet.getString("code_id"));
            temp_data.put("storage_number", sqlRowSet.getDouble("storage_number"));
            temp_data.put("present_value", Double.parseDouble(String.format("%.2f", sqlRowSet.getDouble("present_value"))));
            code_data.add(temp_data);
        }
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        sqlRowSet = jdbcTemplate.queryForRowSet("select present_amount from strategy_log where time = " +
                "(select min(time) from strategy_log where strategy_id = " + strategy_id + ") and strategy_id = " + strategy_id + ";");
        double history_value = 0.0;
        if (sqlRowSet.next()) {
            history_value = sqlRowSet.getDouble("present_amount");
        }
        sqlRowSet = jdbcTemplate.queryForRowSet("select present_amount from strategy_log where time = " +
                "(select max(time) from strategy_log where strategy_id = " + strategy_id + ") and strategy_id = " + strategy_id + ";");
        double present_value = 0.0;
        if (sqlRowSet.next()) {
            present_value = sqlRowSet.getDouble("present_amount");
        }
        sqlRowSet = jdbcTemplate.queryForRowSet("select present_amount from strategy_log where UNIX_TIMESTAMP(time) > UNIX_TIMESTAMP('" + year + "-" + month + "-1') and strategy_id = " + strategy_id + " order by time asc;");
        List<Double> month_value = new ArrayList<>();
        while (sqlRowSet.next()) {
            month_value.add(sqlRowSet.getDouble("present_amount"));
        }
        double benefit_all = Double.parseDouble(String.format("%.2f", (present_value - history_value) / history_value));
        double benefit_net = Double.parseDouble(String.format("%.2f", present_value - history_value));
        double benefit_month = 0.0;
        if (month_value.size() > 1) {
            benefit_month = (month_value.get(month_value.size() - 1) - month_value.get(0)) / month_value.get(0);
            benefit_month = Double.parseDouble(String.format("%.2f", benefit_month));
        }
        sqlRowSet = jdbcTemplate.queryForRowSet("select * from strategy_change where strategy_id = " + strategy_id + ";");
        List<Map<String, Object>> change_log = new ArrayList<>();
        while (sqlRowSet.next()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("code_id", sqlRowSet.getString("code_id"));
            temp.put("old", sqlRowSet.getDouble("old"));
            temp.put("new", sqlRowSet.getDouble("new"));
            if (sqlRowSet.getDouble("new") > sqlRowSet.getDouble("old")) {
                temp.put("type", "买");
            } else {
                temp.put("type", "卖");
            }
            change_log.add(temp);
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("strategy_name", strategy_name);
        result.put("user_name", user_name);
        result.put("brief", brief);
        result.put("code_data", code_data);
        result.put("benefit_all", benefit_all);
        result.put("benefit_month", benefit_month);
        result.put("benefit_net", benefit_net);
        result.put("change_log", change_log);
        return result;
    }

    @Override
    public Object getSelfStrategy(Integer strategy_id) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT * FROM strategy_detail WHERE strategy_id = " + strategy_id + ";");
        List<Map<String, Object>> data = new ArrayList<>();
        while (sqlRowSet.next()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("code", sqlRowSet.getString("code_id"));
            temp.put("num", sqlRowSet.getDouble("storage_number"));
            data.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    @Override
    public Object getMonth3Data(Integer strategy_id) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT * FROM strategy_log where strategy_id = " + strategy_id +
                " and UNIX_TIMESTAMP(time) > UNIX_TIMESTAMP('" + year + "-" + (month - 3) + "-1') order by time asc;");
        List<List<Object>> data = new ArrayList<>();
        while (sqlRowSet.next()) {
//            Map<String, Object> temp = new HashMap<>()
            List<Object> temp = new ArrayList<>();
            temp.add(sqlRowSet.getString("time").split(" ")[0]);
            temp.add(sqlRowSet.getDouble("present_amount"));
            data.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    @Override
    public Object getYearData(Integer strategy_id) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT * FROM strategy_log where strategy_id = " + strategy_id +
                " and UNIX_TIMESTAMP(time) > UNIX_TIMESTAMP('" + year + "-1-1') order by time asc;");
        List<List<Object>> data = new ArrayList<>();
        while (sqlRowSet.next()) {
//            Map<String, Object> temp = new HashMap<>()
            List<Object> temp = new ArrayList<>();
            temp.add(sqlRowSet.getString("time").split(" ")[0]);
            temp.add(sqlRowSet.getDouble("present_amount"));
            data.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    @Override
    public Object getAllData(Integer strategy_id) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT * FROM strategy_log where strategy_id = " + strategy_id +
                " order by time asc;");
        List<List<Object>> data = new ArrayList<>();
        while (sqlRowSet.next()) {
//            Map<String, Object> temp = new HashMap<>()
            List<Object> temp = new ArrayList<>();
            temp.add(sqlRowSet.getString("time").split(" ")[0]);
            temp.add(sqlRowSet.getDouble("present_amount"));
            data.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    @Override
    public Object getProfileStrategy(Integer user_id) {
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet("SELECT name, id FROM strategy WHERE user_id=" + user_id + ";");
        List<Map<String, Object>> data = new ArrayList<>();
        while (sqlRowSet.next()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("name", sqlRowSet.getString("name"));
            temp.put("code", sqlRowSet.getString("id"));
            data.add(temp);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        return result;
    }

    @Override
    public void deleteStrategy(Integer id) {
        jdbcTemplate.execute("DELETE FROM strategy WHERE id = " + id + ";");
        jdbcTemplate.execute("DELETE FROM strategy_change WHERE strategy_id = " + id + ";");
        jdbcTemplate.execute("DELETE FROM strategy_detail WHERE strategy_id = " + id + ";");
        jdbcTemplate.execute("DELETE FROM strategy_log WHERE strategy_id = " + id + ";" );
    }

}