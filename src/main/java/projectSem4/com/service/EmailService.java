package projectSem4.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import projectSem4.com.model.entities.User;

@Service
public class EmailService {

	private final JavaMailSender mailSender;

	// Tuỳ chọn: cấu hình địa chỉ gửi trong application.properties
	// spring.mail.from=no-reply@tradeplatform.local
	@Value("${spring.mail.from:}")
	private String fromAddress;

	@Autowired
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/** Gửi email đơn giản (plain text) */
	public void sendSimple(String to, String subject, String text) {
		SimpleMailMessage msg = new SimpleMailMessage();
		if (fromAddress != null && !fromAddress.isBlank()) {
			msg.setFrom(fromAddress);
		}
		msg.setTo(to);
		msg.setSubject(subject);
		msg.setText(text);
		mailSender.send(msg);
	}

	/** Gửi email reset password bằng link (nếu bạn vẫn cần flow dùng link) */
	public void sendPasswordResetEmail(User user, String resetLink) {
		if (user == null || user.getEmail() == null)
			return;

		String subject = "[TradePlatform] Reset your password";
		String body = "Hello " + displayName(user) + ",\n\n" + "We received a request to reset your account password.\n"
				+ "Please click the link below to set a new password (the link will expire after a certain period):\n\n"
				+ resetLink + "\n\n" + "If you did not make this request, please ignore this email.";

		sendSimple(user.getEmail(), subject, body);
	}

	/** Gửi OTP khi đã có User */
	public void sendOtpEmail(User user, String otp) {
		if (user == null || user.getEmail() == null)
			return;
		doSendOtp(user.getEmail(), displayName(user), otp);
	}

	/** Gửi OTP khi chỉ biết địa chỉ email */
	public void sendOtpEmail(String to, String otp) {
		doSendOtp(to, "bạn", otp);
	}

	// ================= helpers =================
	private void doSendOtp(String to, String name, String otp) {
		String subject = "[TradePlatform] OTP for password reset";
		String body = "Hello " + safe(name) + ",\n\n" + "Your OTP code to reset your password is: " + otp + "\n"
				+ "This OTP is valid for 10 minutes. Do not share this code with anyone.\n\n"
				+ "If you did not request this, please ignore this email.";
		sendSimple(to, subject, body);
	}

	private String displayName(User user) {
		String fn = user.getFullName();
		return (fn == null || fn.isBlank()) ? "bạn" : fn.trim();
	}

	private String safe(String s) {
		return (s == null) ? "" : s;
	}
}
