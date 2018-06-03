package edu.tongji.demo.service;

import edu.tongji.demo.model.Connect;

import java.util.ArrayList;

public interface StockService {

    ArrayList<Connect> getStocksByUserID(Integer id);

    Object getStockByNameOrCode(String content);

    Object getStocksOfIndustry(String name);

    Object getStocksHistory(String code, int type);

    Object getStockBriefInformation(String code);

    String getStockNameByCode(String code);

    Integer getPredict(String code);
}
