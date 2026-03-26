package io.spring.boot.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.spring.boot.entity.Invoice;
import io.spring.boot.entity.InvoiceStatus;
import io.spring.boot.entity.User;
import io.spring.boot.security.JwtService;
import io.spring.boot.security.SecurityConfig;
import io.spring.boot.service.InvoiceService;

@WebMvcTest(InvoiceController.class)
@Import(SecurityConfig.class)
class InvoiceControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private User testUser;

    private void testSetup() {
        testUser = new User(1L, "test@test.com", "password");
    }


    @Test
    void getInvoices_shouldReturnUserInvoices() throws Exception {
        // Setup
        testSetup();

        Invoice invoice1 = new Invoice(
                10L,
                "INV-2026-0001",
                new BigDecimal("9.99"),
                "Subscription: PREMIUM",
                InvoiceStatus.PAID,
                testUser,
                null,
                null,
                "inv_123",
                OffsetDateTime.now()
        );

        Invoice invoice2 = new Invoice(
                11L,
                "INV-2026-0002",
                new BigDecimal("9.99"),
                "Subscription: PREMIUM",
                InvoiceStatus.PAID,
                testUser,
                null,
                null,
                "inv_456",
                OffsetDateTime.now().minusMonths(1)
        );

        given(invoiceService.getUserInvoices(1L)).willReturn(List.of(invoice1, invoice2));

        // Action
        ResultActions response = mockMvc.perform(get("/api/invoices")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))

                // First invoice
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-2026-0001"))
                .andExpect(jsonPath("$[0].amount").value(9.99))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[0].description").value("Subscription: PREMIUM"))

                // Second invoice
                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].invoiceNumber").value("INV-2026-0002"))
                .andExpect(jsonPath("$[1].amount").value(9.99))
                .andExpect(jsonPath("$[1].status").value("PAID"))
                .andExpect(jsonPath("$[1].description").value("Subscription: PREMIUM"));
    }

    @Test
    void getInvoices_shouldReturnEmptyListWhenNoneExist() throws Exception {
        // Setup
        testSetup();

        given(invoiceService.getUserInvoices(1L)).willReturn(List.of());

        // Action
        ResultActions response = mockMvc.perform(get("/api/invoices")
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(status().isOk());
    }

    @Test
    void downloadInvoice_shouldReturnPdf() throws Exception {
        // Setup
        testSetup();

        Long invoiceId = 10L;

        Invoice invoice = new Invoice(
                invoiceId,
                "INV-2026-0001",
                new BigDecimal("9.99"),
                "Subscription: PREMIUM",
                InvoiceStatus.PAID,
                testUser,
                null,
                null,
                "inv_123",
                OffsetDateTime.now()
        );

        byte[] pdfBytes = "fake-pdf-content".getBytes();

        given(invoiceService.getUserInvoices(1L)).willReturn(List.of(invoice));
        given(invoiceService.generateInvoicePDF(invoiceId)).willReturn(pdfBytes);

        // Action
        ResultActions response = mockMvc.perform(get("/api/invoices/{id}/download", invoiceId)
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"INV-2026-0001.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void downloadInvoice_shouldThrowIfInvoiceNotOwnedByUser() throws Exception {
        // Setup
        testSetup();

        Long invoiceId = 99L;

        // User has no matching invoices
        given(invoiceService.getUserInvoices(1L)).willReturn(List.of());

        // Action
        ResultActions response = mockMvc.perform(get("/api/invoices/{id}/download", invoiceId)
                .with(user(testUser))
                .contentType(MediaType.APPLICATION_JSON));

        // Verify
        response.andDo(print())
                .andExpect(status().isNotFound()); // No handler for NoSuchElementException
    }
}