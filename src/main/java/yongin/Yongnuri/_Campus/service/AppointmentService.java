package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.Appointment;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.appointment.AppointmentRequestDto;
import yongin.Yongnuri._Campus.dto.appointment.AppointmentUpdateRequestDto;
import yongin.Yongnuri._Campus.repository.AppointmentRepository;
import yongin.Yongnuri._Campus.repository.LostItemRepository;
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import yongin.Yongnuri._Campus.domain.Enum;
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public Long createAppointment(String email, AppointmentRequestDto requestDto) {
        User currentUserAsSeller = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Long sellerId = currentUserAsSeller.getId();

        Long buyerId = requestDto.getBuyerId();
        if (buyerId == null) {
            throw new IllegalArgumentException("구매자 ID는 필수입니다.");
        }
        String postType = requestDto.getPostType();
        if (postType == null || postType.trim().isEmpty()) {
            postType = "USED_ITEM";
        }

        if ("USED_ITEM".equals(postType)) {
            UsedItem item = usedItemRepository.findById(requestDto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("중고거래 게시글을 찾을 수 없습니다."));

            if (!item.getUserId().equals(sellerId)) { // 게시글 작성자가 아니면 에러
                throw new AccessDeniedException("자신의 게시글에 대해서만 약속을 잡을 수 있습니다.");
            }

        } /**
         else if ("LOST_ITEM".equals(postType)) {
            LostItem item = lostItemRepository.findById(requestDto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("분실물 게시글을 찾을 수 없습니다."));
            party1_Id = item.getUser().getId();

            if (party1_Id.equals(party2_Id)) {
                throw new IllegalArgumentException("자신의 게시글에 대해 약속을 잡을 수 없습니다.");
            }
            if (item.getPurpose() == Enum.LostItemPurpose.FOUND) {
                return saveAppointment(requestDto, party1_Id, party2_Id);
            }
            else {
                return saveAppointment(requestDto, party2_Id, party1_Id);
            }
        } */else {
            throw new IllegalArgumentException("지원하지 않는 게시글 타입입니다.");
        }
        if (sellerId.equals(buyerId)) {
            throw new IllegalArgumentException("자기 자신과 약속을 잡을 수 없습니다.");
        }

        return saveAppointment(requestDto, postType, sellerId, buyerId);
    }

    private Long saveAppointment(AppointmentRequestDto requestDto, String postType, Long sellerId, Long buyerId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime appointmentAt = LocalDateTime.parse(requestDto.getDate() + " " + requestDto.getTime(), formatter);

        Appointment newAppointment = Appointment.builder()
                .chatRoomId(requestDto.getChatRoomId())
                .sellerId(sellerId)
                .buyerId(buyerId)
                .appointmentAt(appointmentAt)
                .location(requestDto.getLocation())
                .status(Enum.AppointmentStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .postType(postType)
                .postId(requestDto.getPostId())
                .build();

        Appointment savedAppointment = appointmentRepository.save(newAppointment);
        return savedAppointment.getId();
    }

   //약속 수정

    @Transactional
    public void updateAppointment(Long appointmentId, String email, AppointmentUpdateRequestDto requestDto) {
        User currentUser = getUserByEmail(email);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 약속입니다."));
        if (!appointment.getSellerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("이 약속을 수정할 권한이 없습니다.");
        }
        if (requestDto.getDate() != null && requestDto.getTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime newAppointmentAt = LocalDateTime.parse(requestDto.getDate() + " " + requestDto.getTime(), formatter);
            appointment.setAppointmentAt(newAppointmentAt);
        }
        if (requestDto.getLocation() != null) {
            appointment.setLocation(requestDto.getLocation());
        }
        if (requestDto.getStatus() != null) {
            appointment.setStatus(Enum.AppointmentStatus.valueOf(requestDto.getStatus().trim().toUpperCase()));
        }
    }
}