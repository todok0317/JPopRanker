package com.example.jpopranker.song.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SongRequestDto {

    private String title;
    private String artist;
    private Integer ranking;
    private String chartName;

}
