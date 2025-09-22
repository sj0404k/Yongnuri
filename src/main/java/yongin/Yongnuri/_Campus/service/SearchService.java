package yongin.Yongnuri._Campus.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import yongin.Yongnuri._Campus.domain.Block;
import yongin.Yongnuri._Campus.domain.Search;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.BlocksRes;
import yongin.Yongnuri._Campus.dto.SearchReq;
import yongin.Yongnuri._Campus.dto.SearchRes;
import yongin.Yongnuri._Campus.repository.SearchRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;

    public boolean deleteAllHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        searchRepository.deleteByUserId(user.getId());

        return true;
    }

    public boolean deleteHistoryById(String email, Long searchId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "해당 사용자를 찾을 수 없습니다."));
        Optional<Search> searchOpt = searchRepository.findById(searchId);

        if (searchOpt.isPresent()) {
            searchRepository.delete(searchOpt.get());
            return true; // 삭제 성공
        }
        return false; // 삭제 실패 (존재하지 않음)
    }

    public List<SearchRes> getHistory(String email) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        List<Search> searches = searchRepository.findAllByUserId(currentUser.getId());

        return searches.stream()
                .map(search -> SearchRes.builder()
                        .searchId(search.getId())
                        .query(search.getQuery())
                        .build()
                )
                .toList();
    }

    public void addHistory(String email, SearchReq searchReq) {

    }
}

