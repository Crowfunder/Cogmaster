package com.crowfunder.cogmaster.Routers;

import com.crowfunder.cogmaster.Configs.ConfigEntry;
import org.springframework.stereotype.Service;

@Service
public class RouterService {

    RouterRepository routerRepository;

    public Router getRouter(String implementation) {
        return routerRepository.getRouters().get(implementation);
    }

    public Router getRouter(ConfigEntry configEntry) {
        if (configEntry == null) {
            return null;
        }
        return getRouter(configEntry.getEffectiveImplementation());
    }

    public RouterService(RouterRepository routerRepository) {
        this.routerRepository = routerRepository;
    }
}
