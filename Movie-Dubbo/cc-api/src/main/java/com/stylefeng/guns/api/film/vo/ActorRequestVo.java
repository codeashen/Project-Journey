package com.stylefeng.guns.api.film.vo;

import lombok.Data;

import java.util.List;

@Data
public class ActorRequestVo {
    private ActorVo director;
    private List<ActorVo> actors;
}
