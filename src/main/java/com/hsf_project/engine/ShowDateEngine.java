package com.hsf_project.engine;

import com.hsf_project.dto.response.ShowDateResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ShowDateEngine {
    public List<ShowDateResponse> getShowDate(LocalDate selectDate){
        List<ShowDateResponse> showDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Locale vietnam = new Locale("vi", "VN");

        DateTimeFormatter monthFormatter =
                DateTimeFormatter.ofPattern("M", vietnam);

        DateTimeFormatter weekdayFormatter =
                DateTimeFormatter.ofPattern("EEEE", vietnam);

        boolean hasSelectDate = selectDate != null && selectDate.isAfter(today) && selectDate.isBefore(today.plusDays(10));

        for (int i = 0; i < 10; i++) {

            LocalDate date = today.plusDays(i);

            boolean selected;

            if(hasSelectDate){
                selected = date.isEqual(selectDate);
            }else{
                selected = i==0;
            }

            ShowDateResponse showDate = new ShowDateResponse(
                    date.toString(),
                    date.getDayOfMonth(),
                    date.format(monthFormatter),
                    date.format(weekdayFormatter),
                    selected
            );

            showDates.add(showDate);
        }
        return showDates;
    }
}
