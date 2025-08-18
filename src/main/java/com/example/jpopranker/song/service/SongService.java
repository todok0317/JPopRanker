package com.example.jpopranker.song.service;

import com.example.jpopranker.song.dto.request.SongRequestDto;
import com.example.jpopranker.song.dto.response.SongResponseDto;
import com.example.jpopranker.song.entity.Song;
import com.example.jpopranker.song.repository.SongRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    // 모든 곡 조회
    public List<SongResponseDto> getAllSongs() {
        return songRepository.findAll().stream()
            .map(SongResponseDto::from)
            .toList();
    }

    // 차트별 곡 조회
    public  List<SongResponseDto> getSongsByChart (String chartName) {
        return songRepository.findByChartNameOrderByRankingAsc(chartName).stream()
            .map(SongResponseDto::from).toList();
    }

    // 곡 저장
    public SongResponseDto saveSong (SongRequestDto requestDto) {
        Song song = Song.builder()
            .title(requestDto.getTitle())
            .artist(requestDto.getArtist())
            .ranking(requestDto.getRanking())
            .chartName(requestDto.getChartName())
            .chartDate(LocalDateTime.now())
            .build();

        Song savedSong =  songRepository.save(song);
        return SongResponseDto.from(savedSong);
    }

}
