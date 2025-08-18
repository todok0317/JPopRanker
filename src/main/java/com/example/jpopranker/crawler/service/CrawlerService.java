package com.example.jpopranker.crawler.service;

import com.example.jpopranker.song.dto.request.SongRequestDto;
import com.example.jpopranker.song.service.SongService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

            // HTML 파싱
            Document doc = Jsoup.parse(testHtml);

            // 곡 정보 추출
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

            // 차트 데이터가 있는 행들 선택 (헤더 제외)
            Elements rows = doc.select("table tr");

            List<SongRequestDto> songs = new ArrayList<>();

            for (int i = 1; i < Math.min(rows.size(), 21); i++) { // 1-20위만
                Element row = rows.get(i);

                try {
                    // 순위 추출
                    Element rankSpan = row.select("td.rank_td span").first();
                    if (rankSpan == null) continue;

                    Integer ranking = Integer.parseInt(rankSpan.text().trim());

                    // 곡제목 추출
                    Element titleElement = row.select("p.musuc_title").first();
                    if (titleElement == null) continue;

                    String title = titleElement.text().trim();

                    // 아티스트 추출
                    Element artistElement = row.select("p.artist_name a").first();
                    if (artistElement == null) continue;

                    String artist = artistElement.text().trim();

                    if (!title.isEmpty() && !artist.isEmpty()) {
                        SongRequestDto song = new SongRequestDto(title, artist, ranking, "billboard-japan");
                        songs.add(song);

                        log.info("Billboard Japan {}위 - {} by {}", ranking, title, artist);
                    }

                } catch (Exception e) {
                    log.warn("{}번째 행 파싱 실패: {}", i, e.getMessage());
                }
            }

            // 데이터베이스에 저장
            for (SongRequestDto song : songs) {
                songService.saveSong(song);
            }

            log.info("Billboard Japan 크롤링 완료! {}곡 저장됨", songs.size());

        } catch (Exception e) {
            log.error("Billboard Japan 크롤링 중 오류 발생", e);
        }
    }


}
