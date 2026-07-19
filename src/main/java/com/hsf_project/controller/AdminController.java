package com.hsf_project.controller;

import com.hsf_project.entity.*;
import com.hsf_project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.*;
import java.time.*;
import java.util.*;

@Controller @RequestMapping("/admin") @Transactional
public class AdminController {
    private static final List<String> ROOM_TYPES=List.of("2D","3D","IMAX"), SEAT_TYPES=List.of("STANDARD","VIP","SWEETBOX");
    /** Số ghế mỗi hàng: sweetbox là ghế đôi nên chỉ 8 ghế/hàng (khớp Data.sql và sơ đồ ghế lúc booking). */
    private static final int SEATS_PER_ROW=10, SWEETBOX_SEATS_PER_ROW=8, MAX_ROWS=26;
    private final CityRepository cities; private final CinemaRepository cinemas; private final CinemaRoomRepository rooms;
    private final SeatRepository seats; private final TicketRepository tickets; private final TicketPriceRepository prices;
    private final ComboRepository combos; private final BookingRepository bookings; private final ShowTimeRepository showTimes;
    public AdminController(CityRepository a,CinemaRepository b,CinemaRoomRepository c,SeatRepository d,TicketRepository e,TicketPriceRepository f,ComboRepository g,BookingRepository h,ShowTimeRepository i){cities=a;cinemas=b;rooms=c;seats=d;tickets=e;prices=f;combos=g;bookings=h;showTimes=i;}

    @GetMapping("/locations") public String locations(Model m){List<Cinema> cinemaList=cinemas.findByIsDeletedFalseOrderByNameAsc();Map<Integer,Long> cityCinemaCounts=new HashMap<>();cinemaList.forEach(x->cityCinemaCounts.merge(x.getCity().getId(),1L,Long::sum));m.addAttribute("cities",cities.findByIsDeletedFalseOrderByNameAsc());m.addAttribute("cinemas",cinemaList);m.addAttribute("cityCinemaCounts",cityCinemaCounts);List<CinemaRoom> roomList=rooms.findByIsDeletedFalseOrderByCinemaNameAscNameAsc();Map<Integer,Map<String,Integer>> roomLayouts=new HashMap<>();roomList.forEach(r->roomLayouts.put(r.getId(),countRows(r.getId())));Map<Integer,Long> cinemaRoomCounts=new HashMap<>(),cityRoomCounts=new HashMap<>();roomList.forEach(r->{cinemaRoomCounts.merge(r.getCinema().getId(),1L,Long::sum);cityRoomCounts.merge(r.getCinema().getCity().getId(),1L,Long::sum);});m.addAttribute("cinemaRoomCounts",cinemaRoomCounts);m.addAttribute("cityRoomCounts",cityRoomCounts);m.addAttribute("rooms",roomList);m.addAttribute("roomLayouts",roomLayouts);m.addAttribute("roomTypes",ROOM_TYPES);m.addAttribute("active","locations");return "admin/locations";}

    /** Đếm số hàng ghế hiện có theo từng loại, để modal "sửa phòng" hiển thị đúng bố cục đang dùng. */
    private Map<String,Integer> countRows(Integer roomId){Map<String,Set<String>> rowsByType=new HashMap<>();SEAT_TYPES.forEach(t->rowsByType.put(t,new HashSet<>()));for(Seat s:seats.findByRoomIdAndIsDeletedFalseOrderByRowLabelAscSeatNumberAsc(roomId)){Set<String> set=rowsByType.get(s.getType());if(set!=null)set.add(s.getRowLabel());}Map<String,Integer> out=new HashMap<>();rowsByType.forEach((t,set)->out.put(t,set.size()));return out;}

    @PostMapping("/cities/save") public String saveCity(@RequestParam(required=false) Integer id,@RequestParam String name,RedirectAttributes ra){
        if(name.trim().isBlank())return err(ra,"Tên thành phố không được để trống","/admin/locations"); City x=id==null?new City():cities.findById(id).orElseThrow();x.setName(name.trim());x.setIsDeleted(false);cities.save(x);return ok(ra,"Đã lưu thành phố","/admin/locations");}
    @PostMapping("/cities/{id}/delete") public String delCity(@PathVariable Integer id,RedirectAttributes ra){
        long upcoming=showTimes.countUpcomingByCity(id,LocalDateTime.now());
        if(upcoming>0)return err(ra,"Không thể xóa thành phố vì còn "+upcoming+" suất chiếu sắp tới","/admin/locations");
        City x=cities.findById(id).orElseThrow();
        List<Cinema> children=cinemas.findByCityIdAndIsDeletedFalse(id);int roomCount=0;
        for(Cinema c:children){roomCount+=softDeleteCinema(c);}
        x.setIsDeleted(true);cities.save(x);
        return ok(ra,"Đã xóa thành phố"+(children.isEmpty()?"":" cùng "+children.size()+" rạp và "+roomCount+" phòng chiếu"),"/admin/locations");}
    @PostMapping("/cinemas/save") public String saveCinema(@RequestParam(required=false) Integer id,@RequestParam String name,@RequestParam String address,@RequestParam Integer cityId,RedirectAttributes ra){if(name.isBlank()||address.isBlank())return err(ra,"Tên và địa chỉ rạp là bắt buộc","/admin/locations");Cinema x=id==null?new Cinema():cinemas.findById(id).orElseThrow();x.setName(name.trim());x.setAddress(address.trim());x.setCity(cities.findById(cityId).orElseThrow());x.setIsDeleted(false);cinemas.save(x);return ok(ra,"Đã lưu rạp chiếu","/admin/locations");}
    @PostMapping("/cinemas/{id}/delete") public String delCinema(@PathVariable Integer id,RedirectAttributes ra){
        long upcoming=showTimes.countUpcomingByCinema(id,LocalDateTime.now());
        if(upcoming>0)return err(ra,"Không thể xóa rạp vì còn "+upcoming+" suất chiếu sắp tới","/admin/locations");
        int roomCount=softDeleteCinema(cinemas.findById(id).orElseThrow());
        return ok(ra,"Đã xóa rạp"+(roomCount==0?"":" cùng "+roomCount+" phòng chiếu"),"/admin/locations");}

    @PostMapping("/rooms/save") public String saveRoom(@RequestParam(required=false) Integer id,@RequestParam String name,@RequestParam String roomType,@RequestParam Integer standardRows,@RequestParam Integer vipRows,@RequestParam Integer sweetboxRows,@RequestParam Integer cinemaId,RedirectAttributes ra){
        if(!ROOM_TYPES.contains(roomType)||name.isBlank())return err(ra,"Thông tin phòng không hợp lệ","/admin/locations");
        if(standardRows<0||vipRows<0||sweetboxRows<0)return err(ra,"Số hàng ghế không được âm","/admin/locations");
        int rowCount=standardRows+vipRows+sweetboxRows;
        if(rowCount<1||rowCount>MAX_ROWS)return err(ra,"Tổng số hàng ghế phải từ 1 đến "+MAX_ROWS,"/admin/locations");
        int totalSeats=(standardRows+vipRows)*SEATS_PER_ROW+sweetboxRows*SWEETBOX_SEATS_PER_ROW;
        CinemaRoom x=id==null?new CinemaRoom():rooms.findById(id).orElseThrow();boolean fresh=x.getId()==null;
        // Bố cục ghế đổi khi số ghế đổi HOẶC khi tỉ lệ 3 loại hàng đổi (vd đổi 1 hàng VIP thành sweetbox).
        boolean layoutChanged=!fresh&&!countRows(x.getId()).equals(Map.of("STANDARD",standardRows,"VIP",vipRows,"SWEETBOX",sweetboxRows));
        if(layoutChanged&&tickets.countByTicketPriceRoomId(x.getId())>0)return err(ra,"Không thể đổi sơ đồ ghế vì phòng đã phát sinh vé","/admin/locations");
        x.setName(name.trim());x.setRoomType(roomType);x.setTotalSeats(totalSeats);x.setCinema(cinemas.findById(cinemaId).orElseThrow());x.setIsDeleted(false);rooms.save(x);
        if(fresh||layoutChanged){if(layoutChanged){List<Seat> old=seats.findByRoomIdAndIsDeletedFalseOrderByRowLabelAscSeatNumberAsc(x.getId());old.forEach(s->{s.setIsActive(false);s.setIsDeleted(true);});seats.saveAll(old);}createSeats(x,standardRows,vipRows,sweetboxRows);}
        if(fresh){for(String st:SEAT_TYPES){TicketPrice p=new TicketPrice();p.setRoom(x);p.setSeatType(st);p.setPrice(defaultPrice(roomType,st));p.setDeleted(false);prices.save(p);}}
        return ok(ra,"Đã lưu phòng chiếu ("+totalSeats+" ghế)"+((fresh||layoutChanged)?" và cập nhật sơ đồ ghế":""),"/admin/locations");}
    @PostMapping("/rooms/{id}/delete") public String delRoom(@PathVariable Integer id,RedirectAttributes ra){
        long upcoming=showTimes.countUpcomingByRoom(id,LocalDateTime.now());
        if(upcoming>0)return err(ra,"Không thể xóa phòng vì còn "+upcoming+" suất chiếu sắp tới","/admin/locations");
        softDeleteRoom(rooms.findById(id).orElseThrow());
        return ok(ra,"Đã xóa phòng chiếu cùng sơ đồ ghế và bảng giá","/admin/locations");}

    /** Ẩn rạp và toàn bộ phòng con của nó. Trả về số phòng đã ẩn. */
    private int softDeleteCinema(Cinema c){
        List<CinemaRoom> children=rooms.findByCinemaIdAndIsDeletedFalseOrderByNameAsc(c.getId());
        children.forEach(this::softDeleteRoom);
        c.setIsDeleted(true);cinemas.save(c);
        return children.size();}

    /** Ẩn phòng cùng sơ đồ ghế và bảng giá vé của phòng, tránh để lại dữ liệu mồ côi. */
    private void softDeleteRoom(CinemaRoom r){
        List<Seat> roomSeats=seats.findByRoomIdAndIsDeletedFalseOrderByRowLabelAscSeatNumberAsc(r.getId());
        roomSeats.forEach(s->{s.setIsActive(false);s.setIsDeleted(true);});seats.saveAll(roomSeats);
        List<TicketPrice> roomPrices=prices.findByRoomIdAndIsDeletedFalse(r.getId());
        roomPrices.forEach(p->p.setDeleted(true));prices.saveAll(roomPrices);
        r.setIsDeleted(true);rooms.save(r);}

    @GetMapping("/ticket-prices") public String prices(Model m){Map<String,BigDecimal> matrix=new LinkedHashMap<>();for(String rt:ROOM_TYPES)for(String st:SEAT_TYPES){List<TicketPrice> list=prices.findByRoomRoomTypeAndSeatTypeAndIsDeletedFalse(rt,st);matrix.put(rt+"_"+st,list.isEmpty()?defaultPrice(rt,st):list.getFirst().getPrice());}m.addAttribute("matrix",matrix);m.addAttribute("roomTypes",ROOM_TYPES);m.addAttribute("seatTypes",SEAT_TYPES);m.addAttribute("active","prices");return "admin/ticket-prices";}
    @PostMapping("/ticket-prices") public String savePrices(@RequestParam Map<String,String> values,RedirectAttributes ra){for(String rt:ROOM_TYPES)for(String st:SEAT_TYPES){String key=rt+"_"+st;BigDecimal amount;try{amount=new BigDecimal(values.get(key));}catch(Exception e){return err(ra,"Giá "+rt+" / "+st+" không hợp lệ","/admin/ticket-prices");}if(amount.signum()<=0)return err(ra,"Giá vé phải lớn hơn 0","/admin/ticket-prices");for(CinemaRoom room:rooms.findByRoomTypeAndIsDeletedFalse(rt)){TicketPrice p=prices.findByRoomIdAndSeatTypeAndIsDeletedFalse(room.getId(),st).orElseGet(()->{TicketPrice n=new TicketPrice();n.setRoom(room);n.setSeatType(st);n.setDeleted(false);return n;});p.setPrice(amount);prices.save(p);}}return ok(ra,"Đã cập nhật đồng bộ 9 mức giá cho tất cả phòng","/admin/ticket-prices");}

    @GetMapping("/combos") public String combos(Model m){m.addAttribute("combos",combos.findByIsDeletedFalseOrderByIdDesc());m.addAttribute("active","combos");return "admin/combos";}
    /** combo.price là DECIMAL(10,2) nên giá phải nhỏ hơn 100 triệu, nếu không Hibernate ném lỗi lúc INSERT. */
    private static final BigDecimal MAX_COMBO_PRICE=new BigDecimal("99999999");
    private static final int MAX_COMBO_QUANTITY=1_000_000;

    /**
     * Nhận price/quantity dưới dạng String rồi tự parse: khai báo thẳng BigDecimal/Integer
     * thì một con số vượt ngưỡng kiểu (hoặc chữ) làm Spring ném MethodArgumentTypeMismatchException
     * trước khi vào thân hàm, người dùng nhận trang 500 thay vì thông báo nhập sai.
     */
    @PostMapping("/combos/save") public String saveCombo(@RequestParam(required=false) Long id,@RequestParam String name,@RequestParam(required=false) String description,@RequestParam String price,@RequestParam String quantity,@RequestParam String status,RedirectAttributes ra){
        if(name.isBlank()||name.trim().length()>100)return err(ra,"Tên combo bắt buộc và tối đa 100 ký tự","/admin/combos");
        if(description!=null&&description.trim().length()>255)return err(ra,"Mô tả combo tối đa 255 ký tự","/admin/combos");
        if(!List.of("ACTIVE","INACTIVE").contains(status))return err(ra,"Trạng thái combo không hợp lệ","/admin/combos");
        BigDecimal priceValue;int quantityValue;
        try{priceValue=new BigDecimal(price.trim()).setScale(2,RoundingMode.HALF_UP);}catch(NumberFormatException|ArithmeticException e){return err(ra,"Giá bán phải là một số hợp lệ","/admin/combos");}
        try{quantityValue=Integer.parseInt(quantity.trim());}catch(NumberFormatException e){return err(ra,"Số lượng tồn phải là số nguyên trong khoảng 0 - "+MAX_COMBO_QUANTITY,"/admin/combos");}
        if(priceValue.signum()<=0)return err(ra,"Giá bán phải lớn hơn 0","/admin/combos");
        if(priceValue.compareTo(MAX_COMBO_PRICE)>0)return err(ra,"Giá bán tối đa là 99.999.999 ₫","/admin/combos");
        if(quantityValue<0)return err(ra,"Số lượng tồn không được âm","/admin/combos");
        if(quantityValue>MAX_COMBO_QUANTITY)return err(ra,"Số lượng tồn tối đa là "+MAX_COMBO_QUANTITY,"/admin/combos");
        Combo x=id==null?new Combo():combos.findById(id).orElseThrow();x.setName(name.trim());x.setDescription(description==null?"":description.trim());x.setPrice(priceValue);x.setQuantity(quantityValue);x.setStatus(status);x.setIsDeleted(false);combos.save(x);return ok(ra,"Đã lưu combo","/admin/combos");}
    @PostMapping("/combos/{id}/delete") public String delCombo(@PathVariable Long id,RedirectAttributes ra){Combo x=combos.findById(id).orElseThrow();x.setIsDeleted(true);x.setStatus("INACTIVE");combos.save(x);return ok(ra,"Đã xóa combo","/admin/combos");}

    @GetMapping("/dashboard/{type}") public String dashboard(@PathVariable String type,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,Model m){if(!List.of("cinema","combo").contains(type))type="cinema";LocalDate end=to==null?LocalDate.now():to,start=from==null?end.minusDays(29):from;if(start.isAfter(end)){LocalDate q=start;start=end;end=q;}buildDashboard(type,bookings.findPaidInRange(start.atStartOfDay(),end.plusDays(1).atStartOfDay()),start,end,m);m.addAttribute("from",start);m.addAttribute("to",end);m.addAttribute("type",type);m.addAttribute("active","dashboard-"+type);return "admin/cinema-combo-dashboard";}
    private void buildDashboard(String type,List<Booking> paid,LocalDate from,LocalDate to,Model m){Map<LocalDate,BigDecimal> daily=new TreeMap<>();for(LocalDate d=from;!d.isAfter(to);d=d.plusDays(1))daily.put(d,BigDecimal.ZERO);Map<String,BigDecimal> revenue=new HashMap<>();Map<String,Long> volume=new HashMap<>();for(Booking b:paid){LocalDate day=b.getBookingDate().toLocalDate();if(type.equals("combo")){for(BookingCombo bc:b.getBookingCombos()){daily.merge(day,bc.getTotalPrice(),BigDecimal::add);revenue.merge(bc.getCombo().getName(),bc.getTotalPrice(),BigDecimal::add);volume.merge(bc.getCombo().getName(),bc.getQuantity().longValue(),Long::sum);}}else for(Ticket t:b.getTickets()){BigDecimal amount=t.getTicketPrice().getPrice();String name=t.getShowtime().getRoom().getCinema().getName();daily.merge(day,amount,BigDecimal::add);revenue.merge(name,amount,BigDecimal::add);volume.merge(name,1L,Long::sum);}}BigDecimal total=daily.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);long count=volume.values().stream().mapToLong(Long::longValue).sum();List<String> names=revenue.entrySet().stream().sorted(Map.Entry.<String,BigDecimal>comparingByValue().reversed()).map(Map.Entry::getKey).toList();m.addAttribute("totalRevenue",total);m.addAttribute("totalVolume",count);m.addAttribute("average",count==0?BigDecimal.ZERO:total.divide(BigDecimal.valueOf(count),0,RoundingMode.HALF_UP));m.addAttribute("chartLabels",daily.keySet().stream().map(LocalDate::toString).toList());m.addAttribute("chartValues",daily.values());m.addAttribute("rankNames",names);m.addAttribute("rankRevenue",revenue);m.addAttribute("rankVolume",volume);}
    /** Sinh sơ đồ ghế theo hàng: thường từ hàng A trở đi, rồi VIP, sweetbox nằm cuối phòng (gần cuối rạp). */
    private void createSeats(CinemaRoom room,int standardRows,int vipRows,int sweetboxRows){
        List<Seat> list=new ArrayList<>();int row=0;
        for(String type:SEAT_TYPES){int rowsOfType=switch(type){case"VIP"->vipRows;case"SWEETBOX"->sweetboxRows;default->standardRows;};int perRow=type.equals("SWEETBOX")?SWEETBOX_SEATS_PER_ROW:SEATS_PER_ROW;
            for(int r=0;r<rowsOfType;r++,row++){String label=String.valueOf((char)('A'+row));
                for(int n=1;n<=perRow;n++){Seat s=new Seat();s.setRoom(room);s.setRowLabel(label);s.setSeatNumber(n);s.setSeatCode(label+n);s.setType(type);s.setIsActive(true);s.setIsDeleted(false);list.add(s);}}}
        seats.saveAll(list);}
    private BigDecimal defaultPrice(String r,String s){int base=switch(r){case"3D"->85000;case"IMAX"->95000;default->75000;};int extra=switch(s){case"VIP"->switch(r){case"3D"->45000;case"IMAX"->60000;default->35000;};case"SWEETBOX"->switch(r){case"3D"->130000;case"IMAX"->175000;default->120000;};default->0;};return BigDecimal.valueOf(base+extra);}
    private String ok(RedirectAttributes r,String m,String p){r.addFlashAttribute("success",m);return"redirect:"+p;}private String err(RedirectAttributes r,String m,String p){r.addFlashAttribute("error",m);return"redirect:"+p;}
}
