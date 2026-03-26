package io.spring.boot.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import io.spring.boot.entity.Invoice;
import io.spring.boot.entity.InvoiceStatus;
import io.spring.boot.entity.Payment;
import io.spring.boot.entity.PaymentStatus;
import io.spring.boot.repository.InvoiceRepository;
import io.spring.boot.repository.PaymentRepository;
import io.spring.boot.repository.SubscriptionRepository;

@Service
public class InvoiceService {

	private InvoiceRepository invoiceRepository;
	
	public InvoiceService(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}
	
	@Transactional(readOnly = true)
	public Invoice getInvoiceById(Long invoiceId) {
		return invoiceRepository.findById(invoiceId)
				.orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));
	}

	@Transactional(readOnly = true)
	public Invoice getInvoiceByStripeId(String stripeInvoiceId){
		return invoiceRepository.findByStripeInvoiceId(stripeInvoiceId)
					.orElseThrow(() -> new NoSuchElementException("Invoice not found: " + stripeInvoiceId));
	}
	
	@Transactional(readOnly = true)
	public Invoice getInvoiceByInvoiceNumber(String invoiceNumber){
		return invoiceRepository.findByInvoiceNumber(invoiceNumber)
					.orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceNumber));
	}
	
	@Transactional(readOnly = true)
	public List<Invoice> getUserInvoices(Long userId){
		List<Invoice> invoices = invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId);
		
		if(invoices != null) {
			return invoices;
		}
		throw new NoSuchElementException("No invoices found for this user id");
	}
	
	@Transactional
	public Invoice createInvoice(Payment payment){
		
		Invoice invoice = new Invoice(generateInvoiceNumber(), payment.getAmount(), 
				"Subscription: " + payment.getSubscription().getSubscriptionPlan().getName(),
				(payment.getStatus() == PaymentStatus.SUCCEEDED ? InvoiceStatus.PAID : InvoiceStatus.DRAFT), 
				payment.getUser(), payment.getSubscription(), payment, payment.getStripeInvoiceId(), OffsetDateTime.now());
		
		return invoiceRepository.save(invoice);
	}
	
	private String generateInvoiceNumber() {
		int year = OffsetDateTime.now().getYear();
		long count = invoiceRepository.count() + 1;
		return String.format("INV-%d-%04d", year, count);
	}
	
	@Transactional
	public byte[] generateInvoicePDF(Long invoiceId) {
		Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new NoSuchElementException("Invoice not found: " + invoiceId));
		
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Company header
        Paragraph header = new Paragraph("*THE COMPANY NAME*")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER);
        document.add(header);
        
        document.add(new Paragraph("Address Line 1\nAddress Line 2\nEmail: *billing@company.com*")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10));
        document.add(new Paragraph("\n"));
        
        // Invoice details
        document.add(new Paragraph("INVOICE").setFontSize(16).setBold());
        
        document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
        document.add(new Paragraph("Invoice Date: " + invoice.getInvoiceDate().format(
            DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        document.add(new Paragraph("Status: " + invoice.getStatus()));
        document.add(new Paragraph("\n"));
        
        // Bill to
        document.add(new Paragraph("BILL TO:").setBold());
        document.add(new Paragraph(invoice.getUser().getEmail()));
        document.add(new Paragraph("\n"));
		
        // Items table
        Table table = new Table(new float[]{3, 1, 2});
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Header
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Qty").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
        
        // Items
        table.addCell(new Cell().add(new Paragraph(invoice.getDescription())));
        table.addCell(new Cell().add(new Paragraph("1")));
        table.addCell(new Cell().add(new Paragraph("$" + invoice.getAmount())));
        
        // Total
        table.addCell(new Cell().add(new Paragraph("").setBold()));
        table.addCell(new Cell().add(new Paragraph("Total:").setBold()));
        table.addCell(new Cell().add(new Paragraph("$" + invoice.getAmount()).setBold()));
        
        document.add(table);
        
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("Thank you for your business!")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10));
        
        document.close();
        
        return baos.toByteArray();
	}
}
