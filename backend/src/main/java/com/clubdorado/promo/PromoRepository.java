package com.clubdorado.promo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromoRepository extends JpaRepository<Promo, Long> {

  List<Promo> findAllByOrderBySortOrderAscIdAsc();
}
