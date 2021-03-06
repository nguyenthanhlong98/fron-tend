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
		        System.out.println("username l?? _------------------- :" +username );
		        String password = ((UserDetails) principal1).getPassword();
		        System.out.println("password l?? _------------------- :" +password );
		        model.addAttribute("us", username);
		     //   model.addAttribute("chucvu",  roleservice.timTenQuyen(username));
		      //  System.out.println("username l?? :" + username);
		   //     System.out.println("T??n Ch???c Danh :" + roleservice.timTenQuyen(username));
		      
		       
		    } else {
		    	
		        String username = principal1.toString();
		        model.addAttribute("us", username);

		        System.out.println("username l?? :" + username);
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
				redirectAttributes.addFlashAttribute("thatbai", "?????i m???t kh???u th???t b???i!- M???t kh???u m???i kh??ng gi???ng v???i x??c nh???n m???t kh???u");
				return "redirect:/doi-mat-khau";
			}
		}
		else
		{
			redirectAttributes.addFlashAttribute("thatbai", "?????i m???t kh???u th???t b???i!- B???n ???? ??i???n sai m???t kh???u c??");
			return "redirect:/doi-mat-khau";
		}
		redirectAttributes.addFlashAttribute("thanhcong", "?????i m???t kh???u th??nh c??ng!-");
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
			redirectAttributes.addFlashAttribute("thatbai", "????ng k?? th???t b???i!- Tr??ng username");
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
				redirectAttributes.addFlashAttribute("thanhcong", "????ng k?? th??nh c??ng!");
				return "redirect:/dang-nhap";
			}
			else
			{
				redirectAttributes.addFlashAttribute("thatbai", "????ng k?? th???t b???i!");
				int ketqua=taikhoanservice.DeleteTaiKhoan(taikhoan.getUsername());
				return "redirect:/dang-ky";
				
			}
		}
		redirectAttributes.addFlashAttribute("thatbai", "????ng k?? th???t b???i!");
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
						
					messageHelper.setSubject("X??c nh???n ?????t l???ch t???i ph??ng kh??m");
					messageHelper.setFrom("phongkhamlv@gmail.com", "Ph??ng kh??m LV");
					messageHelper.setTo(benhNhan.getEmail());
					String content = "<b>Xin ch??o " + benhNhan.getTen() + "</b> <br>";
					content += "Ch??ng t??i ???? nh???n ???????c l???ch h???n tr?????c c???a b???n t???i ph??ng kh??m v???i c??c th??ng tin nh?? sau : <br>";
					content += "H??? v?? t??n :" + benhNhan.getTen() + "<br>";
					content += "Ng??y sinh : " + benhnhanservice.doichuoitungay(benhNhan.getNgaySinh()) + "<br>";
					String gioitinh="";
					if(benhNhan.isGioiTinh())
						gioitinh="Nam";
					else
						gioitinh="N???";
					content += "Gi???i t??nh : " + gioitinh + "<br>";
					content += "V??o l??c : " + lichhenservice.doichuoitungay(lichHen.getThoiGian()) + "<br>";
					if (nhanVien != null) {
						content += "V???i b??c s??? : " + nhanVien.getTen() + "<br>";
					}
					content += "Vui l??ng c?? m???t t???i ph??ng kh??m ????? nh???n ???????c d???ch v??? t???t nh???t!" + "<br>";
					content += "<br>";
					content += "C???m ??n b???n ???? ?????t l???ch ??? ph??ng kh??m ch??ng t??i.";
					messageHelper.setText(content, true);
					javaMailSender.send(message);
					}catch (Exception e) {
					}
				});
				thread.start();
				///
				redirectAttributes.addFlashAttribute("message", "B???n ???? ?????t l???ch h???n th??nh c??ng");
				return "redirect:/thong-bao";
			}
		}
		else
		{
			redirectAttributes.addFlashAttribute("message", "M???i ng??y b???n ch??? ?????t ???????c 1 l???ch h???n ");
			return "redirect:/thong-bao";
		}
		
		redirectAttributes.addFlashAttribute("message", " B???n ???? ?????t l???ch h???n th???t b???i");
		return "redirect:/dat-lich";
	}
	
}
