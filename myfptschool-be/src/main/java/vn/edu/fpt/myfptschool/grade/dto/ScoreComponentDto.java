package vn.edu.fpt.myfptschool.grade.dto;

import vn.edu.fpt.myfptschool.grade.entity.ScoreComponent;

public record ScoreComponentDto(Short id, String code, String name, Short weight) {
    public static ScoreComponentDto from(ScoreComponent c) {
        return new ScoreComponentDto(c.getId(), c.getCode(), c.getName(), c.getWeight());
    }
}
