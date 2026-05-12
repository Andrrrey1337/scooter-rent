package org.example.service;

import org.example.dto.rental.FinishRentalDto;
import org.example.dto.rental.RentalAdminResponseDto;
import org.example.dto.rental.RentalResponseDto;
import org.example.dto.rental.StartRentalDto;

import java.util.List;

public interface RentalService {
    RentalResponseDto startRental(StartRentalDto rentalDto);

    RentalResponseDto finishRental(Long id, FinishRentalDto finishRentalDto);

    List<RentalResponseDto> findRentalsByUserId(Long userId);

    List<RentalAdminResponseDto> findRentalsByScooterId(Long scooterId);

}
