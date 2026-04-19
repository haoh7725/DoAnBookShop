package com.example.BookShop.service;

import com.example.BookShop.model.Coupon;
import com.example.BookShop.model.DiscountType;
import com.example.BookShop.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class CouponService {

    private final CouponRepository couponRepo;

    public CouponService(CouponRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    /**
     * Validate mã giảm giá, trả về Coupon nếu hợp lệ.
     * Ném RuntimeException với thông báo lỗi nếu không hợp lệ.
     */
    public Coupon validate(String code, double orderTotal) {
        Coupon coupon = couponRepo.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        if (!coupon.isActive()) {
            throw new RuntimeException("Mã giảm giá đã bị vô hiệu hóa!");
        }
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }
        if (coupon.getUsageLimit() > 0 && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
        }
        if (orderTotal < coupon.getMinOrderAmount()) {
            throw new RuntimeException(
                    "Đơn hàng tối thiểu " +
                            String.format("%,.0f", coupon.getMinOrderAmount()) +
                            "đ mới được dùng mã này!"
            );
        }
        return coupon;
    }

    /**
     * Tính số tiền được giảm.
     */
    public double calcDiscount(Coupon coupon, double orderTotal) {
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            return Math.min(orderTotal * coupon.getDiscountValue() / 100.0, orderTotal);
        } else {
            return Math.min(coupon.getDiscountValue(), orderTotal);
        }
    }

    /**
     * Tăng usedCount sau khi đặt hàng thành công.
     */
    @Transactional
    public void markUsed(Coupon coupon) {
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepo.save(coupon);
    }
}