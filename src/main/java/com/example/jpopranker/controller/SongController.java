package com.example.jpopranker.controller;

import com.example.jpopranker.entity.Song;
import com.example.jpopranker.service.SongService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    // 모든 곡 조회
    @GetMapping
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    // 차트별 곡 조회
    @GetMapping("/chart/{chartName}")
    public List<Song> getSongsByChart (@PathVariable String chartName) {
        return songService.getSongsByChart(chartName);
    }

    // 테스트 용 곡 추가
    @PostMapping("/test")
    public Song addTestSong() {
        return songService.saveSong("테스트 노래", "테스트 가수", 1, "테스트");
    }

}
