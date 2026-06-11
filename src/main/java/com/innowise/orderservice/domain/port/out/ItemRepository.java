package com.innowise.orderservice.domain.port.out;

import com.innowise.orderservice.domain.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

//    @Query("SELECT it FROM Item it WHERE it.id IN :itemIds")
//    List<Item> findAllWithIdIn(@Param("itemIds") List<Long> itemIds);

}
