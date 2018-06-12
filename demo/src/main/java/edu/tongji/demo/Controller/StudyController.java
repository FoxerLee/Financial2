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
//            String codes = jsonObject.getString("code");
//            JSONArray jsonCodes = JSONArray.fromObject(codes);
            List<Map<String, Double>> codeData = new ArrayList<>();
//            for (int i = 0; i < jsonCodes.size(); i++) {
//                Map<String, Double> codeMap = new HashMap<>();
//                JSONObject temp_code = JSONObject.fromObject(jsonCodes.get(i));
//                codeMap.put(temp_code.getString("code_id"), temp_code.getDouble("number"));
//                codeData.add(codeMap);
//            }
            for (int i = 1; i <= 3; i++){
                Map<String, Double> data = new HashMap<>();
                String name = jsonObject.getString("code_id" + i);
                if (name == null || name.length() == 0){
                    continue;
                }
                Double num = jsonObject.getDouble("num" + i);
                data.put(name, num);
                codeData.add(data);
            }
            int user_id = userService.getIDByName(userService.getNameByCookie(request));
            try{
                String result = studyService.createStrategy(strategyName, brief, codeData, user_id);
                return result;
            }catch (SQLException e){
                System.out.println(e);
                return "error";
            }
        }
    }

    // 获取策略的基本信息(用户id和策略名称)
    @GetMapping("/strategy/information")
    public Object getInformation(@RequestParam(value = "name")String name, HttpServletRequest request){
        if (!Verification.verify()){
            return "False";
        }else{
            int user_id = userService.getIDByName(userService.getNameByCookie(request));
            Map<String, Object> information = studyService.getInformation(name, user_id);
            return information;
        }
    }

//     修改策略
    @PostMapping("/strategy/change")
    public Object changeStrategy(@RequestBody String content, HttpServletRequest request){
        if (!Verification.verify()){
            return "error!";
        }else{
            int user_id = userService.getIDByName(userService.getNameByCookie(request));
            JSONObject jsonObject = JSONObject.fromObject(content);
            String strategy_name = jsonObject.getString("strategy_name");
            JSONArray jsonArray = JSONArray.fromObject(jsonObject.getString("data"));
            List<Map<String, Object>> old_data = studyService.getCodes(strategy_name, user_id);
            List<Map<String, Object>> post_data = new ArrayList();
            for (int i = 0; i < jsonArray.size(); i++){
                Map<String, Object> data = new HashMap<>();
                JSONObject jsonObject1 = JSONObject.fromObject(jsonArray.get(i));
                data.put("code_id", jsonObject1.getString("code_id"));
                data.put("number", jsonObject1.getDouble("number"));
                post_data.add(data);
            }
            studyService.updateUserStrategy(old_data, post_data, strategy_name, user_id);
            return post_data;
        }

    }
}
