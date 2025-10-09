// controller/HistoryController.java (신규 파일)
package yongin.Yongnuri._Campus.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yongin.Yongnuri._Campus.dto.groupbuy.GroupBuyResponseDto;
import yongin.Yongnuri._Campus.dto.lostitem.LostItemResponseDto;
import yongin.Yongnuri._Campus.dto.useditem.UsedItemResponseDto;
import yongin.Yongnuri._Campus.security.CustomUserDetails;
import yongin.Yongnuri._Campus.service.HistoryService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    //중고거래내역조회 "sell" 또는 "buy"
    @GetMapping("/used-items")
    public ResponseEntity<List<UsedItemResponseDto>> getMyUsedItemTransactions(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("type") String type
    ) {
        List<UsedItemResponseDto> transactions = historyService.getUsedItemTransactions(user.getUser().getEmail(), type);
        return ResponseEntity.ok(transactions);
    }

    //분실물내역조회 "found"(습득), "lost"(분실), "returned"(회수)
    @GetMapping("/lost-items")
    public ResponseEntity<List<LostItemResponseDto>> getMyLostItemHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("filter") String filter
    ) {
        List<LostItemResponseDto> history = historyService.getLostItemHistory(user.getUser().getEmail(), filter);
        return ResponseEntity.ok(history);
    }
    //공동구매내역 "registered"(등록), "applied"(신청)
    @GetMapping("/group-buys")
    public ResponseEntity<List<GroupBuyResponseDto>> getMyGroupBuyHistory(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("filter") String filter
    ) {
        List<GroupBuyResponseDto> history = historyService.getGroupBuyHistory(user.getUser().getEmail(), filter);
        return ResponseEntity.ok(history);
    }
}