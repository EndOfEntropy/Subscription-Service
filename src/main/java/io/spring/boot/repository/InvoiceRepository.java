package io.spring.boot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.spring.boot.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>{
	
    List<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId);
    
    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}