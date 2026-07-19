package com.hsf_project.controller.admin;

import com.hsf_project.entity.*;
import com.hsf_project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin")
@Transactional
public class AdminSystemController {
    private static final List<String> ROOM_TYPES = List.of("2D", "3D", "IMAX");
    private static final List<String> SEAT_TYPES = List.of("STANDARD", "VIP", "SWEETBOX");
    private final CityRepository cities; private final CinemaRepository cinemas;
    private final CinemaRoomRepository rooms; private final TicketPriceRepository prices;
    private final ComboRepository combos; private final JdbcTemplate jdbc;

    public AdminSystemController(CityRepository cities, CinemaRepository cinemas, CinemaRoomRepository rooms,
                                 TicketPriceRepository prices, ComboRepository combos, JdbcTemplate jdbc) {
        this.cities= cities; this.cinemas=cinemas; this.rooms=rooms; this.prices=prices; this.combos=combos; this.jdbc=jdbc;
    }

    @GetMapping("/locations")
    public String locations(Model m) {
        List<City> cityList = cities.findByIsDeletedFalseOrderByNameAsc();
        List<Cinema> cinemaList = cinemas.findByIsDeletedFalseOrderByNameAsc();
        List<CinemaRoom> roomList = rooms.findByIsDeletedFalseOrderByCinemaNameAscNameAsc();
        Map<Integer, Long> cityCinemaCounts = new HashMap<>();
        Map<Integer, Long> cinemaRoomCounts = new HashMap<>();
        cinemaList.forEach(c -> cityCinemaCounts.merge(c.getCity().getId(), 1L, Long::sum));
        roomList.forEach(r -> cinemaRoomCounts.merge(r.getCinema().getId(), 1L, Long::sum));
        m.addAttribute("cities", cityList); m.addAttribute("cinemas", cinemaList); m.addAttribute("rooms", roomList);
        m.addAttribute("cityCinemaCounts", cityCinemaCounts); m.addAttribute("cinemaRoomCounts", cinemaRoomCounts);
        m.addAttribute("roomTypes", ROOM_TYPES); m.addAttribute("activePage","locations");
        return "admin/locations";
    }
    @PostMapping("/cities/save")
    public String saveCity(@RequestParam(required=false) Integer id,@RequestParam String name,RedirectAttributes ra){
        City x=id==null?new City():cities.findById(id).orElseThrow(); x.setName(name.trim());x.setIsDeleted(false);cities.save(x);return ok(ra,"Đã lưu thành phố","locations");}
    @PostMapping("/cinemas/save")
    public String saveCinema(@RequestParam(required=false) Integer id,@RequestParam String name,@RequestParam String address,@RequestParam Integer cityId,RedirectAttributes ra){
        Cinema x=id==null?new Cinema():cinemas.findById(id).orElseThrow();x.setName(name.trim());x.setAddress(address.trim());x.setCity(cities.findById(cityId).orElseThrow());x.setIsDeleted(false);cinemas.save(x);return ok(ra,"Đã lưu rạp","locations");}
    @PostMapping("/rooms/save")
    public String saveRoom(@RequestParam(required=false) Integer id,@RequestParam String name,@RequestParam String roomType,@RequestParam Integer totalSeats,@RequestParam Integer cinemaId,RedirectAttributes ra){
        CinemaRoom x=id==null?new CinemaRoom():rooms.findById(id).orElseThrow();boolean fresh=x.getId()==null;x.setName(name.trim());x.setRoomType(roomType);x.setTotalSeats(totalSeats);x.setCinema(cinemas.findById(cinemaId).orElseThrow());x.setIsDeleted(false);rooms.save(x);
        if(fresh)for(String seat:SEAT_TYPES){TicketPrice p=new TicketPrice();p.setRoom(x);p.setSeatType(seat);p.setPrice(defaultPrice(roomType,seat));p.setDeleted(false);prices.save(p);}
        return ok(ra,"Đã lưu phòng chiếu","locations");}
    @PostMapping("/{kind}/{id}/delete")
    public String deleteLocation(@PathVariable String kind,@PathVariable Integer id,RedirectAttributes ra){
        if(kind.equals("cities")){City x=cities.findById(id).orElseThrow();x.setIsDeleted(true);cities.save(x);}
        else if(kind.equals("cinemas")){Cinema x=cinemas.findById(id).orElseThrow();x.setIsDeleted(true);cinemas.save(x);}
        else {CinemaRoom x=rooms.findById(id).orElseThrow();x.setIsDeleted(true);rooms.save(x);}
        return ok(ra,"Đã ẩn dữ liệu","locations");}

    @GetMapping("/ticket-prices")
    public String ticketPrices(Model m){
        Map<String,BigDecimal> matrix=new LinkedHashMap<>();
        for(String rt:ROOM_TYPES)for(String st:SEAT_TYPES){var list=prices.findByRoomRoomTypeAndSeatTypeAndIsDeletedFalse(rt,st);matrix.put(rt+"_"+st,list.isEmpty()?defaultPrice(rt,st):list.getFirst().getPrice());}
        m.addAttribute("matrix",matrix);m.addAttribute("roomTypes",ROOM_TYPES);m.addAttribute("seatTypes",SEAT_TYPES);m.addAttribute("activePage","prices");return "admin/ticket-prices";}
    @PostMapping("/ticket-prices")
    public String savePrices(@RequestParam Map<String,String> form,RedirectAttributes ra){
        for(String rt:ROOM_TYPES)for(String st:SEAT_TYPES){BigDecimal value=new BigDecimal(form.get(rt+"_"+st));for(CinemaRoom room:rooms.findByRoomTypeAndIsDeletedFalse(rt)){TicketPrice p=prices.findByRoomIdAndSeatTypeAndIsDeletedFalse(room.getId(),st).orElseGet(()->{TicketPrice n=new TicketPrice();n.setRoom(room);n.setSeatType(st);n.setDeleted(false);return n;});p.setPrice(value);prices.save(p);}}
        return ok(ra,"Đã cập nhật bảng giá","ticket-prices");}

    @GetMapping("/combos")
    public String combos(Model m){m.addAttribute("combos",combos.findByIsDeletedFalseOrderByIdDesc());m.addAttribute("activePage","combos");return "admin/combos";}
    @PostMapping("/combos/save")
    public String saveCombo(@RequestParam(required=false) Long id,@RequestParam String name,@RequestParam(required=false) String description,@RequestParam BigDecimal price,@RequestParam Integer quantity,@RequestParam String status,RedirectAttributes ra){
        Combo x=id==null?new Combo():combos.findById(id).orElseThrow();x.setName(name.trim());x.setDescription(description);x.setPrice(price);x.setQuantity(quantity);x.setStatus(status);x.setIsDeleted(false);combos.save(x);return ok(ra,"Đã lưu combo","combos");}
    @PostMapping("/combos/{id}/delete")
    public String deleteCombo(@PathVariable Long id,RedirectAttributes ra){Combo x=combos.findById(id).orElseThrow();x.setIsDeleted(true);x.setStatus("INACTIVE");combos.save(x);return ok(ra,"Đã ẩn combo","combos");}

    @GetMapping("/dashboard/{type}")
    public String report(@PathVariable String type,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
                         @RequestParam(required=false) Integer cityId,
                         @RequestParam(required=false) Integer cinemaId, Model m){
        LocalDate end=to==null?LocalDate.now():to,start=from==null?end.minusDays(29):from;
        boolean combo=type.equals("combo");
        String scope = (cityId == null && cinemaId == null) ? "" :
                " and exists (select 1 from ticket tx join show_time sx on sx.id=tx.showtime_id join cinema_room rx on rx.id=sx.room_id join cinema cx on cx.id=rx.cinema_id where tx.booking_id=b.id" +
                        (cityId == null ? "" : " and cx.city_id="+cityId) +
                        (cinemaId == null ? "" : " and cx.id="+cinemaId) + ")";
        String sql=combo?
            "select cast(b.booking_date as date),coalesce(sum(bc.total_price),0) from booking b join booking_combo bc on bc.booking_id=b.id where b.booking_date>=? and b.booking_date<? and b.is_deleted=0"+scope+" group by cast(b.booking_date as date) order by 1":
            "select cast(b.booking_date as date),coalesce(sum(tp.price),0) from booking b join ticket t on t.booking_id=b.id join ticket_price tp on tp.id=t.ticket_price_id join show_time st on st.id=t.showtime_id join cinema_room r on r.id=st.room_id join cinema c on c.id=r.cinema_id where b.booking_date>=? and b.booking_date<? and b.is_deleted=0"+
                    (cityId == null ? "" : " and c.city_id="+cityId) + (cinemaId == null ? "" : " and c.id="+cinemaId) +
                    " group by cast(b.booking_date as date) order by 1";
        Map<LocalDate,BigDecimal> daily=new TreeMap<>();for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1))daily.put(d,BigDecimal.ZERO);
        jdbc.query(sql,rs->{daily.put(rs.getDate(1).toLocalDate(),rs.getBigDecimal(2));},start,end.plusDays(1));
        BigDecimal total=daily.values().stream().reduce(BigDecimal.ZERO,BigDecimal::add);
        List<Cinema> cinemaList=cinemas.findByIsDeletedFalseOrderByNameAsc();
        if(cityId!=null) cinemaList=cinemaList.stream().filter(c->c.getCity().getId().equals(cityId)).toList();
        m.addAttribute("cities",cities.findByIsDeletedFalseOrderByNameAsc());m.addAttribute("cinemas",cinemaList);
        m.addAttribute("selectedCityId",cityId);m.addAttribute("selectedCinemaId",cinemaId);
        m.addAttribute("totalRevenue",total);m.addAttribute("chartLabels",daily.keySet());m.addAttribute("chartValues",daily.values());m.addAttribute("from",start);m.addAttribute("to",end);m.addAttribute("type",combo?"combo":"cinema");m.addAttribute("activePage","dashboard-"+(combo?"combo":"cinema"));return "admin/business-dashboard";}
    private BigDecimal defaultPrice(String room,String seat){int base=room.equals("IMAX")?95000:room.equals("3D")?85000:75000;int extra=seat.equals("SWEETBOX")?120000:seat.equals("VIP")?35000:0;return BigDecimal.valueOf(base+extra);}
    private String ok(RedirectAttributes ra,String msg,String page){ra.addFlashAttribute("success",msg);return "redirect:/admin/"+page;}
}
