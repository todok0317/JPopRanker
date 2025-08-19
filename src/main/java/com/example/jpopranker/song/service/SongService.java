package com.example.jpopranker.song.service;

import com.example.jpopranker.song.dto.request.SongRequestDto;
import com.example.jpopranker.song.dto.response.SongResponseDto;
import com.example.jpopranker.song.entity.Song;
import com.example.jpopranker.song.repository.SongRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    public List<SongResponseDto> getSongsByChart(String chartName) {
        return songRepository.findByChartNameOrderByRankingAsc(chartName).stream()
            .map(SongResponseDto::from).toList();
    }

    // 곡 저장
    public SongResponseDto saveSong(SongRequestDto requestDto) {
        // 같은 차트에서 같은 곡이 이미 있는지 확인
        Optional<Song> existingSong = songRepository.findByTitleAndArtistAndChartName(
            requestDto.getTitle(),
            requestDto.getArtist(),
            requestDto.getChartName()
        );

        if (existingSong.isPresent()) {
            // 기존 곡이 있으면 랭킹만 업데이트
            Song oldSong = existingSong.get();
            Song updatedSong = Song.builder()
                .id(oldSong.getId())
                .title(oldSong.getTitle())
                .artist(oldSong.getArtist())
                .ranking(requestDto.getRanking())
                .chartName(oldSong.getChartName())
                .chartDate(LocalDateTime.now())
                .createdAt(oldSong.getCreatedAt())
                .build();

            Song savedSong = songRepository.save(updatedSong);
            log.info("기존 곡 업데이트 : {} - {} ({}위)", requestDto.getArtist(), requestDto.getTitle(),
                requestDto.getRanking());
            return SongResponseDto.from(savedSong);
        } else {
            // 새 곡 저장
            Song song = Song.builder()
                .title(requestDto.getTitle())
                .artist(requestDto.getArtist())
                .ranking(requestDto.getRanking())
                .chartName(requestDto.getChartName())
                .chartDate(LocalDateTime.now())
                .build();

            Song savedSong = songRepository.save(song);
            log.info("새 곡 저장: {} - {} ({}위)", requestDto.getArtist(), requestDto.getTitle(),
                requestDto.getRanking());
            return SongResponseDto.from(savedSong);
        }
    }

    // 중복 데이터 정리 (같은 제목+아티스트+차트에서 최신 것만 남기기)
    @Transactional
    public int cleanupDuplicates() {
        List<Song> allSongs = songRepository.findAll();
        Map<String, List<Song>> groupedSongs = allSongs.stream()
            .collect(Collectors.groupingBy(song ->
                song.getTitle() + "|" + song.getArtist() + "|" + song.getChartName()));

        int deletedCount = 0;
        for (List<Song> duplicates : groupedSongs.values()) {
            if (duplicates.size() > 1) {
                // 가장 최신 데이터만 남기고 나머지 삭제
                duplicates.sort((a, b) -> b.getChartDate().compareTo(a.getChartDate()));
                List<Song> toDelete = duplicates.subList(1, duplicates.size());
                songRepository.deleteAll(toDelete);
                deletedCount += toDelete.size();

                log.info("중복 제거: {} - {} ({} 차트에서 {}개 삭제)",
                    duplicates.get(0).getTitle(),
                    duplicates.get(0).getArtist(),
                    duplicates.get(0).getChartName(),
                    toDelete.size());
            }
        }

        log.info("중복 데이터 {}개 정리 완료", deletedCount);
        return deletedCount;
    }

    // 같은 순위의 중복 데이터 정리 (특정 차트에서 같은 순위에 여러 곡이 있는 경우)
    @Transactional
    public int cleanupSameRankingDuplicates(String chartName) {
        List<Song> chartSongs = songRepository.findByChartName(chartName);
        Map<Integer, List<Song>> rankingGroups = chartSongs.stream()
            .collect(Collectors.groupingBy(Song::getRanking));

        int deletedCount = 0;
        for (Map.Entry<Integer, List<Song>> entry : rankingGroups.entrySet()) {
            List<Song> songsAtSameRank = entry.getValue();
            if (songsAtSameRank.size() > 1) {
                // 같은 순위에 여러 곡이 있으면 가장 최신 것만 남기고 삭제
                songsAtSameRank.sort((a, b) -> b.getChartDate().compareTo(a.getChartDate()));
                List<Song> toDelete = songsAtSameRank.subList(1, songsAtSameRank.size());
                songRepository.deleteAll(toDelete);
                deletedCount += toDelete.size();

                log.info("같은 순위({}) 중복 제거: {}개 삭제", entry.getKey(), toDelete.size());
            }
        }

        log.info("{} 차트에서 같은 순위 중복 {}개 정리 완료", chartName, deletedCount);
        return deletedCount;
    }

    // 크롤링 후 자동 데이터 정리 (중복, Unknown 등 모든 문제 해결)
    @Transactional
    public void autoCleanupAfterCrawling() {
        log.info("크롤링 후 자동 데이터 정리 시작");

        // 1. Unknown 아티스트 제거
        int unknownCount = cleanupUnknownArtists();

        // 2. 전체 중복 데이터 제거
        int duplicateCount = cleanupDuplicates();

        // 3. 각 차트별 같은 순위 중복 제거
        int billboardDuplicates = cleanupSameRankingDuplicates("billboard-japan");
        int oriconDuplicates = cleanupSameRankingDuplicates("oricon");

        // 4. 빈 제목/아티스트 제거
        int emptyCount = cleanupEmptyData();

        log.info("자동 정리 완료 - Unknown: {}, 중복: {}, 같은순위: {}, 빈데이터: {}",
            unknownCount, duplicateCount, billboardDuplicates + oriconDuplicates, emptyCount);
    }

    // 빈 제목이나 아티스트 데이터 정리
    @Transactional
    public int cleanupEmptyData() {
        List<Song> allSongs = songRepository.findAll();
        List<Song> toDelete = allSongs.stream()
            .filter(song ->
                song.getTitle() == null || song.getTitle().trim().isEmpty() ||
                song.getArtist() == null || song.getArtist().trim().isEmpty() ||
                song.getTitle().trim().length() <= 1 ||
                song.getArtist().trim().length() <= 1 ||
                song.getTitle().equals(song.getArtist())
            )
            .toList();

        songRepository.deleteAll(toDelete);
        log.info("빈 데이터 {}개 정리됨", toDelete.size());
        return toDelete.size();
    }

    // 잘못된 데이터 삭제 (아티스트가 "Unknown"인 데이터)
    public int cleanupUnknownArtists() {
        List<Song> unknownSongs = songRepository.findByArtist("Unknown");
        songRepository.deleteAll(unknownSongs);
        log.info("Unknown 아티스트 데이터 {}개 삭제됨", unknownSongs.size());
        return unknownSongs.size();
    }

    // 특정 차트의 모든 데이터 삭제
    public int cleanupByChart(String chartName) {
        List<Song> chartSongs = songRepository.findByChartName(chartName);
        songRepository.deleteAll(chartSongs);
        log.info("{} 차트 데이터 {}개 삭제됨", chartName, chartSongs.size());
        return chartSongs.size();
    }

    // 전체 데이터 삭제 (주의!)
    public long cleanupAll() {
        long count = songRepository.count();
        songRepository.deleteAll();
        log.info("전체 데이터 {}개 삭제됨", count);
        return count;
    }

}
