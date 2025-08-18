package com.example.jpopranker.service;

import com.example.jpopranker.entity.Song;
import com.example.jpopranker.repository.SongRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository songRepository;

    // 모든 곡 조회
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    // 차트별 곡 조회
    public  List<Song> getSongsByChart (String chartName) {
        return songRepository.findByChartNameOrderByRankingAsc(chartName);
    }

    // 곡 저장
    public Song saveSong (String title, String artist, Integer ranking, String chartName) {
        Song song = Song.builder()
            .title(title)
            .artist(artist)
            .ranking(ranking)
            .chartName(chartName)
            .chartDate(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

        return songRepository.save(song);
    }

}
