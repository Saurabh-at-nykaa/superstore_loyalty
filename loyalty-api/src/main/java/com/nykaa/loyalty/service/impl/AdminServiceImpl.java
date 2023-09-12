package com.nykaa.loyalty.service.impl;

import com.nykaa.loyalty.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.nykaa.cs.constants.Constants.UI_ERROR_MESSAGE;
import static com.nykaa.cs.constants.Constants.UI_SUCCESS_MESSAGE;

@Service("loyaltyAdminService")
public class AdminServiceImpl implements AdminService {

    @Override
    public void updateRedirectAttributesForSaveOrUpdate(
            RedirectAttributes redirectAttributes, boolean isCreateRequest, boolean isSuccess) {
        if (isCreateRequest) {
            if (isSuccess) {
                redirectAttributes.addFlashAttribute(UI_SUCCESS_MESSAGE, "Created Successfully");
            } else {
                redirectAttributes.addFlashAttribute(UI_ERROR_MESSAGE, "Not Created");
            }
        } else {
            if (isSuccess) {
                redirectAttributes.addFlashAttribute(UI_SUCCESS_MESSAGE, "Updated Successfully");
            } else {
                redirectAttributes.addFlashAttribute(UI_ERROR_MESSAGE, "Not Updated");
            }
        }
    }

    @Override
    public void updateRedirectAttributesForRefresh(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(UI_SUCCESS_MESSAGE, "Cache Reloaded Successfully");
    }
}
