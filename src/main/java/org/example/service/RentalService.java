package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.StartRentalDto;
import org.example.entity.*;

import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RentalService {
    private final UserService userService;
    private final ScooterService scooterService;
    private final TariffService tariffService;
    private final RentalRepository rentalRepository;
    private final ScooterRepository scooterRepository;
    private final UserRepository userRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final RentalPointRepository rentalPointRepository;

    private static final int DEFAULT_HOLD_MINUTES = 10;

    public Rental startRental(StartRentalDto rentalDto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Tariff tariff = tariffService.findTariffById(rentalDto.getTariffId());
        Scooter scooter = scooterService.findScooterById(rentalDto.getScooterId());

        if (rentalRepository.findActiveRentalByUserId(user.getId()).isPresent()) {
            throw new BusinessException("У пользователя уже есть активная поездка");
        }

        if (scooter.getScooterStatus() == ScooterStatus.RENTED) {
            throw new BusinessException("Самокат уже занят");
        }

        // Расчет суммы для холдирования (старт + 10 минут)
        BigDecimal pricePerMinute = scooter.getScooterModel().getPricePerMinute();
        BigDecimal holdAmount = tariff.getPrice().add(pricePerMinute.multiply(BigDecimal.valueOf(DEFAULT_HOLD_MINUTES)));

        // Проверяем подписки для уточнения холда (если старт бесплатный)
        Optional<UserSubscription> userSub = getValidUserSubscription(user.getId());
        if (userSub.isPresent() && userSub.get().getSubscription().getIsFreeStart()) {
            holdAmount = pricePerMinute.multiply(BigDecimal.valueOf(DEFAULT_HOLD_MINUTES));
        }

        if (user.getBalance().compareTo(holdAmount) < 0) {
            throw new BusinessException("Недостаточно средств на балансе для начала поездки (требуется " + holdAmount + ")");
        }

        // Холдируем средства
        user.setBalance(user.getBalance().subtract(holdAmount));
        user.setHeldBalance(user.getHeldBalance().add(holdAmount));
        userRepository.update(user);

        PromoCode promoCode = null;
        if (rentalDto.getPromoCode() != null && !rentalDto.getPromoCode().isBlank()) {
            promoCode = promoCodeRepository.findByCode(rentalDto.getPromoCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Промокод '" + rentalDto.getPromoCode() + "' не найден"));

            if (!promoCode.getIsActive() || (promoCode.getEndDate() != null && promoCode.getEndDate().isBefore(LocalDateTime.now()))) {
                throw new BusinessException("Промокод недействителен или истек");
            }
        }

        scooter.setScooterStatus(ScooterStatus.RENTED);

        Rental rental = Rental.builder()
                .user(user)
                .scooter(scooter)
                .tariff(tariff)
                .promoCode(promoCode)
                .startTime(LocalDateTime.now())
                .startLatitude(scooter.getLatitude())
                .startLongitude(scooter.getLongitude())
                .isActive(true)
                .build();

        rental = rentalRepository.create(rental);
        log.info("Успешно начата поездка ID={} для пользователя {}", rental.getId(), user.getUsername());

        return rental;
    }

    public Rental finishRental(Long id, FinishRentalDto finishRentalDto) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Поездка с ID " + id + " не найдена"));

        validateRentalFinish(rental);

        // Фиксируем базовые данные поездки
        rental.setEndLatitude(finishRentalDto.getEndLatitude());
        rental.setEndLongitude(finishRentalDto.getEndLongitude());
        rental.setEndTime(LocalDateTime.now());
        rental.setIsActive(false);
        rental.setDistance(finishRentalDto.getDistance());

        // Расчет стоимости
        long durationMinutes = calculateDuration(rental.getStartTime(), rental.getEndTime());
        BigDecimal totalPrice = calculateFinalPrice(rental, durationMinutes);
        rental.setPrice(totalPrice);

        // Обновление баланса
        updateUserBalanceAfterRental(rental.getUser(), totalPrice);

        // Обновление состояния самоката
        updateScooterDataAfterRental(rental.getScooter(), finishRentalDto);

        log.info("Поездка ID={} успешно завершена. Списано: {}. Длительность: {} мин. Дистанция: {} км",
                rental.getId(), totalPrice, durationMinutes, finishRentalDto.getDistance());

        return rental;
    }

    private void validateRentalFinish(Rental rental) {
        if (!rental.getIsActive()) {
            throw new BusinessException("Эта поездка уже была завершена ранее");
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isOwner = rental.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Вы не можете завершить чужую поездку!");
        }
    }

    private long calculateDuration(LocalDateTime start, LocalDateTime end) {
        long seconds = Duration.between(start, end).getSeconds();
        long minutes = (long) Math.ceil(seconds / 60.0);
        return Math.max(1, minutes); // Округление вверх, минимум 1 минута
    }

    private BigDecimal calculateFinalPrice(Rental rental, long durationMinutes) {
        BigDecimal pricePerMinute = rental.getScooter().getScooterModel().getPricePerMinute();
        BigDecimal tariffPrice = rental.getTariff().getPrice();
        BigDecimal effectiveDuration = BigDecimal.valueOf(durationMinutes);

        // Обработка абонемента
        Optional<UserSubscription> userSubOpt = getValidUserSubscription(rental.getUser().getId());
        if (userSubOpt.isPresent()) {
            UserSubscription userSub = userSubOpt.get();
            if (userSub.getSubscription().getIsFreeStart()) {
                tariffPrice = BigDecimal.ZERO;
            }

            int availableMin = userSub.getRemainingMinutes();
            if (availableMin > 0) {
                if (availableMin >= durationMinutes) {
                    userSub.setRemainingMinutes(availableMin - (int) durationMinutes);
                    effectiveDuration = BigDecimal.ZERO;
                } else {
                    effectiveDuration = BigDecimal.valueOf(durationMinutes - availableMin);
                    userSub.setRemainingMinutes(0);
                }
                userSubscriptionRepository.update(userSub);
            }
        }

        BigDecimal totalPrice = pricePerMinute.multiply(effectiveDuration).add(tariffPrice);

        // Применение промокода
        if (rental.getPromoCode() != null) {
            BigDecimal discount = BigDecimal.valueOf(rental.getPromoCode().getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalPrice = totalPrice.subtract(totalPrice.multiply(discount));
        }

        return totalPrice;
    }

    private void updateUserBalanceAfterRental(User user, BigDecimal totalPrice) {
        BigDecimal heldAmount = user.getHeldBalance();
        user.setHeldBalance(BigDecimal.ZERO);
        user.setBalance(user.getBalance().add(heldAmount).subtract(totalPrice));
        userRepository.update(user);
    }

    private static final double PARKING_RADIUS_METERS = 50.0;

    private void updateScooterDataAfterRental(Scooter scooter, FinishRentalDto dto) {
        // ищем ближайшую парковку уровня 3 в радиусе PARKING_RADIUS_METERS
        RentalPoint nearestPoint = rentalPointRepository.findNearestValidParkingPoint(
                dto.getEndLatitude(), 
                dto.getEndLongitude(), 
                PARKING_RADIUS_METERS
        ).orElseThrow(() -> new BusinessException(
                "Невозможно завершить аренду. Вы находитесь вне зоны парковки (радиус " + PARKING_RADIUS_METERS + "м). " +
                "Пожалуйста, оставьте самокат на ближайшей точке проката."
        ));

        // обновляем данные самоката
        scooter.setScooterStatus(ScooterStatus.AVAILABLE);
        scooter.setLongitude(dto.getEndLongitude());
        scooter.setLatitude(dto.getEndLatitude());
        scooter.setRentalPoint(nearestPoint); // привязываем самокат к новой точке
        scooter.setBatteryLevel(dto.getBatteryLevel());

        if (scooter.getMileage() == null) {
            scooter.setMileage(BigDecimal.ZERO);
        }
        scooter.setMileage(scooter.getMileage().add(dto.getDistance()));
        scooterRepository.update(scooter);
    }

    public List<Rental> findRentalsByUserId(Long userId) {
        userService.findById(userId);
        List<Rental> rentals = rentalRepository.findAllByUserId(userId);

        log.info("Получена история поездок для пользователя с ID={}. Количество записей: {}", userId, rentals.size());

        return rentals;
    }

    public List<Rental> findRentalsByScooterId(Long scooterId) {
        scooterRepository.findById(scooterId);
        List<Rental> rentals = rentalRepository.findByScooterId(scooterId);

        log.info("Получена история аренды для самоката с ID={}. Количество записей: {}", scooterId, rentals.size());

        return rentals;
    }

    private Optional<UserSubscription> getValidUserSubscription(Long userId) {
        Optional<UserSubscription> userSubscriptionOptional = userSubscriptionRepository.findActiveByUserId(userId);

        if (userSubscriptionOptional.isPresent()) {
            UserSubscription userSubscription = userSubscriptionOptional.get();
            if (userSubscription.getEndDate().isBefore(LocalDateTime.now())) {
                log.info("Срок действия абонемента ID={} для пользователя ID={} истек. Деактивация.",
                        userSubscription.getId(), userId);
                userSubscription.setIsActive(false);
                userSubscriptionRepository.update(userSubscription);
                return Optional.empty();
            }
        }
        return userSubscriptionOptional;
    }
}
