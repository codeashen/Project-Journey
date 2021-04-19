package com.ashen.xunwu.web.controller.house;

import com.ashen.xunwu.config.RentValueBlock;
import com.ashen.xunwu.entity.SupportAddress;
import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.house.IHouseService;
import com.ashen.xunwu.service.user.IUserService;
import com.ashen.xunwu.web.dto.*;
import com.ashen.xunwu.web.form.RentSearch;
import com.ashen.xunwu.base.ApiResponse;
import com.ashen.xunwu.service.search.ISearchService;
import com.ashen.xunwu.service.search.common.HouseBucketDTO;
import com.ashen.xunwu.service.house.IAddressService;
import com.ashen.xunwu.web.dto.*;
import com.ashen.xunwu.web.form.MapSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 房屋信息
 */
@Controller
public class HouseController {

    @Autowired
    private IAddressService addressService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IHouseService houseService;
    @Autowired
    private ISearchService searchService;

    /**
     * 输入自动补全接口
     * @param prefix 输入字符串
     * @return 返回提示字符串列表
     */
    @GetMapping("rent/house/autocomplete")
    @ResponseBody
    public ApiResponse<List<String>> autocomplete(@RequestParam(value = "prefix") String prefix) {

        if (prefix.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }
        ServiceResult<List<String>> result = this.searchService.suggest(prefix);
        return ApiResponse.ofSuccess(result.getResult());
    }
    
    /**
     * 获取支持城市列表
     * @return
     */
    @GetMapping("address/support/cities")
    @ResponseBody
    public ApiResponse<List<SupportAddressDTO>> getSupportCities() {
        ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
        if (result.getResultSize() == 0) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(result.getResult());
    }

    /**
     * 获取对应城市支持区域列表
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/regions")
    @ResponseBody
    public ApiResponse<List<SupportAddressDTO>> getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(cityEnName);
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(addressResult.getResult());
    }

    /**
     * 获取具体城市所支持的地铁线路
     * @param cityEnName
     * @return
     */
    @GetMapping("address/support/subway/line")
    @ResponseBody
    public ApiResponse<List<SubwayDTO>> getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
        List<SubwayDTO> subways = addressService.findAllSubwayByCity(cityEnName);
        if (subways.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(subways);
    }

    /**
     * 获取对应地铁线路所支持的地铁站点
     * @param subwayId
     * @return
     */
    @GetMapping("address/support/subway/station")
    @ResponseBody
    public ApiResponse<List<SubwayStationDTO>> getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
        List<SubwayStationDTO> stationDTOS = addressService.findAllStationBySubway(subwayId);
        if (stationDTOS.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
        }
        return ApiResponse.ofSuccess(stationDTOS);
    }

    /**
     * 租房筛选页面
     * @param rentSearch 搜索对象
     * @param model MVC的页面模型
     * @param session 会话对象
     * @param redirectAttributes MVC用于跳转的页面模型
     * @return
     */
    @GetMapping("rent/house")
    public String rentHousePage(@ModelAttribute RentSearch rentSearch,
                                Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        // 获取当前城市，获取房源列表必须指定城市
        if (rentSearch.getCityEnName() == null) {
            // 没传就送session中获取
            String cityEnNameInSession = (String) session.getAttribute("cityEnName");
            if (cityEnNameInSession == null) {
                // session中都没有，设置提示信息，重定向到首页
                redirectAttributes.addAttribute("msg", "must_chose_city");
                return "redirect:/index";
            } else {
                rentSearch.setCityEnName(cityEnNameInSession);
            }
        } else {
            // 将城市设置到session中
            session.setAttribute("cityEnName", rentSearch.getCityEnName());
        }

        // 查询城市信息
        ServiceResult<SupportAddressDTO> city = addressService.findCity(rentSearch.getCityEnName());
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        // 查询城市支持的所有区域
        ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(rentSearch.getCityEnName());
        if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        }
        // 查询房源列表
        ServiceMultiResult<HouseDTO> serviceMultiResult = houseService.query(rentSearch);

        if (rentSearch.getRegionEnName() == null) {
            rentSearch.setRegionEnName("*");
        }

        // 统一设置属性
        model.addAttribute("searchBody", rentSearch);                   // 搜索表单信息
        model.addAttribute("currentCity", city.getResult());            // 当前城市
        model.addAttribute("regions", addressResult.getResult());       // 支持的区域
        model.addAttribute("total", serviceMultiResult.getTotal());     // 房源总数
        model.addAttribute("houses", serviceMultiResult.getResult());   // 房源列表
        model.addAttribute("priceBlocks", RentValueBlock.PRICE_BLOCK);  // 价格区间列表
        model.addAttribute("areaBlocks", RentValueBlock.AREA_BLOCK);    // 面积区间列表
        model.addAttribute("currentPriceBlock", RentValueBlock.matchPrice(rentSearch.getPriceBlock())); // 当前价格区间
        model.addAttribute("currentAreaBlock", RentValueBlock.matchArea(rentSearch.getAreaBlock()));    // 当前面积区间

        return "rent-list";
    }

    /**
     * 房屋详情
     * @param houseId
     * @param model
     * @return
     */
    @GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId, Model model) {
        if (houseId <= 0) {
            return "404";
        }
        
        // 查询房源详情
        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(houseId);
        if (!serviceResult.isSuccess()) {
            return "404";
        }
        
        // 查询并设置区域信息
        HouseDTO houseDTO = serviceResult.getResult();
        Map<SupportAddress.Level, SupportAddressDTO> addressMap = 
                addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());
        
        SupportAddressDTO city = addressMap.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = addressMap.get(SupportAddress.Level.REGION);
        model.addAttribute("city", city);
        model.addAttribute("region", region);

        // 查询并设置房源发布人信息
        ServiceResult<UserDTO> userDTOServiceResult = userService.findById(houseDTO.getAdminId());
        model.addAttribute("agent", userDTOServiceResult.getResult());
        model.addAttribute("house", houseDTO);

        // 小区中出租数量，需要聚合查询
        ServiceResult<Long> countResult = searchService.aggregateDistrictHouse(city.getEnName(), region.getEnName(), houseDTO.getDistrict());
        model.addAttribute("houseCountInDistrict", countResult.getResult());

        return "house-detail";
    }

    /**
     * 
     * @param cityEnName 城市名简称
     * @param model 
     * @param session 会话对象
     * @param redirectAttributes 重定向属性存放
     * @return
     */
    @GetMapping("rent/house/map")
    public String rentMapPage(@RequestParam(value = "cityEnName") String cityEnName,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // 根据cityEnName查询城市
        ServiceResult<SupportAddressDTO> city = addressService.findCity(cityEnName);
        if (!city.isSuccess()) {
            redirectAttributes.addAttribute("msg", "must_chose_city");
            return "redirect:/index";
        } else {
            session.setAttribute("cityName", cityEnName);
            model.addAttribute("city", city.getResult());
        }

        // 获取支持的区域列表
        ServiceMultiResult<SupportAddressDTO> regions = addressService.findAllRegionsByCityName(cityEnName);


        ServiceMultiResult<HouseBucketDTO> serviceResult = searchService.mapAggregate(cityEnName);

        model.addAttribute("aggData", serviceResult.getResult());
        model.addAttribute("total", serviceResult.getTotal());
        model.addAttribute("regions", regions.getResult());
        
        return "rent-map";
    }

    /**
     * 在地图上根据城市搜索房源列表
     * @param mapSearch
     * @return
     */
    @GetMapping("rent/house/map/houses")
    @ResponseBody
    public ApiResponse<List<HouseDTO>> rentMapHouses(@ModelAttribute MapSearch mapSearch) {
        if (mapSearch.getCityEnName() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须选择城市");
        }
        ServiceMultiResult<HouseDTO> serviceMultiResult;
        if (mapSearch.getLevel() < 13) {
            serviceMultiResult = houseService.wholeMapQuery(mapSearch);
        } else {
            // 小地图查询必须要传递地图边界参数
            serviceMultiResult = houseService.boundMapQuery(mapSearch);
        }

        ApiResponse<List<HouseDTO>> response = ApiResponse.ofSuccess(serviceMultiResult.getResult());
        response.setMore(serviceMultiResult.getTotal() > (mapSearch.getStart() + mapSearch.getSize()));
        return response;

    }

}
