package org.example.repository;

import org.example.entity.RentalPoint;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class RentalPointRepository extends AbstractDao<RentalPoint, Long> {
    public RentalPointRepository() {
        super(RentalPoint.class);
    }

    public Optional<RentalPoint> findRentalPointByName(String name) {
        return entityManager.createQuery("SELECT p FROM RentalPoint p WHERE p.name = :name", RentalPoint.class)
                .setParameter("name", name)
                .getResultStream().findFirst();
    }

    // найти ближайшую точку проката уровня 3 в заданном радиусе
    public Optional<RentalPoint> findNearestValidParkingPoint(BigDecimal latitude, BigDecimal longitude, double maxDistanceMeters) {
        String hql = """
            SELECT rp FROM RentalPoint rp
            WHERE rp.houseNumber IS NOT NULL AND rp.street IS NOT NULL AND rp.city IS NOT NULL
            AND (
                6371000 * acos(
                    cos(radians(:lat)) * cos(radians(rp.latitude)) *
                    cos(radians(rp.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(rp.latitude))
                )
            ) <= :maxDistance
            ORDER BY (
                6371000 * acos(
                    cos(radians(:lat)) * cos(radians(rp.latitude)) *
                    cos(radians(rp.longitude) - radians(:lon)) +
                    sin(radians(:lat)) * sin(radians(rp.latitude))
                )
            ) ASC
        """;

        return entityManager.createQuery(hql, RentalPoint.class)
                .setParameter("lat", latitude)
                .setParameter("lon", longitude)
                .setParameter("maxDistance", maxDistanceMeters)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
