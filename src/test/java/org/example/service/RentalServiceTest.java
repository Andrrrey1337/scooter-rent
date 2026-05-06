package org.example.service;

import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.StartRentalDto;
import org.example.entity.*;
import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private UserService userService;
    @Mock private ScooterService scooterService;
    @Mock private TariffService tariffService;
    @Mock private RentalRepository rentalRepository;
    @Mock private ScooterRepository scooterRepository;
    @Mock private UserRepository userRepository;
    @Mock private PromoCodeRepository promoCodeRepository;
    @Mock private UserSubscriptionRepository userSubscriptionRepository;
    @Mock private RentalPointRepository rentalPointRepository;

    @InjectMocks
    private RentalService rentalService;

    private User user;
    private Scooter scooter;
    private Tariff tariff;
    private Rental rental;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setBalance(new BigDecimal("1000.00"));
        user.setHeldBalance(BigDecimal.ZERO);
        user.setRole(Role.USER);

        ScooterModel model = new ScooterModel();
        model.setPricePerMinute(new BigDecimal("5.00"));

        scooter = new Scooter();
        scooter.setId(1L);
        scooter.setScooterStatus(ScooterStatus.AVAILABLE);
        scooter.setScooterModel(model);
        scooter.setLatitude(new BigDecimal("53.9"));
        scooter.setLongitude(new BigDecimal("27.5"));

        tariff = new Tariff();
        tariff.setId(1L);
        tariff.setPrice(new BigDecimal("50.00"));

        rental = Rental.builder()
                .id(1L)
                .user(user)
                .scooter(scooter)
                .tariff(tariff)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .isActive(true)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("startRental - Успех")
    void startRental_Success() {
        StartRentalDto startDto = new StartRentalDto();
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);

        when(tariffService.findTariffById(1L)).thenReturn(tariff);
        when(scooterService.findScooterById(1L)).thenReturn(scooter);
        when(rentalRepository.findActiveRentalByUserId(1L)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());
        when(rentalRepository.create(any(Rental.class))).thenReturn(rental);

        Rental result = rentalService.startRental(startDto);

        assertNotNull(result);
        assertEquals(ScooterStatus.RENTED, scooter.getScooterStatus());
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("startRental - Самокат не найден")
    void startRental_ScooterNotFound_ThrowsException() {
        StartRentalDto startDto = new StartRentalDto();
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);

        when(tariffService.findTariffById(1L)).thenReturn(tariff);
        when(scooterService.findScooterById(1L)).thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> rentalService.startRental(startDto));
    }

    @Test
    @DisplayName("startRental - Ошибка: самокат недоступен (уже в аренде или разряжен)")
    void startRental_ScooterNotAvailable_ThrowsBusinessException() {
        // самокат сейчас занят кем-то другим
        scooter.setScooterStatus(ScooterStatus.RENTED);
        when(scooterService.findScooterById(1L)).thenReturn(scooter);

        StartRentalDto startDto = new StartRentalDto();
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> rentalService.startRental(startDto));
    }

    @Test
    @DisplayName("startRental - Ошибка: недостаточно средств на балансе и нет абонемента")
    void startRental_InsufficientBalance_ThrowsBusinessException() {
        scooter.setScooterStatus(ScooterStatus.AVAILABLE);
        when(scooterService.findScooterById(1L)).thenReturn(scooter);
        when(tariffService.findTariffById(1L)).thenReturn(tariff);

        // нет подписки с бесплатным стартом
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());

        user.setBalance(BigDecimal.ZERO);

        StartRentalDto startDto = new StartRentalDto();
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);

        BusinessException exception = assertThrows(BusinessException.class, () -> rentalService.startRental(startDto));
    }

    @Test
    @DisplayName("startRental - Ошибка: введен несуществующий промокод")
    void startRental_InvalidPromoCode_ThrowsResourceNotFoundException() {
        scooter.setScooterStatus(ScooterStatus.AVAILABLE);
        when(scooterService.findScooterById(1L)).thenReturn(scooter);

        when(tariffService.findTariffById(1L)).thenReturn(tariff);

        StartRentalDto startDto = new StartRentalDto();
        startDto.setUserId(1L);
        startDto.setScooterId(1L);
        startDto.setTariffId(1L);
        startDto.setPromoCode("FAKE_PROMO");

        when(promoCodeRepository.findByCode("FAKE_PROMO")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> rentalService.startRental(startDto));    }

    @Test
    @DisplayName("finishRental - Успех")
    void finishRental_Success() {
        FinishRentalDto finishDto = new FinishRentalDto();
        finishDto.setEndLatitude(new BigDecimal("53.91"));
        finishDto.setEndLongitude(new BigDecimal("27.51"));
        finishDto.setDistance(new BigDecimal("1.5"));
        finishDto.setBatteryLevel(80);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(userSubscriptionRepository.findActiveByUserId(1L)).thenReturn(Optional.empty());
        when(rentalPointRepository.findNearestValidParkingPoint(any(), any(), anyDouble()))
                .thenReturn(Optional.of(new RentalPoint()));

        Rental result = rentalService.finishRental(1L, finishDto);

        assertNotNull(result);
        assertFalse(result.getIsActive());
        assertEquals(ScooterStatus.AVAILABLE, scooter.getScooterStatus());
        assertEquals(80, scooter.getBatteryLevel());
        verify(userRepository).update(user);
    }

    @Test
    @DisplayName("finishRental - Ошибка: попытка завершить уже завершенную аренду")
    void finishRental_AlreadyFinished_ThrowsBusinessException() {
        // поездка уже завершена
        rental.setIsActive(false);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));

        FinishRentalDto finishDto = new FinishRentalDto();
        finishDto.setEndLatitude(new BigDecimal("53.91"));
        finishDto.setEndLongitude(new BigDecimal("27.51"));
        finishDto.setDistance(new BigDecimal("1.5"));
        finishDto.setBatteryLevel(80);

        BusinessException exception = assertThrows(BusinessException.class, () -> rentalService.finishRental(1L, finishDto));
    }


    @Test
    @DisplayName("findRentalsByUserId - Успех")
    void findRentalsByUserId_Success() {
        when(userService.findById(1L)).thenReturn(user);
        when(rentalRepository.findAllByUserId(1L)).thenReturn(Collections.singletonList(rental));

        List<Rental> result = rentalService.findRentalsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("findRentalsByScooterId - Успех")
    void findRentalsByScooterId_Success() {
        when(scooterRepository.findById(1L)).thenReturn(Optional.of(scooter));
        when(rentalRepository.findByScooterId(1L)).thenReturn(Collections.singletonList(rental));

        List<Rental> result = rentalService.findRentalsByScooterId(1L);

        assertEquals(1, result.size());
    }
}
