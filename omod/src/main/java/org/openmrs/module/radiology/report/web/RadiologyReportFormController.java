/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.radiology.report.web;

import org.openmrs.Order;
import org.openmrs.module.radiology.dicom.DicomWebViewer;
import org.openmrs.module.radiology.order.RadiologyOrder;
import org.openmrs.module.radiology.order.web.RadiologyOrderFormController;
import org.openmrs.module.radiology.report.RadiologyReport;
import org.openmrs.module.radiology.report.RadiologyReportService;
import org.openmrs.module.radiology.report.RadiologyReportValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = RadiologyReportFormController.RADIOLOGY_REPORT_FORM_REQUEST_MAPPING)
public class RadiologyReportFormController {
    
    
    protected static final String RADIOLOGY_REPORT_FORM_REQUEST_MAPPING = "/module/radiology/radiologyReport.form";
    
    static final String RADIOLOGY_REPORT_FORM_VIEW = "/module/radiology/reports/radiologyReportForm";
    
    @Autowired
    private RadiologyReportService radiologyReportService;
    
    @Autowired
    private DicomWebViewer dicomWebViewer;
    
    @Autowired
    private RadiologyReportValidator radiologyReportValidator;
    
    /**
     * Handles GET requests for the radiologyReportForm creating a new radiology report for given
     * radiology order
     * 
     * @param radiologyOrder radiology order for which a radiology report will be created
     * @return model and view containing new radiology report for given radiology order
     * @should populate model and view with new radiology report for given radiology order
     */
    @RequestMapping(method = RequestMethod.GET, params = "orderId")
    protected ModelAndView getRadiologyReportFormWithNewRadiologyReport(
            @RequestParam(value = "orderId", required = true) RadiologyOrder radiologyOrder) {
        ModelAndView modelAndView = new ModelAndView(RADIOLOGY_REPORT_FORM_VIEW);
        
        final RadiologyReport radiologyReport = radiologyReportService.createAndClaimRadiologyReport(radiologyOrder);
        modelAndView = new ModelAndView(
                "redirect:" + RADIOLOGY_REPORT_FORM_REQUEST_MAPPING + "?radiologyReportId=" + radiologyReport.getId());
        
        addObjectsToModelAndView(modelAndView, radiologyReport);
        return modelAndView;
    }
    
    /**
     * Handles GET requests for the radiologyReportForm when called for existing radiology reports
     * 
     * @param radiologyReportId radiology report id of the existing radiology report which should be
     *        put into the model and view
     * @return model and view containing radiology report for given radiology report id
     * @should populate model and view with existing radiology report matching given radiology
     *         report id
     */
    @RequestMapping(method = RequestMethod.GET, params = "radiologyReportId")
    protected ModelAndView getRadiologyReportFormWithExistingRadiologyReport(
            @RequestParam(value = "radiologyReportId", required = true) RadiologyReport radiologyReport) {
        
        ModelAndView modelAndView = new ModelAndView(RADIOLOGY_REPORT_FORM_VIEW);
        addObjectsToModelAndView(modelAndView, radiologyReport);
        return modelAndView;
    }
    
    /**
     * Handles POST request for the RadiologyReportForm to save a RadiologyReport
     *
     * @param radiologyReport radiology report to be saved
     * @return ModelAndView containing saved radiology report
     * @should save given radiology report and populate model and view with it
     */
    @RequestMapping(method = RequestMethod.POST, params = "saveRadiologyReport")
    protected ModelAndView saveRadiologyReport(@ModelAttribute("radiologyReport") RadiologyReport radiologyReport) {
        
        final ModelAndView modelAndView = new ModelAndView(RADIOLOGY_REPORT_FORM_VIEW);
        radiologyReportService.saveRadiologyReport(radiologyReport);
        
        addObjectsToModelAndView(modelAndView, radiologyReport);
        return modelAndView;
    }
    
    /**
     * Handles POST request for the RadiologyReportForm to unclaim given RadiologyReport
     *
     * @param radiologyReport radiology report to be unclaimed
     * @return ModelAndView with redirect to order form if unclaim was successful, otherwise stay on
     *         report form
     * @should redirect to radiology order form if unclaim was successful
     */
    @RequestMapping(method = RequestMethod.POST, params = "unclaimRadiologyReport")
    protected ModelAndView unclaimRadiologyReport(@ModelAttribute("radiologyReport") RadiologyReport radiologyReport) {
        
        radiologyReportService.unclaimRadiologyReport(radiologyReport);
        return new ModelAndView("redirect:" + RadiologyOrderFormController.RADIOLOGY_ORDER_FORM_REQUEST_MAPPING + "?orderId="
                + radiologyReport.getRadiologyOrder()
                        .getOrderId());
    }
    
    /**
     * Handles POST request for the RadiologyReportForm to complete given RadiologyReport
     *
     * @param radiologyReport radiology report to be completed
     * @param bindingResult BindingResult
     * @return ModelAndView RadiologyOrderForm if complete was successful, otherwise the
     *         ModelAndView with BindingResult errors
     * @should complete given radiology report and populate model and view with it
     * @should populate model and view radiology report form with BindingResult errors if provider
     *         is null
     */
    @RequestMapping(method = RequestMethod.POST, params = "completeRadiologyReport")
    protected ModelAndView completeRadiologyReport(@ModelAttribute("radiologyReport") RadiologyReport radiologyReport,
            BindingResult bindingResult) {
        
        final ModelAndView modelAndView = new ModelAndView(RADIOLOGY_REPORT_FORM_VIEW);
        
        radiologyReportValidator.validate(radiologyReport, bindingResult);
        if (bindingResult.hasErrors()) {
            addObjectsToModelAndView(modelAndView, radiologyReport);
            return modelAndView;
        }
        
        final RadiologyReport completedRadiologyReport = radiologyReportService.completeRadiologyReport(radiologyReport,
            radiologyReport.getPrincipalResultsInterpreter());
        addObjectsToModelAndView(modelAndView, completedRadiologyReport);
        return modelAndView;
    }
    
    /**
     * Convenience method to add objects (Order, RadiologyOrder, RadiologyReport) to given
     * ModelAndView
     *
     * @param modelAndView model and view to which objects should be added
     * @param radiologyReport radiology report from which objects should be added to the model and
     *        view
     */
    private void addObjectsToModelAndView(ModelAndView modelAndView, RadiologyReport radiologyReport) {
        
        modelAndView.addObject("radiologyReport", radiologyReport);
        modelAndView.addObject("order", (Order) radiologyReport.getRadiologyOrder());
        modelAndView.addObject("dicomViewerUrl", dicomWebViewer.getDicomViewerUrl(radiologyReport.getRadiologyOrder()
                .getStudy()));
        modelAndView.addObject("radiologyOrder", radiologyReport.getRadiologyOrder());
    }
}
