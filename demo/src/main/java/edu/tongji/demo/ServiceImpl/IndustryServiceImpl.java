package edu.tongji.demo.serviceimpl;

import edu.tongji.demo.dao.IndustryMapper;
import edu.tongji.demo.model.Industry;
import edu.tongji.demo.service.IndustryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class IndustryServiceImpl implements IndustryService {

    @Autowired
    private IndustryMapper industryMapper;

    @Override
    public ArrayList<Industry> getAllIndustry(){
        return industryMapper.getAllIndustryInfor();
    }
}
