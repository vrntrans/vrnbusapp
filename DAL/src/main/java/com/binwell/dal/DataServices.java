package com.binwell.dal;

import com.binwell.dal.api.BusStationApi;
import com.binwell.dal.api.NewRouteApi;
import com.binwell.dal.api.ObjectApi;
import com.binwell.dal.api.RouteApi;

public class DataServices {

    public static BusStationApi BusStationService;
    public static NewRouteApi NewRouteService;
    public static ObjectApi ObjectService;
    public static RouteApi RouteService;

    public static void init() {
        ApiClient client = new ApiClient();
        client.createDefaultAdapter();
        BusStationService = client.createService(BusStationApi.class);
        NewRouteService = client.createService(NewRouteApi.class);
        ObjectService = client.createService(ObjectApi.class);
        RouteService = client.createService(RouteApi.class);
    }
}
