package plus.ruoyi.common.http;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.CoordinateUtil;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.ruoyi.common.http.client.gaode.map.GaodeMapClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import plus.ruoyi.common.http.client.gaode.map.response.*;

@SpringBootTest
@DisplayName("高德地图API测试")
public class GaodeMapTest {

    /**
     * 实例化GaodeMapClient
     */
    @Autowired
    private  GaodeMapClient gaodeMapClient;

    @DisplayName("根据IP获取位置信息")
    @Test
    public void getLocationByIp() {
        IPLocationResponse response = gaodeMapClient.getLocationByIp("113.88.65.10", "4").getResult();
        Console.log("IP位置信息:", response);
    }

    @DisplayName("根据IP获取详细地址")
    @Test
    public void getAddressByIp() {
        IPLocationResponse ipResponse = gaodeMapClient.getLocationByIp("112.74.85.1", "4").getResult();
        if (ObjectUtil.isNotNull(ipResponse) && ObjectUtil.isNotNull(ipResponse.getLocation())) {
            String[] location = ipResponse.getLocation().split(",");
            if (location.length == 2) {
                CoordinateUtil.Coordinate coord = new CoordinateUtil.Coordinate(
                    Double.parseDouble(location[0]),
                    Double.parseDouble(location[1])
                );
                ReverseGeocodingResponse regeoResponse = gaodeMapClient.reverseGeocoding(coord).getResult();
                Console.log("IP对应详细地址:", regeoResponse != null ? regeoResponse.getRegeocode().getFormattedAddress() : "未获取到地址");
            }
        }
    }

    @DisplayName("地理编码 - 根据地址获取坐标")
    @Test
    public void geocoding() {
        GeocodingResponse response = gaodeMapClient.geocoding("东莞市南城天安数码城").getResult();
        Console.log("地理编码结果:", response);
    }

    @DisplayName("根据地址获取经纬度坐标")
    @Test
    public void getCoordinateByAddress() {
        GeocodingResponse response = gaodeMapClient.geocoding("东莞市南城天安数码城").getResult();
        if (ObjectUtil.isNotNull(response) && CollUtil.isNotEmpty(response.getGeocodes())) {
            String location = response.getGeocodes().get(0).getLocation();
            String[] split = location.split(",");
            if (split.length == 2) {
                CoordinateUtil.Coordinate coordinate = new CoordinateUtil.Coordinate(
                    Double.parseDouble(split[0]),
                    Double.parseDouble(split[1])
                );
                Console.log("地址对应坐标:", coordinate);
            }
        }
    }

    @DisplayName("逆地理编码 - 根据坐标获取地址")
    @Test
    public void reverseGeocoding() {
        CoordinateUtil.Coordinate coordinate = new CoordinateUtil.Coordinate(113.705015, 22.989603);
        ReverseGeocodingResponse response = gaodeMapClient.reverseGeocoding(coordinate).getResult();
        Console.log("逆地理编码结果:", response);
    }

    @DisplayName("根据坐标获取详细地址")
    @Test
    public void getAddressByCoordinate() {
        CoordinateUtil.Coordinate coordinate = new CoordinateUtil.Coordinate(114.21, 37.32);
        ReverseGeocodingResponse response = gaodeMapClient.reverseGeocoding(coordinate).getResult();
        String address = null;
        if (ObjectUtil.isNotNull(response) && ObjectUtil.isNotNull(response.getRegeocode())) {
            address = response.getRegeocode().getFormattedAddress();
        }
        Console.log("坐标对应地址:", address);
    }

    @DisplayName("根据坐标获取行政区划代码")
    @Test
    public void getAdcodeByCoordinate() {
        CoordinateUtil.Coordinate coordinate = new CoordinateUtil.Coordinate(114.21, 37.32);
        ReverseGeocodingResponse response = gaodeMapClient.reverseGeocoding(coordinate).getResult();
        String adcode = null;
        if (ObjectUtil.isNotNull(response) &&
            ObjectUtil.isNotNull(response.getRegeocode()) &&
            ObjectUtil.isNotNull(response.getRegeocode().getAddressComponent())) {
            adcode = response.getRegeocode().getAddressComponent().getAdcode();
        }
        Console.log("坐标对应行政区划代码:", adcode);
    }

    @DisplayName("根据地址获取行政区划代码")
    @Test
    public void getAdcodeByAddress() {
        GeocodingResponse response = gaodeMapClient.geocoding("东莞市南城天安数码城").getResult();
        String adcode = null;
        if (ObjectUtil.isNotNull(response) && CollUtil.isNotEmpty(response.getGeocodes())) {
            adcode = response.getGeocodes().get(0).getAdcode();
        }
        Console.log("地址对应行政区划代码:", adcode);
    }

    @DisplayName("根据地址获取天气信息")
    @Test
    public void getWeatherByAddress() {
        // 1. 先获取地址的adcode
        GeocodingResponse geoResponse = gaodeMapClient.geocoding("东莞市南城天安数码城").getResult();
        WeatherResponse.WeatherData weatherData = null;

        if (ObjectUtil.isNotNull(geoResponse) && CollUtil.isNotEmpty(geoResponse.getGeocodes())) {
            String adcode = geoResponse.getGeocodes().get(0).getAdcode();

            // 2. 根据adcode获取天气
            WeatherResponse weatherResponse = gaodeMapClient.getWeatherByAdcode(adcode).getResult();
            if (ObjectUtil.isNotNull(weatherResponse) && CollUtil.isNotEmpty(weatherResponse.getLives())) {
                weatherData = weatherResponse.getLives().get(0);
            }
        }
        Console.log("地址对应天气信息:", weatherData);
    }

    @DisplayName("根据坐标获取天气信息")
    @Test
    public void getWeatherByCoordinate() {
        CoordinateUtil.Coordinate coordinate = new CoordinateUtil.Coordinate(114.21, 37.32);
        WeatherResponse.WeatherData weatherData = null;

        // 1. 先通过逆地理编码获取adcode
        ReverseGeocodingResponse regeoResponse = gaodeMapClient.reverseGeocoding(coordinate).getResult();
        if (ObjectUtil.isNotNull(regeoResponse) &&
            ObjectUtil.isNotNull(regeoResponse.getRegeocode()) &&
            ObjectUtil.isNotNull(regeoResponse.getRegeocode().getAddressComponent())) {

            String adcode = regeoResponse.getRegeocode().getAddressComponent().getAdcode();

            // 2. 根据adcode获取天气
            WeatherResponse weatherResponse = gaodeMapClient.getWeatherByAdcode(adcode).getResult();
            if (ObjectUtil.isNotNull(weatherResponse) && CollUtil.isNotEmpty(weatherResponse.getLives())) {
                weatherData = weatherResponse.getLives().get(0);
            }
        }
        Console.log("坐标对应天气信息:", weatherData);
    }

    @DisplayName("计算两点间距离")
    @Test
    public void calculateDistance() {
        CoordinateUtil.Coordinate start = new CoordinateUtil.Coordinate(113.746262, 23.046237);
        CoordinateUtil.Coordinate end = new CoordinateUtil.Coordinate(113.716262, 21.046237);

        DistanceResponse response = gaodeMapClient.calculateDistance(start, end).getResult();
        Long distance = null;
        if (ObjectUtil.isNotNull(response) && CollUtil.isNotEmpty(response.getResults())) {
            distance = Long.valueOf(response.getResults().get(0).getDistance());
        }
        Console.log("两点间距离(米):", distance);
    }

    @DisplayName("综合测试 - 获取地址完整信息")
    @Test
    public void getCompleteLocationInfo() {
        String address = "东莞市南城天安数码城";
        Console.log("===== 地址: " + address + " 的完整信息 =====");

        // 1. 地理编码
        GeocodingResponse geoResponse = gaodeMapClient.geocoding(address).getResult();
        if (ObjectUtil.isNotNull(geoResponse) && CollUtil.isNotEmpty(geoResponse.getGeocodes())) {
            GeocodingResponse.GeocodingData geocode = geoResponse.getGeocodes().get(0);
            Console.log("坐标:", geocode.getLocation());
            Console.log("行政区划代码:", geocode.getAdcode());
            Console.log("格式化地址:", geocode.getFormattedAddress());

            // 2. 获取天气
            WeatherResponse weatherResponse = gaodeMapClient.getWeatherByAdcode(geocode.getAdcode()).getResult();
            if (ObjectUtil.isNotNull(weatherResponse) && CollUtil.isNotEmpty(weatherResponse.getLives())) {
                WeatherResponse.WeatherData weather = weatherResponse.getLives().get(0);
                Console.log("天气:", weather.getWeather());
                Console.log("温度:", weather.getTemperature() + "°C");
                Console.log("湿度:", weather.getHumidity() + "%");
            }
        }
    }
}
