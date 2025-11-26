package projectSem4.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.servlet.http.HttpServletResponse;
import projectSem4.com.service.VNPayService;

@Controller
public class PaymentController {

    private final VNPayService vnPayService = new VNPayService();

    @GetMapping("/pay")
    public void payment(@RequestParam(name = "amount", defaultValue = "10000,00") Double amount,
                        @RequestParam(name = "info", defaultValue = "Thanh toán đơn hàng test") String info,
                        @RequestParam(name = "orderId", defaultValue = "ORDER999636463") String orderId,
                        HttpServletResponse response) throws Exception {
        String url = vnPayService.createPaymentUrl(amount, info, orderId);
        response.sendRedirect(url); // điều hướng người dùng tới VNPay
    }
}
