package com.example.jpopranker.song.controller;

import com.example.jpopranker.song.dto.request.SongRequestDto;
import com.example.jpopranker.song.dto.response.SongResponseDto;
import com.example.jpopranker.song.entity.Song;
import com.example.jpopranker.song.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Songs", description = "음악 차트 데이터 관리 API")
public class SongController {

    private final SongService songService;

    @Operation(summary = "전체 곡 목록 조회", description = "데이터베이스의 모든 곡을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public List<SongResponseDto> getAllSongs() {
        return songService.getAllSongs();
    }

    @Operation(summary = "차트별 곡 목록 조회", description = "특정 차트의 곡들을 랭킹 순으로 조회합니다.")
    @Parameter(name = "chartName", description = "차트 이름 (예: billboard-japan, oricon)", example = "billboard-japan")
    @GetMapping("/chart/{chartName}")
    public List<SongResponseDto> getSongsByChart(@PathVariable String chartName) {
        return songService.getSongsByChart(chartName);
    }

    @Operation(summary = "테스트 곡 추가", description = "테스트용 샘플 데이터를 추가합니다.")
    @PostMapping("/test")
    public SongResponseDto addTestSong() {
        SongRequestDto testRequest = new SongRequestDto("테스트 노래", "테스트 가수", 1, "테스트");
        return songService.saveSong(testRequest);
    }

    @Operation(summary = "새 곡 추가", description = "새로운 곡을 데이터베이스에 추가합니다.")
    @PostMapping
    public SongResponseDto addSong(@RequestBody SongRequestDto request) {
        return songService.saveSong(request);
    }

    @Operation(summary = "Unknown 아티스트 데이터 삭제", description = "아티스트명이 'Unknown'인 잘못된 데이터를 삭제합니다.")
    @DeleteMapping("/cleanup/unknown")
    public String cleanupUnknownArtists() {
        int deletedCount = songService.cleanupUnknownArtists();
        return deletedCount + "개의 잘못된 데이터가 삭제되었습니다.";
    }

    @Operation(summary = "특정 차트 데이터 삭제", description = "지정된 차트의 모든 데이터를 삭제합니다.")
    @Parameter(name = "chartName", description = "삭제할 차트 이름", example = "billboard-japan")
    @DeleteMapping("/cleanup/chart/{chartName}")
    public String cleanupByChart(@PathVariable String chartName) {
        int deletedCount = songService.cleanupByChart(chartName);
        return chartName + " 차트의 " + deletedCount + "개 데이터가 삭제되었습니다.";
    }

    @Operation(summary = "전체 데이터 삭제", description = "⚠️ 주의: 모든 곡 데이터를 삭제합니다.")
    @DeleteMapping("/cleanup/all")
    public String cleanupAll() {
        long deletedCount = songService.cleanupAll();
        return "전체 " + deletedCount + "개 데이터가 삭제되었습니다.";
    }

    @Operation(summary = "중복 데이터 정리", description = "같은 곡의 중복 데이터를 정리하고 최신 데이터만 남깁니다.")
    @DeleteMapping("/cleanup/duplicates")
    public String cleanupDuplicates() {
        int deletedCount = songService.cleanupDuplicates();
        return deletedCount + "개의 중복 데이터가 정리되었습니다.";
    }
}
