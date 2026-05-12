package org.example.service.impl;

import org.example.service.*;
import org.example.service.RentalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.RentalAdminResponseDto;
import org.example.dto.rental.RentalResponseDto;
import org.example.dto.rental.StartRentalDto;
import org.example.entity.*;

import org.example.exception.BusinessException;
import org.example.exception.ResourceNotFoundException;
import org.example.mapper.RentalMapper;
import org.example.repository.RentalRepository;
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

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static java.util.Objects.isNull;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final UserService userService;
    private final ScooterService scooterService;
    private final TariffService tariffService;
    private final PromoCodeService promoCodeService;
    private final RentalPointService rentalPointService;
    private final UserSubscriptionService userSubscriptionService;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;

    private static final int DEFAULT_HOLD_MINUTES = 10;
    private static final double PARKING_RADIUS_METERS = 50.0;

    public RentalResponseDto startRental(StartRentalDto rentalDto) {
        User user = getAuthenticatedUser();
        Tariff tariff = tariffService.findTariffById(rentalDto.getTariffId());
        Scooter scooter = scooterService.findScooterById(rentalDto.getScooterId());

        validateRentalStartPreconditions(user, scooter); // проверка начала поездки

        BigDecimal holdAmount = calculateRequiredHoldAmount(user.getId(), tariff, scooter); // расчет холда
        holdFundsForRentalStart(user, holdAmount); // проверка баланса и холд средств

        PromoCode promoCode = findValidPromoCode(rentalDto.getPromoCode()); // проверка промокода
        scooter.setScooterStatus(ScooterStatus.RENTED); // сделать самокат активным

        Rental rental = buildRentalEntity(user, scooter, tariff, promoCode); // создать поездку

        rental = rentalRepository.create(rental);
        log.info("Успешно начата поездка ID={} для пользователя {}", rental.getId(), user.getUsername());

        return rentalMapper.toDto(rental);
    }

    public RentalResponseDto finishRental(Long id, FinishRentalDto finishRentalDto) {
        Rental rental = findRentalEntityById(id);

        validateRentalFinish(rental); // есть права на завершение поездки
        markRentalAsFinished(rental, finishRentalDto);

        long durationMinutes = calculateBillableMinutes(rental.getStartTime(), rental.getEndTime());
        BigDecimal totalPrice = calculateRentalPrice(rental, durationMinutes); // считаем стоимость поездки
        rental.setPrice(totalPrice);

        setUserBalanceAfterRental(rental.getUser(), totalPrice); // пересчитываем баланс юзера
        parkingScooter(rental.getScooter(), finishRentalDto);

        log.info("Поездка ID={} успешно завершена. Списано: {}. Длительность: {} мин. Дистанция: {} км",
                rental.getId(), totalPrice, durationMinutes, finishRentalDto.getDistance());

        return rentalMapper.toDto(rental);
    }

    public List<RentalResponseDto> findRentalsByUserId(Long userId) {
        userService.findEntityById(userId);
        List<Rental> rentals = rentalRepository.findAllByUserId(userId);

        log.info("Получена история поездок для пользователя с ID={}. Количество записей: {}", userId, rentals.size());

        return rentalMapper.toDtos(rentals);
    }

    public List<RentalAdminResponseDto> findRentalsByScooterId(Long scooterId) {
        scooterService.findScooterById(scooterId);
        List<Rental> rentals = rentalRepository.findByScooterId(scooterId);

        log.info("Получена история аренды для самоката с ID={}. Количество записей: {}", scooterId, rentals.size());

        return rentalMapper.toAdminDtos(rentals);
    }

    private record PricingContext(BigDecimal tariffPrice, BigDecimal effectiveDuration) {
    }

    private void validateRentalFinish(Rental rental) {
        if (isFalse(rental.getIsActive())) {
            throw new BusinessException("Эта поездка уже была завершена ранее");
        }

        User currentUser = getAuthenticatedUser();

        Long rentalUserId = rental.getUser().getId(); // пользователь поездки
        boolean isOwner = rentalUserId.equals(currentUser.getId());
        boolean isAdmin = Role.ADMIN == currentUser.getRole();

        if (isFalse(isOwner) && isFalse(isAdmin)) {
            throw new AccessDeniedException("Вы не можете завершить чужую поездку!");
        }
    }

    private User getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isNull(authentication) || isNull(authentication.getPrincipal())) {
            throw new AccessDeniedException("Пользователь не авторизован");
        }
        return (User) authentication.getPrincipal();
    }

    private void validateRentalStartPreconditions(User user, Scooter scooter) {
        Optional<Rental> activeRental = rentalRepository.findActiveRentalByUserId(user.getId());
        if (activeRental.isPresent()) {
            throw new BusinessException("У пользователя уже есть активная поездка");
        }
        if (ScooterStatus.RENTED == scooter.getScooterStatus()) {
            throw new BusinessException("Самокат уже занят");
        }
    }

    private BigDecimal calculateRequiredHoldAmount(Long userId, Tariff tariff, Scooter scooter) {
        BigDecimal pricePerMinute = scooter.getScooterModel().getPricePerMinute();
        BigDecimal defaultHoldDuration = BigDecimal.valueOf(DEFAULT_HOLD_MINUTES);
        BigDecimal holdForTime = pricePerMinute.multiply(defaultHoldDuration);
        
        BigDecimal holdAmount = tariff.getPrice().add(holdForTime);

        Optional<UserSubscription> userSub = userSubscriptionService.findValidActiveSubscription(userId);
        if (userSub.isPresent()) {
            Subscription subscription = userSub.get().getSubscription();
            if (subscription.getIsFreeStart()) {
                return holdForTime;
            }
        }
        return holdAmount;
    }

    private void holdFundsForRentalStart(User user, BigDecimal holdAmount) {
        if (user.getBalance().compareTo(holdAmount) < 0) {
            throw new BusinessException(
                    "Недостаточно средств на балансе для начала поездки (требуется " + holdAmount + ")");
        }

        BigDecimal newBalance = user.getBalance().subtract(holdAmount);
        BigDecimal newHeldBalance = user.getHeldBalance().add(holdAmount);
        
        user.setBalance(newBalance);
        user.setHeldBalance(newHeldBalance);
        userService.update(user);
    }

    private PromoCode findValidPromoCode(String promoCodeRaw) {
        if (isBlank(promoCodeRaw)) {
            log.info("Промокод не предоставлен, пропуск валидации");
            return null;
        }

        PromoCode promoCode = promoCodeService.findByCode(promoCodeRaw);

        boolean isExpired = nonNull(promoCode.getEndDate()) && promoCode.getEndDate().isBefore(LocalDateTime.now());
        if (isFalse(promoCode.getIsActive()) || isExpired) {
            throw new BusinessException("Промокод недействителен или истек");
        }
        return promoCode;
    }


    private Rental buildRentalEntity(User user, Scooter scooter, Tariff tariff, PromoCode promoCode) {
        return Rental.builder()
                .user(user)
                .scooter(scooter)
                .tariff(tariff)
                .promoCode(promoCode)
                .startTime(LocalDateTime.now())
                .startLatitude(scooter.getLatitude())
                .startLongitude(scooter.getLongitude())
                .isActive(true)
                .build();
    }

    private Rental findRentalEntityById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Поездка с ID " + id + " не найдена"));
    }

    private void markRentalAsFinished(Rental rental, FinishRentalDto finishRentalDto) {
        rental.setEndLatitude(finishRentalDto.getEndLatitude());
        rental.setEndLongitude(finishRentalDto.getEndLongitude());
        rental.setEndTime(LocalDateTime.now());
        rental.setIsActive(false);
        rental.setDistance(finishRentalDto.getDistance());
    }

    private long calculateBillableMinutes(LocalDateTime start, LocalDateTime end) {
        long seconds = Duration.between(start, end).getSeconds();
        long minutes = (long) Math.ceil(seconds / 60.0);
        return Math.max(1, minutes); // округление вверх, минимум 1 минута
    }

    private BigDecimal calculateRentalPrice(Rental rental, long durationMinutes) {
        BigDecimal pricePerMinute = rental.getScooter().getScooterModel().getPricePerMinute(); // стоимость минуты

        // получаем стоимость старта и время поездки с учетом подписки
        PricingContext pricingContext = calculatePriceWithSubscription(rental, durationMinutes);
        BigDecimal min = pricingContext.effectiveDuration();
        BigDecimal startPrice = pricingContext.tariffPrice();

        BigDecimal totalPrice = pricePerMinute.multiply(min).add(startPrice); // стоимость поездки
        totalPrice = applyPromoCodeDiscount(totalPrice, rental.getPromoCode()); // стоимость поездки с промиком

        return totalPrice;
    }

    private PricingContext calculatePriceWithSubscription(Rental rental, long durationMinutes) {
        BigDecimal tariffPrice = rental.getTariff().getPrice();
        BigDecimal effectiveDuration = BigDecimal.valueOf(durationMinutes);
        Long userId = rental.getUser().getId();

        Optional<UserSubscription> userSubOpt = userSubscriptionService.findValidActiveSubscription(userId);
        if (userSubOpt.isEmpty()) { // нет подписки
            log.info("Нет активного абонемента для пользователя ID={}, используется стандартный тариф", userId);
            return new PricingContext(tariffPrice, effectiveDuration);
        }

        UserSubscription userSub = userSubOpt.get();
        Subscription subscription = userSub.getSubscription();
        if  (subscription.getIsFreeStart()) { // проверка бесплатного старта
            tariffPrice = BigDecimal.ZERO;
            log.info("Применение скидки на бесплатный старт от абонемента ID={}", subscription.getId());
        }

        // вычет из поездки оставшихся минут в абонементе
        effectiveDuration = calculateMinutesWithSubscription(durationMinutes, userSub);

        return new PricingContext(tariffPrice, effectiveDuration);
    }


    private BigDecimal calculateMinutesWithSubscription(long durationMinutes, UserSubscription userSub) {
        int availableMin = userSub.getRemainingMinutes();
        if (availableMin <= 0) {
            log.info("В абонементе ID={} не осталось минут, {} мин подлежит оплате",
                    userSub.getSubscription().getId(), durationMinutes);
            return BigDecimal.valueOf(durationMinutes);
        }

        if (availableMin >= durationMinutes) {
            log.info("Все {} минут покрыты абонементом ID={}", durationMinutes, userSub.getSubscription().getId());
            userSub.setRemainingMinutes(availableMin - (int) durationMinutes);
            userSubscriptionService.updateSubscription(userSub);
            return BigDecimal.ZERO;
        }

        log.info("Частичное покрытие: {} минут из абонемента ID={}, оставшиеся {} минут подлежат оплате", 
                availableMin, userSub.getSubscription().getId(), durationMinutes - availableMin);
        userSub.setRemainingMinutes(0);
        userSubscriptionService.updateSubscription(userSub);
        return BigDecimal.valueOf(durationMinutes - availableMin);
    }

    private BigDecimal applyPromoCodeDiscount(BigDecimal totalPrice, PromoCode promoCode) {
        if (isNull(promoCode)) {
            log.info("К поездке не применен промокод");
            return totalPrice;
        }
        
        BigDecimal discountPercentage = BigDecimal.valueOf(promoCode.getDiscount());
        BigDecimal discountMultiplier = discountPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = totalPrice.multiply(discountMultiplier);
        
        return totalPrice.subtract(discountAmount);
    }

    private void setUserBalanceAfterRental(User user, BigDecimal totalPrice) {
        BigDecimal heldAmount = user.getHeldBalance();
        user.setHeldBalance(BigDecimal.ZERO);
        user.setBalance(user.getBalance().add(heldAmount).subtract(totalPrice));
        userService.update(user);
    }


    private void parkingScooter(Scooter scooter, FinishRentalDto dto) {
        // ищем ближайшую парковку уровня 3 в радиусе PARKING_RADIUS_METERS
        RentalPoint nearestPoint = rentalPointService.findNearestValidParkingPoint(
                dto.getEndLatitude(), 
                dto.getEndLongitude(), 
                PARKING_RADIUS_METERS
        ).orElseThrow(() -> new BusinessException(
                "Невозможно завершить аренду. Вы находитесь вне зоны парковки (радиус " + PARKING_RADIUS_METERS + "м). " +
                "Пожалуйста, оставьте самокат на ближайшей точке проката."
        ));

        applyScooterFinishState(scooter, dto, nearestPoint); // обновляем данные самоката
        scooterService.update(scooter);
    }

    private void applyScooterFinishState(Scooter scooter, FinishRentalDto dto, RentalPoint nearestPoint) {
        scooter.setScooterStatus(ScooterStatus.AVAILABLE);
        scooter.setLongitude(dto.getEndLongitude());
        scooter.setLatitude(dto.getEndLatitude());
        scooter.setRentalPoint(nearestPoint); // привязываем самокат к новой точке
        scooter.setBatteryLevel(dto.getBatteryLevel());

        scooter.setMileage(scooter.getMileage().add(dto.getDistance()));
    }
}
