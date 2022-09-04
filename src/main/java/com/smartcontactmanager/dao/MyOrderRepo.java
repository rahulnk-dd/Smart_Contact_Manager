package com.smartcontactmanager.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcontactmanager.entities.MyOrder;

public interface MyOrderRepo extends JpaRepository<MyOrder, Long> {

	public MyOrder findByOrderId(String orderId);
}
