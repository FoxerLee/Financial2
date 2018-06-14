package edu.tongji.demo.Controller;

import edu.tongji.demo.Model.IntroductionFile;
import edu.tongji.demo.Security.Verification;
import edu.tongji.demo.Service.StudyService;
import edu.tongji.demo.Service.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/study")
public class StudyController {

    @Autowired
    private StudyService studyService;

    @Autowired
    private UserService userService;

    @GetMapping("/introduction")
    public Object getIntroduction(@RequestParam(value = "name")String name){
        IntroductionFile introductionFile= studyService.getFileByName(name);
        if (introductionFile == null){
            return "Not found";
        }else{
            String content = introductionFile.getContent();
            Map<String, Object> result  = new HashMap<>();
            result.put("title", name);
            result.put("text", content);
            return result;
        }
    }

    @PostMapping("/strategy/create")
    public Object createStrategy(@RequestBody String text, HttpServletRequest request) {
        if (!Verification.verify()){
            return "error!";
        }else{
            JSONObject jsonObject = JSONObject.fromObject(text);
            String strategyName = jsonObject.getString("name");
            String brief = jsonObject.getString("brief");
            String codes = jsonObject.getString("code");
            JSONArray jsonCodes = JSONArray.fromObject(codes);
            List<Map<String, Object>> codeData = new ArrayList<>();
            for (int i = 0; i < jsonCodes.size(); i++) {
                Map<String, Object> codeMap = new HashMap<>();
                JSONObject temp_code = JSONObject.fromObject(jsonCodes.get(i));
                codeMap.put("code_id", temp_code.getString("code_id"));
                codeMap.put("num", temp_code.getDouble("num"));
                codeData.add(codeMap);
            }
//        for (int i = 1; i <= 3; i++){
//            Map<String, Double> data = new HashMap<>();
//            String name = jsonObject.getString("code_id" + i);
//            if (name == null || name.length() == 0){
//                continue;
//            }
//            Double num = jsonObject.getDouble("num" + i);
//            data.put(name, num);
//            codeData.add(data);
//        }
//        int user_id = userService.getIDByName(userService.getNameByCookie(request));

            int user_id = 4;
            try{
                Map<String, Object> value = studyService.createStrategy(strategyName, brief, codeData, user_id);
                if (Double.parseDouble(value.get("left_storage").toString()) > 0)
                    value.put("status", 200);
                else if(Double.parseDouble(value.get("left_storage").toString()) == -1.0)
                    value.put("status", 300);
                else
                    value.put("status", 400);
                return value;
            }catch (SQLException e){
                System.out.println(e);
                return -1;
            }
        }
    }

    // 获取策略的基本信息
    @GetMapping("/strategy/information")
    public Object getInformation(){
//        if (!Verification.verify()){
//            return "False";
//        }else{
//
//        }
        Map<String, Object> map = new HashMap<>();
        map.put("data", studyService.getInformation());
        return map;
    }

//     修改策略
    @PostMapping("/strategy/change")
    public Object changeStrategy(@RequestBody String content, HttpServletRequest request){
//        if (!Verification.verify()){
//            return "error!";
//        }else{
//
//        }
        System.out.println(content);
//            int user_id = userService.getIDByName(userService.getNameByCookie(request));
        JSONObject jsonObject = JSONObject.fromObject(content);
        JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("data"));
        Integer strategy_id = jsonObject.getInt("strategy_id");
        List<Map<String, Object>> old_data = studyService.getCodes(strategy_id);
        List<Map<String, Object>> post_data = new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++){
            Map<String, Object> data = new HashMap<>();
            JSONObject jsonObject1 = JSONObject.fromObject(jsonArray.get(i));
            data.put("code_id", jsonObject1.getString("code_id"));
            data.put("number", jsonObject1.getDouble("number"));
            post_data.add(data);
        }
        Object result = studyService.changeLog(strategy_id, old_data, post_data);
        studyService.updateUserStrategy(post_data, strategy_id);
        return result;
    }

    @GetMapping("/strategy/detail")
    public Object getStrategyDetail(@RequestParam(value = "strategy_id")String id){
        Integer strategy_id = Integer.parseInt(id);
        return studyService.getStrategtDetail(strategy_id);
    }

    @GetMapping("/strategy/self")
    public Object getStrategySelf(@RequestParam(value = "strategy_id")String id){
        Integer strategy_id = Integer.parseInt(id);
        return studyService.getSelfStrategy(strategy_id);
    }

    @GetMapping("/strategy/log/month")
    public Object getStrategyMonth(@RequestParam(value = "strategy_id")String id){
        Integer strategy_id = Integer.parseInt(id);
        return studyService.getMonth3Data(strategy_id);
    }

    @GetMapping("/strategy/log/year")
    public Object getStrategyYear(@RequestParam(value = "strategy_id")String id){
        Integer strategy_id = Integer.parseInt(id);
        return studyService.getYearData(strategy_id);
    }

    @GetMapping("/strategy/log/all")
    public Object getStrategyAll(@RequestParam(value = "strategy_id")String id){
        Integer strategy_id = Integer.parseInt(id);
        return studyService.getAllData(strategy_id);
    }
}
