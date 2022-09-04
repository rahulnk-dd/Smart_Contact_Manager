package com.smartcontactmanager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.User;

public interface ContactRepo extends JpaRepository<Contact, Integer> {

	// pagination...
	// pageable current-page, contact per page 5
	@Query("from Contact as d where d.user.id =:userId ")
	public Page<Contact> findContactByUser( @Param("userId") int userId, Pageable pageable);
	
//	for search control
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
