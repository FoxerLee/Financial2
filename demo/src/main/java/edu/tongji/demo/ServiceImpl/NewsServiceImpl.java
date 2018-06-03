package edu.tongji.demo.serviceimpl;

import edu.tongji.demo.dao.NewsMapper;
import edu.tongji.demo.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public Object getNewsByCode(String code){
        return newsMapper.getNews(code);
    }

    @Override
    public Object getBriefNewsByCode(String code){
        return newsMapper.getEveryNews(code);
    }
}
