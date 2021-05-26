package com.example.demo.controller;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.SecurityContextProvider;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.BenhNhanService;
import com.example.demo.service.LichHenService;
import com.example.demo.service.NhanVienService;
import com.example.demo.service.TaiKhoanService;

import enity.BenhNhan;
import enity.LichHen;
import enity.NhanVien;
import enity.TaiKhoan;




@Controller
public class MyController {
	@Autowired
	TaiKhoanService taikhoanservice;
	@Autowired
	BenhNhanService benhnhanservice;
	@Autowired
	NhanVienService nhanvienservice;
	@Autowired 
	LichHenService lichhenservice;
	@Autowired
    public JavaMailSender javaMailSender;
	 
	
	@GetMapping(value = {"/","/trang-chu"})
	public String hienThiTrangChu(Principal principal,Model model ) {
		if(principal!=null) {
			System.out.println(principal.getName());
		}
	
		 Object principal1 = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 //BCryptPasswordEncoder passwordEncoder =new BCryptPasswordEncoder();
		//System.out.println("mat khau la :" +passwordEncoder.encode("anh"));	
	     
		    if (principal1 instanceof UserDetails) {
		        String username = ((UserDetails) principal1).getUsername();
		        System.out.println("username là _------------------- :" +username );
		        String password = ((UserDetails) principal1).getPassword();
		        System.out.println("password là _------------------- :" +password );
		        model.addAttribute("us", username);
		     //   model.addAttribute("chucvu",  roleservice.timTenQuyen(username));
		      //  System.out.println("username là :" + username);
		   //     System.out.println("Tên Chức Danh :" + roleservice.timTenQuyen(username));
		      
		       
		    } else {
		    	
		        String username = principal1.toString();
		        model.addAttribute("us", username);

		        System.out.println("username là :" + username);
		   // model.addAttribute("chucvu",  roleservice.timTenQuyen(username));
		       
		    }
		 model.addAttribute("chucvu",null);
		return "index";
	}
	
	
	@GetMapping("/dich-vu")
	public String hienThiDanhSachDichVu() {
		return "dich-vu";
	}
	@GetMapping("/doimatkhau")
	public String hienthidoiMatKhau() {
		
			return "doi-mat-khau";
		
	}
	@PostMapping("/doimatkhau")
	public String doiMatKhau(@RequestParam("matkhaucu")String matkhaucu,
			@RequestParam("matkhaumoi")String matkhaumoi,
			@RequestParam("nhaplaimatkhau")String nhaplaimatkhau,Principal principal, RedirectAttributes redirectAttributes
			) throws IOException{
		
		TaiKhoan tk= new TaiKhoan();
		tk=taikhoanservice.GetOneTaiKhoan(principal.getName());
		if(tk.getPassword().equals(matkhaucu))
		{
			if(matkhaumoi.equals(nhaplaimatkhau))
			{
				tk.setPassword(matkhaumoi);
				taikhoanservice.UpdateTK(tk);
			}
			else
			{
				redirectAttributes.addFlashAttribute("thatbai", "Đổi mật khẩu thất bại!- Mật khẩu mới không giống với xác nhận mật khẩu");
				return "redirect:/doi-mat-khau";
			}
		}
		else
		{
			redirectAttributes.addFlashAttribute("thatbai", "Đổi mật khẩu thất bại!- Bạn đã điền sai mật khẩu cũ");
			return "redirect:/doi-mat-khau";
		}
		redirectAttributes.addFlashAttribute("thanhcong", "Đổi mật khẩu thành công!-");
		return "/logout";
	}
	
	@GetMapping(value = "/dang-nhap")
	public String showSignIn(HttpSession session, Model model) {
		
		
			TaiKhoan taiKhoan=new TaiKhoan();
			model.addAttribute("taiKhoan", new TaiKhoan());
			if(session.getAttribute("username")!=null) {
				session.invalidate();
			}
			return "dang-nhap";
	
	}
	@GetMapping("/thong-tin")
	public String thongTinCaNhan(Principal principal,Model model ) {
		 Object principal1 = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 
		 BenhNhan bn= new BenhNhan();
			try {
				bn= benhnhanservice.GetOneBenhNhanByUser(principal.getName());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(bn.getTen());
			model.addAttribute("benhnhan",bn);
		 
		 return "thong-tin";
		
	}
	
	@GetMapping("/dang-ky")
	public String hienThiDangKy(Model model) {
		BenhNhan benhNhan=new BenhNhan();
		benhNhan.setTaiKhoan(new TaiKhoan());
		model.addAttribute("benhNhan", new BenhNhan());
		return "dang-ky";
	}
	@PostMapping("/dang-ky")
	public String dangKy(@ModelAttribute("benhNhan")BenhNhan benhNhan,@ModelAttribute("taikhoan")TaiKhoan taikhoan,@RequestParam("ngay_sinh")String ngaysinh,RedirectAttributes redirectAttributes) throws IOException {
		
		TaiKhoan tkExist=null;
		tkExist=taikhoanservice.GetOneTaiKhoan(taikhoan.getUsername());
		if(tkExist.getUsername()!=null) {
			redirectAttributes.addFlashAttribute("thatbai", "Đăng ký thất bại!- Trùng username");
			return "index";
		}
		
		taikhoan.setRole(benhnhanservice.GetOneRole((long) 5));
		taikhoan.setPassword("123456");
		int ketquaAddTK=taikhoanservice.POSTRequest(taikhoan);
		if(ketquaAddTK==200)
		{
			benhNhan.setTaiKhoan(taikhoan);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date=new Date();
	        try {
	             date = formatter.parse(ngaysinh);
	        } catch (ParseException e) {
	            e.printStackTrace();
	        }
			benhNhan.setNgaySinh(date);
			int ketquaPOST=benhnhanservice.POSTBenhNhan(benhNhan);
			if (ketquaPOST==200)
			{
				BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
				taikhoan.setPassword(encoder.encode(taikhoan.getPassword()));
				redirectAttributes.addFlashAttribute("thanhcong", "Đăng ký thành công!");
				return "redirect:/dang-nhap";
			}
			else
			{
				redirectAttributes.addFlashAttribute("thatbai", "Đăng ký thất bại!");
				int ketqua=taikhoanservice.DeleteTaiKhoan(taikhoan.getUsername());
				return "redirect:/dang-ky";
				
			}
		}
		redirectAttributes.addFlashAttribute("thatbai", "Đăng ký thất bại!");
		return "redirect:/dang-ky";
	}

	
	
	@PostMapping("/doLogin")
	public String dangNhap(@ModelAttribute("taiKhoan")TaiKhoan taikhoan,RedirectAttributes redirectAttributes) throws IOException {
		
		System.out.println("User name :" + taikhoan.getUsername());
		TaiKhoan tkExist=taikhoanservice.GetOneTaiKhoan(taikhoan.getUsername());
		if(taikhoan.getUsername()!=null)	
		{
			if(tkExist.getPassword().equals(taikhoan.getPassword()))
				return "redirect:/index";
		}
		
		return "redirect:/dang-nhap";
	}
	
	@GetMapping("/thong-bao")
	public String thongBaoThanhCong() {
		return "thong-bao";
	}
	
	@GetMapping("/logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/dang-nhap";
    }
	
	@GetMapping("/dat-lich")
	public String hienThiDatLich(Model model, Principal principal) throws IOException {
		
		TaiKhoan user = taikhoanservice.GetOneTaiKhoan(principal.getName());
		if (user.getUsername() == null) {
			return "redirect:/dang-nhap";
		}
		BenhNhan benhNhan = benhnhanservice.GetOneBenhNhanByUser(principal.getName());
		LichHen lichHen = new LichHen();
		lichHen.setBenhNhan(benhNhan);
		model.addAttribute("lichHen", lichHen);
		NhanVien nhanVien = nhanvienservice.GetOneNhanVien((long) (2));
		System.out.println("User name :" + nhanVien.getTen());
		return "dat-lich";
	}
	@PostMapping("/dat-lich")
	public String datLich(@ModelAttribute("lichHen") LichHen lichHen,
			@RequestParam(value = "manhanvien", required = false) String manhanvien, Principal principal, 
			@RequestParam(value = "thoigiankham", required = false) String thoigiankham,
			RedirectAttributes redirectAttributes) throws IOException {
		TaiKhoan user = taikhoanservice.GetOneTaiKhoan(principal.getName());
		if (user.getUsername() == null) {
			return "redirect:/dang-nhap";
		}
		BenhNhan benhNhan = benhnhanservice.GetOneBenhNhanByUser(principal.getName());
		
		NhanVien nhanVien = nhanvienservice.GetOneNhanVien((long)(Integer.parseInt(manhanvien)));
		System.out.println("User name :" + nhanVien.getTen());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date=new Date();
        try {
             date = formatter.parse(thoigiankham);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        LichHen lh=null;
        lh=lichhenservice.GetLichHenBenhNhan(lichhenservice.doichuoitungay(date), benhNhan.getId());	
		lichHen.setBenhNhan(benhNhan);
		lichHen.setNhanVien(nhanVien);
		lichHen.setThoiGian(date);
		lichHen.setHinhThuc(true);
		lichHen.setTrangThai("3");
		if(lh==null)
		{
			int ketqua=lichhenservice.POSTLichHen(lichHen);
			
			if(ketqua==200)
			{
				
				Thread thread = new Thread(() -> {
					MimeMessage message = javaMailSender.createMimeMessage();
					MimeMessageHelper messageHelper = new MimeMessageHelper(message);
					try {
						
					messageHelper.setSubject("Xác nhận đặt lịch tại phòng khám");
					messageHelper.setFrom("phongkhamlv@gmail.com", "Phòng khám LV");
					messageHelper.setTo(benhNhan.getEmail());
					String content = "<b>Xin chào " + benhNhan.getTen() + "</b> <br>";
					content += "Chúng tôi đã nhận được lịch hẹn trước của bạn tại phòng khám với các thông tin như sau : <br>";
					content += "Họ và tên :" + benhNhan.getTen() + "<br>";
					content += "Ngày sinh : " + benhnhanservice.doichuoitungay(benhNhan.getNgaySinh()) + "<br>";
					String gioitinh="";
					if(benhNhan.isGioiTinh())
						gioitinh="Nam";
					else
						gioitinh="Nữ";
					content += "Giới tính : " + gioitinh + "<br>";
					content += "Vào lúc : " + lichhenservice.doichuoitungay(lichHen.getThoiGian()) + "<br>";
					if (nhanVien != null) {
						content += "Với bác sỹ : " + nhanVien.getTen() + "<br>";
					}
					content += "Vui lòng có mặt tại phòng khám để nhận được dịch vụ tốt nhất!" + "<br>";
					content += "<br>";
					content += "Cảm ơn bạn đã đặt lịch ở phòng khám chúng tôi.";
					messageHelper.setText(content, true);
					javaMailSender.send(message);
					}catch (Exception e) {
					}
				});
				thread.start();
				///
				redirectAttributes.addFlashAttribute("message", "Bạn đã đặt lịch hẹn thành công");
				return "redirect:/thong-bao";
			}
		}
		else
		{
			redirectAttributes.addFlashAttribute("message", "Mỗi ngày bạn chỉ đặt được 1 lịch hẹn ");
			return "redirect:/thong-bao";
		}
		
		redirectAttributes.addFlashAttribute("message", " Bạn đã đặt lịch hẹn thất bại");
		return "redirect:/dat-lich";
	}
	
}
