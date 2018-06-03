package edu.tongji.demo.service;

import javax.servlet.http.HttpServletRequest;

public interface ResearchService {
    Object getBriefResearchByCode(String code);

    Object getPersonalResearch(HttpServletRequest request);
}
