package plus.ruoyi.common.http.client.gaode.map;

import cn.hutool.core.util.CoordinateUtil;
import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;
import plus.ruoyi.common.http.client.gaode.map.response.*;

/**
 * 高德地图API客户端
 *
 * @author bkywksj
 */
@BaseRequest(baseURL = "https://restapi.amap.com", interceptor = GaodeMapInterceptor.class)
public interface GaodeMapClient {

    /**
     * 根据IP获取位置信息
     *
     * @param ip   IP地址
     * @param type 4代表IPv4
     * @return IP位置信息
     */
    @Get(url = "/v3/ip")
    ForestResponse<IPLocationResponse> getLocationByIp(@Query("ip") String ip, @Query("type") String type);

    /**
     * 地理编码 - 根据地址获取经纬度信息
     *
     * @param address 地址
     * @return 地理编码响应
     */
    @Get(url = "/v3/geocode/geo?output=json")
    ForestResponse<GeocodingResponse> geocoding(@Query("address") String address);

    /**
     * 逆地理编码 - 根据经纬度获取位置信息
     *
     * @param coord 经纬度坐标
     * @return 逆地理编码响应
     */
    @Get(url = "/v3/geocode/regeo?output=json&location=${coord.lng},${coord.lat}")
    ForestResponse<ReverseGeocodingResponse> reverseGeocoding(@Var("coord") CoordinateUtil.Coordinate coord);

    /**
     * 根据行政区划代码获取天气信息
     *
     * @param adcode 行政区划代码
     * @return 天气响应
     */
    @Get(url = "/v3/weather/weatherInfo")
    ForestResponse<WeatherResponse> getWeatherByAdcode(@Query("city") String adcode);

    /**
     * 计算两点间距离
     *
     * @param start 起点坐标
     * @param end   终点坐标
     * @return 距离响应
     */
    @Get(url = "/v3/distance?type=0&origins=${origins.lng},${origins.lat}&destination=${destination.lng},${destination.lat}")
    ForestResponse<DistanceResponse> calculateDistance(@Var("origins") CoordinateUtil.Coordinate start, @Query("destination") CoordinateUtil.Coordinate end);
}
