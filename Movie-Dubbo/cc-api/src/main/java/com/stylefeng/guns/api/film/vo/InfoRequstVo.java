package com.stylefeng.guns.api.film.vo;

import lombok.Data;

@Data
public class InfoRequstVo {
    private String biography;
    private ActorRequestVo actors;
    private ImgVo imgVo;
    private String filmId;
}
