package com.nykaa.loyalty.service;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface AdminService {

    void updateRedirectAttributesForSaveOrUpdate(
            RedirectAttributes redirectAttributes, boolean isCreateRequest, boolean isSuccess);

    void updateRedirectAttributesForRefresh(RedirectAttributes redirectAttributes);
}
