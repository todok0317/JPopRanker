package com.example.jpopranker.song.repository;

import com.example.jpopranker.song.entity.Song;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // 차트별로 조회
    List<Song> findByChartName(String chartName);

    // 아티스트별로 조회
    List<Song> findByArtist(String artist);

    // 랭킹 순서로 조회
    List<Song> findByChartNameOrderByRankingAsc(String chartName);
}
