package com.example.jpopranker.song.dto.response;

import com.example.jpopranker.song.entity.Song;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongResponseDto {

    private Long id;

    private String title;

    private String artist;

    private Integer ranking;

    private String chartName;

    private LocalDateTime chartDate;

    // Entity -> DTO 변환 메서드
    public static SongResponseDto from(Song song) {
        return SongResponseDto.builder()
            .id(song.getId())
            .title(song.getTitle())
            .artist(song.getArtist())
            .ranking(song.getRanking())
            .chartName(song.getChartName())
            .chartDate(song.getChartDate())
            .build();
    }
}
