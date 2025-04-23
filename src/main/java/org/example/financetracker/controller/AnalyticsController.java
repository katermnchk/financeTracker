package org.example.financetracker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetracker.dto.AnalyticsDTO;
import org.example.financetracker.dto.TransactionDTO;
import org.example.financetracker.entity.User;
import org.example.financetracker.security.CustomUserDetails;
import org.example.financetracker.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AnalyticsController {

    private final TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public AnalyticsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(value = "startDate", required = false) String startDate,
                            @RequestParam(value = "endDate", required = false) String endDate,
                            @RequestParam(value = "type", required = false) String type,
                            Model model) {
        if (userDetails == null) {
            return "redirect:/login?error";
        }

        User user = userDetails.getUser();

        LocalDateTime start;
        try {
            start = startDate != null && !startDate.isEmpty() ?
                    LocalDate.parse(startDate).atStartOfDay() :
                    LocalDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        } catch (Exception e) {
            start = LocalDateTime.now().minusMonths(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            System.out.println("Ошибка парсинга startDate: " + startDate + ", использую дефолтное значение: " + start);
        }

        LocalDateTime end;
        try {
            end = endDate != null && !endDate.isEmpty() ?
                    LocalDate.parse(endDate).atTime(23, 59, 59, 999999999) :
                    LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        } catch (Exception e) {
            end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            System.out.println("Ошибка парсинга endDate: " + endDate + ", использую дефолтное значение: " + end);
        }

        List<AnalyticsDTO> analytics = transactionService.getAnalytics(user.getId(), start, end, type);
        try {
            String analyticsJson = objectMapper.writeValueAsString(analytics);
            model.addAttribute("analyticsDataJson", analyticsJson);
        } catch (JsonProcessingException e) {
            model.addAttribute("analyticsDataJson", "[]"); // fallback
            System.out.println("Ошибка сериализации аналитики: " + e.getMessage());
        }

        System.out.println("Аналитика для userId=" + user.getId() + ", start=" + start + ", end=" + end + ", type=" + type + ": " + analytics);

        //model.addAttribute("analyticsData", analytics);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("type", type);

        return "analytics";
    }
}