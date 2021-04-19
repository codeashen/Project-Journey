package com.ashen.ccfilm.hall.feign;

import com.ashen.ccfilm.film.facade.FilmFacade;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "film-service")
public interface FilmFeignClient extends FilmFacade {
    
}
