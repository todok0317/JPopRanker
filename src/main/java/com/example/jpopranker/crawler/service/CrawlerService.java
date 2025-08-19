package com.example.jpopranker.crawler.service;

import com.example.jpopranker.song.dto.request.SongRequestDto;
import com.example.jpopranker.song.service.SongService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerService {

    private final SongService songService;
    private final WebClient webClient;

    public void crawlTestData() {
        try {
            // 테스트용 HTML (실제 사이트 대신)
            String testHtml = """
                <html>
                <body>
                    <div class="chart">
                        <div class="song-item">
                            <span class="rank">1</span>
                            <span class="title">Pretender</span>
                            <span class="artist">Official髭男dism</span>
                        </div>
                        <div class="song-item">
                            <span class="rank">2</span>
                            <span class="title">Marigold</span>
                            <span class="artist">あいみょん</span>
                        </div>
                        <div class="song-item">
                            <span class="rank">3</span>
                            <span class="title">猫</span>
                            <span class="artist">DISH//</span>
                        </div>
                    </div>
                </body>
                </html>
                """;

            Document doc = Jsoup.parse(testHtml);
            Elements songItems = doc.select(".song-item");
            List<SongRequestDto> songs = new ArrayList<>();

            for (Element item : songItems) {
                String rankText = item.select(".rank").text();
                String title = item.select(".title").text();
                String artist = item.select(".artist").text();

                Integer ranking = Integer.parseInt(rankText);
                SongRequestDto song = new SongRequestDto(title, artist, ranking, "test-chart");
                songs.add(song);

                log.info("크롤링한 곡 : {}위 - {} by {}", ranking, title, artist);
            }
            
            for (SongRequestDto song : songs) {
                songService.saveSong(song);
            }
            log.info("크롤링 완료! {}곡 저장됨", songs.size());
        } catch (Exception e) {
            log.error("크롤링 중 오류 발생", e);
        }
    }

    public void crawlBillboardJapan() {
        try {
            log.info("Billboard Japan 크롤링 시작");
            
            // 🔥 자동 데이터 정리: 크롤링 전에 기존 데이터 삭제
            log.info("기존 Billboard Japan 데이터 정리 중...");
            int deletedCount = songService.cleanupByChart("billboard-japan");
            log.info("기존 데이터 {}개 정리 완료", deletedCount);
            
            String url = "https://www.billboard-japan.com/charts/detail?a=hot100";

            String html = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (html == null) {
                log.error("HTML을 가져올 수 없습니다.");
                return;
            }

            Document doc = Jsoup.parse(html);
            log.info("HTML 파싱 완료, 데이터 추출 시작");

            // Billboard Japan의 실제 구조에 맞게 수정
            // 각 순위의 tr 요소를 선택 (rank1, rank2, ... 클래스를 가진 tr들)
            Elements rankRows = doc.select("tr[class*=rank]");
            List<SongRequestDto> songs = new ArrayList<>();

            for (Element row : rankRows) {
                try {
                    // 순위 추출 - rank_td 내의 span에서
                    Element rankCell = row.select("td.rank_td span").first();
                    if (rankCell == null) continue;
                    
                    String rankText = rankCell.text().trim();
                    if (!rankText.matches("^\\d+$")) continue;
                    
                    int ranking = Integer.parseInt(rankText);
                    if (ranking > 50) break; // 50위까지만
                    
                    // 곡 제목 추출 - musuc_title 클래스에서 (사이트의 오타 그대로)
                    Element titleElement = row.select("p.musuc_title").first();
                    if (titleElement == null) continue;
                    
                    String title = titleElement.text().trim();
                    
                    // 아티스트 추출 - artist_name 클래스에서
                    Element artistElement = row.select("p.artist_name").first();
                    if (artistElement == null) continue;
                    
                    String artist = artistElement.text().trim();
                    
                    // 데이터 정리
                    title = title.replaceAll("^\\d+\\s*", "").trim(); // 앞의 숫자 제거
                    artist = artist.replaceAll("^\\d+\\s*", "").trim(); // 앞의 숫자 제거
                    
                    // 유효성 검사
                    if (title.length() > 1 && artist.length() > 1 && 
                        !artist.equals("Unknown") && !title.equals(artist)) {
                        
                        SongRequestDto song = new SongRequestDto(title, artist, ranking, "billboard-japan");
                        songs.add(song);

                        log.info("Billboard Japan {}위 - \"{}\" by \"{}\"", ranking, title, artist);
                    } else {
                        log.warn("데이터 누락 - 순위: {}, 제목: \"{}\", 아티스트: \"{}\"", ranking, title, artist);
                    }

                } catch (Exception e) {
                    log.warn("행 파싱 실패: {}", e.getMessage());
                }
            }

            // 혹시 위의 방법이 안 되면 백업 방법 시도
            if (songs.isEmpty()) {
                log.info("첫 번째 방법 실패, 백업 방법 시도...");
                
                // tbody 내의 모든 tr 중에서 rank 클래스를 가진 것들
                Elements allRows = doc.select("tbody tr");
                
                for (Element row : allRows) {
                    try {
                        // 클래스명에 rank가 포함된 행만 처리
                        String className = row.className();
                        if (!className.startsWith("rank")) continue;
                        
                        // 순위 정보 추출
                        Elements rankSpans = row.select("span");
                        if (rankSpans.isEmpty()) continue;
                        
                        String rankText = rankSpans.first().text().trim();
                        if (!rankText.matches("^\\d+$")) continue;
                        
                        int ranking = Integer.parseInt(rankText);
                        if (ranking > 20) break;
                        
                        // 곡 제목과 아티스트 추출
                        Elements titleElements = row.select("p.musuc_title");
                        Elements artistElements = row.select("p.artist_name");
                        
                        if (titleElements.isEmpty() || artistElements.isEmpty()) continue;
                        
                        String title = titleElements.text().trim();
                        String artist = artistElements.text().trim();
                        
                        if (title.length() > 1 && artist.length() > 1) {
                            SongRequestDto song = new SongRequestDto(title, artist, ranking, "billboard-japan");
                            songs.add(song);
                            log.info("Billboard Japan {}위 - \"{}\" by \"{}\" (백업방법)", ranking, title, artist);
                        }
                        
                    } catch (Exception e) {
                        log.warn("백업 방법 파싱 실패: {}", e.getMessage());
                    }
                }
            }

            // 중복 제거
            Map<Integer, SongRequestDto> uniqueSongs = new HashMap<>();
            for (SongRequestDto song : songs) {
                uniqueSongs.put(song.getRanking(), song);
            }

            // 데이터베이스에 저장
            for (SongRequestDto song : uniqueSongs.values()) {
                songService.saveSong(song);
            }

            log.info("Billboard Japan 크롤링 완료! {}곡 저장됨", uniqueSongs.size());
            
            // 🔥 자동 데이터 정리 (크롤링 후)
            songService.autoCleanupAfterCrawling();

        } catch (Exception e) {
            log.error("Billboard Japan 크롤링 중 오류 발생", e);
        }
    }

    public void crawlOriconChart() {
        try {
            log.info("Oricon 크롤링 시작");
            
            // 🔥 자동 데이터 정리: 크롤링 전에 기존 데이터 삭제
            log.info("기존 Oricon 데이터 정리 중...");
            int deletedCount = songService.cleanupByChart("oricon");
            log.info("기존 데이터 {}개 정리 완료", deletedCount);
            
            String url = "https://www.oricon.co.jp/music/rankinglab/cos/2025-08-18/";

            String html = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (html == null) {
                log.error("Oricon HTML을 가져올 수 없습니다.");
                return;
            }

            Document doc = Jsoup.parse(html);
            Elements rankingItems = doc.select(".media-box");
            List<SongRequestDto> songs = new ArrayList<>();

            for (Element item : rankingItems) {
                try {
                    // 순위 추출
                    Element rankElement = item.select("p.media-rank").first();
                    if (rankElement == null) continue;

                    Integer ranking = Integer.parseInt(rankElement.text().trim());

                    // 제목 추출
                    Element titleElement = item.select("li.media-title").first();
                    if (titleElement == null) continue;

                    String title = titleElement.text().trim();

                    // 아티스트 추출
                    Element artistElement = item.select("li.media-artist").first();
                    if (artistElement == null) continue;

                    String artist = artistElement.text().trim();

                    if (!title.isEmpty() && !artist.isEmpty() && ranking <= 50) { // 1-50위만
                        SongRequestDto song = new SongRequestDto(title, artist, ranking, "oricon");
                        songs.add(song);

                        log.info("Oricon {}위 - {} by {}", ranking, title, artist);
                    }

                } catch (Exception e) {
                    log.warn("Oricon 데이터 파싱 실패: {}", e.getMessage());
                }
            }

            // 데이터베이스에 저장
            for (SongRequestDto song : songs) {
                songService.saveSong(song);
            }

            log.info("Oricon 크롤링 완료! {}곡 저장됨", songs.size());
            
            // 🔥 자동 데이터 정리 (크롤링 후)
            songService.autoCleanupAfterCrawling();

        } catch (Exception e) {
            log.error("Oricon 크롤링 중 오류 발생", e);
        }
    }

    // 매일 오전 9시에 Billboard Japan 크롤링
    @Scheduled(cron = "0 0 9 * * *")
    public void scheduledBillboardJapanCrawling() {
        log.info("정기 Billboard Japan 크롤링 시작: {}", LocalDateTime.now());
        crawlBillboardJapan();
    }

    // 매일 오전 9시 30분에 Oricon 크롤링
    @Scheduled(cron = "0 30 9 * * *")
    public void scheduledOriconCrawling() {
        log.info("정기 Oricon 크롤링 시작: {}", LocalDateTime.now());
        crawlOriconChart();
    }

    // 매주 일요일 오전 10시에 중복 데이터 정리
    @Scheduled(cron = "0 0 10 * * SUN")
    public void scheduledDataCleanup() {
        log.info("정기 데이터 정리 시작: {}", LocalDateTime.now());
        songService.autoCleanupAfterCrawling(); // 🔥 통합 자동 정리 사용
    }
}
