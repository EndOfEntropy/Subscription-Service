package io.spring.boot.controller;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.spring.boot.dto.InvoiceResponse;
import io.spring.boot.entity.Invoice;
import io.spring.boot.entity.User;
import io.spring.boot.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

	private InvoiceService invoiceService;

	public InvoiceController(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}
	
	
	@GetMapping
	public ResponseEntity<List<InvoiceResponse>> getInvoices(@AuthenticationPrincipal User currentUser){
		
        List<InvoiceResponse> response = invoiceService.getUserInvoices(currentUser.getId()).stream()
				.map(i -> new InvoiceResponse(
					i.getId(), i.getInvoiceNumber(), i.getAmount(), i.getStatus().toString(),
					i.getInvoiceDate(), i.getDescription()
				))
				.toList();
		
		return ResponseEntity.ok(response);
	}
	
	// Download invoice PDF
	@GetMapping("/{id}/download")
	public ResponseEntity<byte[]> downloadInvoice(@AuthenticationPrincipal User currentUser, @PathVariable Long id){
		// verify the invoice belongs to the user
        Invoice invoice = invoiceService.getUserInvoices(currentUser.getId()).stream()
                .filter(inv -> inv.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

		byte[] pdfBytes = invoiceService.generateInvoicePDF(id);
		// create http metadata
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(
				ContentDisposition.attachment()
					.filename(invoice.getInvoiceNumber() + ".pdf")
					.build()
		);
		
		return ResponseEntity.ok().headers(headers).body(pdfBytes);
	}
}
