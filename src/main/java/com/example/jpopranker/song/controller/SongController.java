package com.example.jpopranker.song.controller;

import com.example.jpopranker.song.dto.request.SongRequestDto;
import com.example.jpopranker.song.dto.response.SongResponseDto;
import com.example.jpopranker.song.entity.Song;
import com.example.jpopranker.song.service.SongService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    // 모든 곡 조회
    @GetMapping
    public List<SongResponseDto> getAllSongs() {
        return songService.getAllSongs();
    }

    // 차트별 곡 조회
    @GetMapping("/chart/{chartName}")
    public List<SongResponseDto> getSongsByChart(@PathVariable String chartName) {
        return songService.getSongsByChart(chartName);
    }

    // 테스트 용 곡 추가
    @PostMapping("/test")
    public SongResponseDto addTestSong() {
        SongRequestDto testRequest = new SongRequestDto("테스트 노래", "테스트 가수", 1, "테스트");
        return songService.saveSong(testRequest);
    }

    // 새로운 곡 추가
    @PostMapping
    public SongResponseDto addSong(@RequestBody SongRequestDto request) {
        return songService.saveSong(request);
    }

    // 잘못된 데이터 삭제 (아티스트가 "Unknown"인 데이터)
    @DeleteMapping("/cleanup/unknown")
    public String cleanupUnknownArtists() {
        int deletedCount = songService.cleanupUnknownArtists();
        return deletedCount + "개의 잘못된 데이터가 삭제되었습니다.";
    }

    // 특정 차트의 모든 데이터 삭제
    @DeleteMapping("/cleanup/chart/{chartName}")
    public String cleanupByChart(@PathVariable String chartName) {
        int deletedCount = songService.cleanupByChart(chartName);
        return chartName + " 차트의 " + deletedCount + "개 데이터가 삭제되었습니다.";
    }

    // 전체 데이터 삭제 (주의!)
    @DeleteMapping("/cleanup/all")
    public String cleanupAll() {
        long deletedCount = songService.cleanupAll();
        return "전체 " + deletedCount + "개 데이터가 삭제되었습니다.";
    }

    // 중복 정리
    @DeleteMapping("/cleanup/duplicates")
    public String cleanupDuplicates() {
        int deletedCount = songService.cleanupDuplicates();
        return deletedCount + "개의 중복 데이터가 정리되었습니다.";
    }
}
