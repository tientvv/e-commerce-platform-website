<template>
  <main class="max-w-[1440px] mx-auto w-full px-4 pt-4">
    <div class="flex justify-between gap-8 pb-8 border-b">
      <div class="w-1/5">
        <div class="mb-4">Hỗ trợ khách hàng</div>
        <ul class="flex flex-col gap-1">
          <li>Hotline: 1900-6035</li>
          <li>(1000 đ/phút, 8h-21h kể cả T7, CN)</li>
          <li>Chính sách kiểm hàng</li>
          <li>Chính sách đổi trả</li>
          <li>Chính sách hàng nhập khẩu</li>
          <li>Hỗ trợ khách hàng: hotro@tt.vn</li>
          <li>Báo lỗi bảo mật: security@tt.vn</li>
        </ul>
      </div>
      <div class="w-1/5">
        <div class="mb-4">Về TT E-Commerce</div>
        <ul class="flex flex-col gap-1">
          <li>Giới thiệu TT E-Commerce</li>
          <li>Chính sách bảo mật thanh toán</li>
          <li>Chính sách bảo mật thông tin cá nhân</li>
          <li>Chính sách giải quyết khiếu nại</li>
          <li>Bán hàng danh nghiệp</li>
        </ul>
      </div>
      <div class="w-1/5">
        <div class="mb-4">Hợp tác và liên kết</div>
        <ul class="flex flex-col gap-1">
          <li>Quy chế hoạt động Sàn GDTMĐT</li>
        </ul>
      </div>
      <div class="w-1/5">
        <div class="mb-4">Phương thức thanh toán</div>
        <ul class="flex flex-row gap-4">
          <li><img class="w-[30px] h-auto" :src="ZaloPay" /></li>
          <li><img class="w-[30px] h-auto" :src="MomoIcon" /></li>
          <li><img class="w-[30px] h-auto" :src="VNPayIcon" /></li>
        </ul>
      </div>
      <div class="w-1/5">
        <div class="mb-4">Kết nối với chúng tôi</div>
        <ul class="flex flex-row gap-4">
          <li><img class="w-[30px] h-auto" :src="FacebookIcon" /></li>
          <li><img class="w-[30px] h-auto" :src="InstagramIcon" /></li>
          <li><img class="w-[30px] h-auto" :src="TiktokIcon" /></li>
          <li><img class="w-[30px] h-auto" :src="ZaloIcon" /></li>
        </ul>
      </div>
    </div>
    <div class="mt-4 pb-4">
      <div class="text-[16px] font-bold">Công ty TNHH TT E-Commerce</div>
      <div>Số 000 đường Nguyễn Văn Linh, Phường Thạc Gián, Quận Thanh Khê, Thành phố Đà Nẵng</div>
      <div>Hotline: 0000 0000</div>
    </div>
  </main>
</template>

<script setup>
import FacebookIcon from '@/assets/icons/facebook.png'
import TiktokIcon from '@/assets/icons/tiktok.png'
import InstagramIcon from '@/assets/icons/instagram.png'
import ZaloIcon from '@/assets/icons/zalo.png'
import ZaloPay from '@/assets/icons/zalopay.png'
import MomoIcon from '@/assets/icons/momo.png'
import VNPayIcon from '@/assets/icons/vnpay.svg'
</script>
