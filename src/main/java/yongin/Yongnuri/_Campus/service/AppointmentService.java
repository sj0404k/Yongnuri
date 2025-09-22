package yongin.Yongnuri._Campus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yongin.Yongnuri._Campus.domain.Appointment;
import yongin.Yongnuri._Campus.domain.LostItem;
import yongin.Yongnuri._Campus.domain.UsedItem;
import yongin.Yongnuri._Campus.domain.User;
import yongin.Yongnuri._Campus.dto.appointment.AppointmentRequestDto;
import yongin.Yongnuri._Campus.repository.AppointmentRepository;
import yongin.Yongnuri._Campus.repository.LostItemRepository;
import yongin.Yongnuri._Campus.repository.UsedItemRepository;
import yongin.Yongnuri._Campus.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final UsedItemRepository usedItemRepository;
    private final LostItemRepository lostItemRepository;

    @Transactional
    public Long createAppointment(String email, AppointmentRequestDto requestDto) {
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        Long party1_Id;
        Long party2_Id = currentUser.getId();
        String postType = requestDto.getPostType();
        if ("USED_ITEM".equals(postType)) {
            UsedItem item = usedItemRepository.findById(requestDto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("중고거래 게시글을 찾을 수 없습니다."));
            party1_Id = item.getUserId();

            if (party1_Id.equals(party2_Id)) {
                throw new IllegalArgumentException("자신의 게시글에 대해 약속을 잡을 수 없습니다.");
            }

            return saveAppointment(requestDto, party1_Id, party2_Id);

        } else if ("LOST_ITEM".equals(postType)) {
            LostItem item = lostItemRepository.findById(requestDto.getPostId())
                    .orElseThrow(() -> new EntityNotFoundException("분실물 게시글을 찾을 수 없습니다."));
            party1_Id = item.getUser().getId();

            if (party1_Id.equals(party2_Id)) {
                throw new IllegalArgumentException("자신의 게시글에 대해 약속을 잡을 수 없습니다.");
            }
            if (item.getPurpose() == LostItem.ItemPurpose.FOUND) {
                return saveAppointment(requestDto, party1_Id, party2_Id);
            }
            else {
                return saveAppointment(requestDto, party2_Id, party1_Id);
            }
        } else {
            throw new IllegalArgumentException("지원하지 않는 게시글 타입입니다.");
        }
    }

    private Long saveAppointment(AppointmentRequestDto requestDto, Long sellerId, Long buyerId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime appointmentAt = LocalDateTime.parse(requestDto.getDate() + " " + requestDto.getTime(), formatter);

        Appointment newAppointment = Appointment.builder()
                .chatRoomId(requestDto.getChatRoomId())
                .sellerId(sellerId)
                .buyerId(buyerId)
                .appointmentAt(appointmentAt)
                .location(requestDto.getLocation())
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();

        Appointment savedAppointment = appointmentRepository.save(newAppointment);
        return savedAppointment.getId();
    }
}