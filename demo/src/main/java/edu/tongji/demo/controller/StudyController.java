package edu.tongji.demo.Controller;

import edu.tongji.demo.Model.IntroductionFile;
import edu.tongji.demo.Service.StudyService;
import edu.tongji.demo.Service.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            return introductionFile.getContent();
        }
    }

    @PostMapping("/strategy/create")
    public Object createStrategy(@RequestBody String text) {
        JSONObject jsonObject = JSONObject.fromObject(text);
        String strategyName = jsonObject.getString("name");
        String brief = jsonObject.getString("brief");
        String codes = jsonObject.getString("code");
        JSONArray jsonCodes = JSONArray.fromObject(codes);
        List<Map<String, Integer>> codeData = new ArrayList<>();
        for (int i = 0; i < jsonCodes.size(); i++) {
            Map<String, Integer> codeMap = new HashMap<>();
            JSONObject temp_code = JSONObject.fromObject(jsonCodes.get(i));
            codeMap.put(temp_code.getString("code_id"), Integer.parseInt(temp_code.getString("number")));
            codeData.add(codeMap);
        }
        int user_id = 5;
        try{
            String result = studyService.createStrategy(strategyName, brief, codeData, user_id);
            return result;
        }catch (SQLException e){
            System.out.println(e);
            return "error";
        }
    }

    // 获取策略的基本信息(用户id和策略名称)
    @GetMapping("/strategy/information")
    public Object getInformation(@RequestParam(value = "name")String name, @RequestParam(value = "id")String user_id){
        Object information = studyService.getInformation(name, Integer.parseInt(user_id));
        return information;
    }

//    @PostMapping("/strategy/change")
//    public String changeStrategy(@RequestBody String text){
//        JSONArray jsonArray = JSONArray.fromObject(text);
//    }

}
