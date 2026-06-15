package vn.edu.fpt.myfptschool.academic.dto;

import vn.edu.fpt.myfptschool.academic.entity.Campus;

public record CampusResponse(Long id, String name) {
    public static CampusResponse from(Campus c) {
        return new CampusResponse(c.getId(), c.getName());
    }
}
