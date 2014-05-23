package org.openmrs.module.bahmnicore.web.v1_0.controller;


import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.DrugOrder;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.*;


@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/orders")
public class BahmniOrderController {

    @Autowired
    private OrderService orderService;
    private static Logger logger = Logger.getLogger(BahmniOrderController.class);


    public BahmniOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public BahmniOrderController() {
    }

    //TODO: Active orders are available in OMRS 1.10.x. Consider moving once we upgrade OpenMRS.
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Map> getActiveDrugOrders(@RequestParam(value = "patientUuid") String patientUuid){
        logger.info("Retrieving active drug orders for patient with uuid " + patientUuid);
        List<DrugOrder> activeDrugOrders = orderService.getActiveDrugOrders(patientUuid);
        logger.info(activeDrugOrders.size() + " active drug orders found");

        return mapToResponse(activeDrugOrders);
    }

    private ArrayList<Map> mapToResponse(List<DrugOrder> activeDrugOrders) {
        ArrayList<Map> response = new ArrayList<>();
        for (DrugOrder drugOrder : activeDrugOrders) {
            HashMap<String, Object> responseHashMap = new HashMap<>();
            responseHashMap.put("name", drugOrder.getDrug().getName());
            responseHashMap.put("orderDate", serializeDate(drugOrder.getStartDate()));

            responseHashMap.put("dosage", drugOrder.getDrug().getDosageForm().getDisplayString());
            responseHashMap.put("dose", drugOrder.getDose());
            if (drugOrder.getAutoExpireDate() != null) {
                DateTime autoExpireDate = new DateTime(drugOrder.getAutoExpireDate());
                DateTime startDate = new DateTime(drugOrder.getStartDate());
                responseHashMap.put("days", Days.daysBetween(startDate, autoExpireDate).getDays());
            }
            responseHashMap.put("name", drugOrder.getDrug().getName());
            response.add(responseHashMap);
        }
        return response;
    }

    private String serializeDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

}
