package edu.tongji.demo.serviceimpl;

import edu.tongji.demo.dao.IntroductionFileMapper;
import edu.tongji.demo.model.IntroductionFile;
import edu.tongji.demo.service.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudyServiceImpl implements StudyService{

    @Autowired
    private IntroductionFileMapper introductionFileMapper;

    @Override
    public IntroductionFile getFileByName(String name){
        try{
            IntroductionFile introductionFile = introductionFileMapper.getMModelByName(name);
            return introductionFile;
        }catch (Exception e){
            return null;
        }
    }

}
