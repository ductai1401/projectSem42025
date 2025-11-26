package projectSem4.com.service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectSem4.com.model.entities.SearchHistory;
import projectSem4.com.model.repositories.SearchHistoryRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchKeyService {

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    /* ================= LẤY DỮ LIỆU ================= */

    // Lấy toàn bộ lịch sử tìm kiếm, mới nhất trước
    public List<SearchHistory> getAllHistory() {
        List<SearchHistory> all = searchHistoryRepository.findAll();
        return all.stream()
                .sorted(Comparator.comparing(SearchHistory::getCreateAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    // Lấy theo từ khóa
    public List<SearchHistory> findByKeyword(String keyword) {
        return searchHistoryRepository.findByKeyword(keyword);
    }

    // Lấy top keyword tìm kiếm nhiều nhất
    public List<Object[]> getTopKeywords(int limit) {
        return searchHistoryRepository.findTopKeywords(limit);
    }

    /* ================= LƯU / CẬP NHẬT ================= */

    // Lưu một record tìm kiếm
    public Integer saveSearch(SearchHistory sh) {
        if (sh == null || sh.getKeyWord() == null || sh.getKeyWord().isBlank()) {
            return null;
        }
        sh.setCreateAt(java.time.LocalDateTime.now());
        return searchHistoryRepository.create(sh);
    }

    // Tăng search count cho keyword (nếu có thì +1, nếu chưa có thì tạo mới)
    public void increaseSearchCount(String keyword) {
        if (keyword == null || keyword.isBlank()) return;

        // tìm xem có keyword này chưa
        List<SearchHistory> found = searchHistoryRepository.findByKeyword(keyword);
        Optional<SearchHistory> latest = found.stream()
                .filter(Objects::nonNull)
                .max(Comparator.comparing(SearchHistory::getCreateAt));

        if (latest.isPresent()) {
            SearchHistory sh = latest.get();
            sh.setSearchCount(sh.getSearchCount() == null ? 1 : sh.getSearchCount() + 1);
            searchHistoryRepository.update(sh);
        } else {
            SearchHistory sh = new SearchHistory();
            sh.setKeyWord(keyword);
            sh.setSearchCount(1);
            saveSearch(sh);
        }
    }

    /* ================= XÓA ================= */

    public int deleteById(Long id) {
        return searchHistoryRepository.deleteById(id);
    }

    public int deleteAll() {
        return searchHistoryRepository.deleteAll();
    }
}
