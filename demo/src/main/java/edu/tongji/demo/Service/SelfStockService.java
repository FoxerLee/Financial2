package edu.tongji.demo.service;

import javax.servlet.http.HttpServletRequest;

public interface SelfStockService {

    boolean addStockByCode(String code, HttpServletRequest request);

    boolean deleteStockByCode(String code, HttpServletRequest request);
}
