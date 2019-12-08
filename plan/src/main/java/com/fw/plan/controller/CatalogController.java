package com.fw.plan.controller;

import com.fw.plan.PO.CatalogPO;
import com.fw.plan.dao.CatalogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/catalog")
public class CatalogController {

    @Autowired
    private CatalogDao catalogDao;

    @ResponseBody
    @RequestMapping(value = "/getCatalogList", method = RequestMethod.GET)
    public List<CatalogPO> getCatalogList() {
        return catalogDao.getCatalogList();
    }

    @ResponseBody
    @RequestMapping(value = "/initCatalog", method = RequestMethod.POST)
    public String initCatalog(@RequestBody List<CatalogPO> list) {
        return catalogDao.initCatalog(list);
    }

    @ResponseBody
    @RequestMapping(value = "/addCatalog", method = RequestMethod.POST)
    public Object initCatalog(@RequestBody CatalogPO catalogPO) {
        CatalogPO result = catalogDao.addCatalog(catalogPO);

        if (result == null)
            return "添加失败";

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/deleteCatalog", method = RequestMethod.POST)
    public String deleteCatalog(@RequestBody CatalogPO catalogPO) {
        String result = catalogDao.deleteCatalog(catalogPO);

        if (result == null)
            return "添加失败";

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateCatalogOrder/{position}", method = RequestMethod.POST)
    public String updateCatalogOrder(@RequestBody Map<String, CatalogPO> map, @PathVariable("position") int position) {
        String result = catalogDao.updateCatalogOrder(map.get("current"), map.get("old"), map.get("parent"), position);

        if (result == null)
            return "添加失败";

        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/updateCatalogName", method = RequestMethod.POST)
    public String updateCatalogName(@RequestBody CatalogPO catalogPO) {
        String result = catalogDao.updateCatalogName(catalogPO);

        if (result == null)
            return "添加失败";

        return result;
    }
}
